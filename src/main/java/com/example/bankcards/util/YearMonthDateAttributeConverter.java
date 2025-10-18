package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.YearMonth;

@Converter(autoApply = false)
public class YearMonthDateAttributeConverter implements AttributeConverter<YearMonth, LocalDate> {
    @Override
    public LocalDate convertToDatabaseColumn(YearMonth attribute) {
        return (attribute == null) ? null : attribute.atEndOfMonth(); // store as YYYY-MM-DD (last day of the month)
    }
    @Override
    public YearMonth convertToEntityAttribute(LocalDate dbData) {
        return (dbData == null) ? null : YearMonth.from(dbData);
    }
}
