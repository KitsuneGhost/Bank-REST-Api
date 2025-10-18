package com.example.bankcards.entity.convertor;

import com.example.bankcards.util.CryptoUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PanEncryptConverter implements AttributeConverter<String, String> {
    @Override public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : CryptoUtils.encrypt(attribute);
    }
    @Override public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : CryptoUtils.decrypt(dbData);
    }
}