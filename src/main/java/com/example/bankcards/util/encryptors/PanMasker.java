package com.example.bankcards.util.encryptors;


/**
 * Utility class for masking card Primary Account Numbers (PANs)
 * before exposing them in API responses or logs.
 * <p>
 * The {@link #mask(String)} method replaces all but the last four digits
 * of the card number with a standard mask pattern.
 * <p>
 * This ensures that sensitive financial data is never displayed or transmitted
 * in full, maintaining compliance with PCI DSS and general security best practices.
 *
 * <p><b>Behavior:</b>
 * <ul>
 *   <li>If the input is {@code null} or shorter than 4 digits, returns {@code "****"}.</li>
 *   <li>Otherwise, returns a formatted masked string like {@code "**** **** **** 1234"}.</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>
 * String masked = PanMasker.mask("1234567812345678");
 * // Result: "**** **** **** 5678"
 * </pre>
 *
 * @see com.example.bankcards.util.converter.PanEncryptConverter
 * @see com.example.bankcards.util.encryptors.AttributeEncryptor
 */
public final class PanMasker {

    /** Private constructor to prevent instantiation (utility class). */
    private PanMasker() {}


    /**
     * Masks a card PAN so that only the last four digits are visible.
     *
     * @param pan the full 16-digit card number, or {@code null}
     * @return a masked string with only the last four digits visible,
     *         or {@code "****"} if the input is {@code null} or too short
     */
    public static String mask(String pan) {
        if (pan == null || pan.length() < 4) return "****";
        String last4 = pan.substring(pan.length()-4);
        return "**** **** **** " + last4;
    }
}
