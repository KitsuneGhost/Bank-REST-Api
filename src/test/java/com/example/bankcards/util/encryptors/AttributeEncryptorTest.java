package com.example.bankcards.util.encryptors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeEncryptorTest {

    private final AttributeEncryptor converter = new AttributeEncryptor();

    @Test
    void decryptsEncryptedAttribute() {
        String ciphertext = converter.convertToDatabaseColumn("123");
        assertEquals("123", converter.convertToEntityAttribute(ciphertext));
    }

    @Test
    void returnsRawDigitsWhenLegacyPlainTextIsStored() {
        assertEquals("123", converter.convertToEntityAttribute("123"));
        assertEquals("1234", converter.convertToEntityAttribute("1234"));
    }
}
