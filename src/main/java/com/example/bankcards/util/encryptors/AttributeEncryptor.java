package com.example.bankcards.util.encryptors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;


/**
 * JPA {@link jakarta.persistence.AttributeConverter} that transparently encrypts and decrypts
 * sensitive string attributes (e.g., card PIN, CVV) using AES encryption.
 * <p>
 * This converter ensures that sensitive data is never stored in plaintext
 * within the database. Encryption and decryption are handled automatically
 * whenever entities are persisted or loaded via JPA.
 *
 * <p><b>Key Management:</b>
 * <ul>
 *   <li>The AES key is loaded from the environment variable
 *       {@code ATTRIBUTE_AES_KEY_B64}, which must contain a Base64-encoded key.</li>
 *   <li>If the variable is missing, a static fallback key is used for development only.</li>
 *   <li>In production, you must provide a securely managed 128/192/256-bit key.</li>
 * </ul>
 *
 * <p><b>Behavior:</b>
 * <ul>
 *   <li>During writes, plaintext values are AES-encrypted and Base64-encoded before persistence.</li>
 *   <li>During reads, values are Base64-decoded and decrypted.</li>
 *   <li>If decryption fails and the stored value appears to be raw digits
 *       (3–4 digits, e.g., a CVV), it is treated as a legacy unencrypted record
 *       and returned as-is after logging a warning.</li>
 * </ul>
 *
 * <p><b>Example usage:</b>
 * <pre>
 * &#64;Convert(converter = AttributeEncryptor.class)
 * private String cvv;
 * </pre>
 *
 * @see jakarta.persistence.AttributeConverter
 * @see jakarta.persistence.Convert
 * @see javax.crypto.Cipher
 * @see javax.crypto.spec.SecretKeySpec
 */
@Converter
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final Logger log = LoggerFactory.getLogger(AttributeEncryptor.class);
    private static final Pattern RAW_DIGITS = Pattern.compile("\\d{3,4}");

    private static final String ALGORITHM = "AES";
    private static final byte[] KEY;

    static {
        // Load AES key from environment
        String keyBase64 = System.getenv().getOrDefault(
                "ATTRIBUTE_AES_KEY_B64",
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=" // fallback for dev
        );
        KEY = Base64.getDecoder().decode(keyBase64);
    }


    /**
     * Encrypts the given plaintext value before persisting it to the database.
     *
     * @param attribute the raw sensitive value (e.g., CVV or PIN)
     * @return the AES-encrypted and Base64-encoded representation, or {@code null} if input is null
     * @throws RuntimeException if encryption fails
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, ALGORITHM));
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Decrypts the given database value back into plaintext.
     * <p>
     * If decryption fails but the stored value matches a 3–4 digit pattern,
     * the method assumes the value was stored unencrypted (legacy data)
     * and logs a warning before returning it as-is.
     *
     * @param dbData the encrypted (or legacy plaintext) database value
     * @return decrypted plaintext string, or raw digits if legacy record
     * @throws RuntimeException if decryption fails for a non-legacy value
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, ALGORITHM));
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            if (RAW_DIGITS.matcher(dbData).matches()) {
                log.warn("Sensitive card attribute stored without encryption; returning raw value");
                return dbData;
            }
            if (e instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new RuntimeException(e);
        }
    }
}