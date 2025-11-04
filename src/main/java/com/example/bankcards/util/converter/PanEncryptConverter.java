package com.example.bankcards.util.converter;

import com.example.bankcards.util.encryptors.CryptoUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;


/**
 * JPA {@link jakarta.persistence.AttributeConverter} that transparently encrypts and decrypts
 * card Primary Account Numbers (PANs) when persisting and retrieving {@link String} values.
 * <p>
 * This converter ensures that sensitive card data is never stored in plaintext within
 * the database. Encryption and decryption are delegated to {@link com.example.bankcards.util.encryptors.CryptoUtils},
 * which handles key management and cryptographic operations.
 *
 * <p><b>Behavior:</b>
 * <ul>
 *   <li>When saving entities, the converter encrypts the raw PAN before it is persisted.</li>
 *   <li>When reading entities, it decrypts the stored value back into plaintext.</li>
 *   <li>If decryption fails but the database value matches a 16-digit pattern,
 *       it is assumed to be a legacy unencrypted PAN, and a warning is logged.</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>
 * &#64;Convert(converter = PanEncryptConverter.class)
 * private String pan;
 * </pre>
 *
 * @see com.example.bankcards.util.encryptors.CryptoUtils
 * @see jakarta.persistence.AttributeConverter
 * @see jakarta.persistence.Convert
 */
@Converter
public class PanEncryptConverter implements AttributeConverter<String, String> {
    private static final Logger log = LoggerFactory.getLogger(PanEncryptConverter.class);
    private static final Pattern RAW_PAN = Pattern.compile("\\d{16}");


    /**
     * Converts the raw PAN value into an encrypted string for database storage.
     *
     * @param attribute the plaintext PAN (may be {@code null})
     * @return encrypted PAN string, or {@code null} if input is {@code null}
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : CryptoUtils.encrypt(attribute);
    }


    /**
     * Decrypts an encrypted PAN value retrieved from the database.
     * <p>
     * If decryption fails but the stored value matches a 16-digit numeric pattern,
     * the method assumes the value originates from a legacy dataset that predates encryption
     * and returns it as-is after logging a warning.
     *
     * @param dbData the encrypted (or legacy plaintext) PAN from the database
     * @return decrypted plaintext PAN, or raw value if legacy
     * @throws IllegalStateException if the value cannot be decrypted or recognized as a valid PAN
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            return CryptoUtils.decrypt(dbData);
        } catch (IllegalStateException ex) {
            // Legacy databases may contain raw PAN values that predate encryption.
            if (RAW_PAN.matcher(dbData).matches()) {
                log.warn("Card number found without encryption; returning raw value");
                return dbData;
            }
            throw ex;
        }
    }
}