package com.example.bankcards.util.encryptors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

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