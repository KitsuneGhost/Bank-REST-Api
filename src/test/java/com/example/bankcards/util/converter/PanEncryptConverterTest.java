package com.example.bankcards.util.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PanEncryptConverterTest {

    private final PanEncryptConverter converter = new PanEncryptConverter();

    @Test
    void decryptsEncryptedValue() {
        String ciphertext = converter.convertToDatabaseColumn("1234567890123456");
        assertEquals("1234567890123456", converter.convertToEntityAttribute(ciphertext));
    }

    @Test
    void returnsRawPanWhenLegacyPlainTextIsEncountered() {
        assertEquals("1234567890123456", converter.convertToEntityAttribute("1234567890123456"));
    }
}
