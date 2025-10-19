package com.example.bankcards.util.converter;

import com.example.bankcards.util.encryptors.CryptoUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

@Converter
public class PanEncryptConverter implements AttributeConverter<String, String> {

    private static final Logger log = LoggerFactory.getLogger(PanEncryptConverter.class);
    private static final Pattern RAW_PAN = Pattern.compile("\\d{16}");

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : CryptoUtils.encrypt(attribute);
    }

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
