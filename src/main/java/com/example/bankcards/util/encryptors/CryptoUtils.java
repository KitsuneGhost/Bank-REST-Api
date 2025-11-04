package com.example.bankcards.util.encryptors;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;


/**
 * Utility class providing AES-GCM encryption and decryption for sensitive data,
 * such as card Primary Account Numbers (PANs).
 * <p>
 * This class is designed to be stateless and thread-safe. All methods are static.
 * <p>
 * Encryption is performed using the {@code AES/GCM/NoPadding} transformation,
 * which provides authenticated encryption ensuring both confidentiality and integrity.
 *
 * <p><b>Key Management:</b>
 * <ul>
 *   <li>The AES key is loaded from the environment variable {@code PAN_AES_KEY_B64},
 *       which must contain a Base64-encoded AES key.</li>
 *   <li>If the variable is missing, a static fallback key is used for development only.</li>
 *   <li>In production, a secure 128-, 192-, or 256-bit AES key must be provided.</li>
 * </ul>
 *
 * <p><b>Encryption Format:</b>
 * <ul>
 *   <li>Random 12-byte IV (nonce) is generated for each encryption.</li>
 *   <li>Resulting ciphertext is prefixed with the IV before Base64 encoding.</li>
 *   <li>Decryption reverses this process: splits IV + ciphertext, then decrypts.</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>
 * String encrypted = CryptoUtils.encrypt("1234567812345678");
 * String decrypted = CryptoUtils.decrypt(encrypted);
 * </pre>
 *
 * @see javax.crypto.Cipher
 * @see javax.crypto.spec.GCMParameterSpec
 * @see javax.crypto.spec.SecretKeySpec
 * @see java.security.SecureRandom
 * @see com.example.bankcards.util.converter.PanEncryptConverter
 */
public final class CryptoUtils {
    private static final String ALG = "AES";
    private static final String TRANS = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;
    private static final SecureRandom RNG = new SecureRandom();
    private static final byte[] KEY = Base64.getDecoder()
            .decode(System.getenv().getOrDefault("PAN_AES_KEY_B64",
                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="));

    /** Private constructor to prevent instantiation (utility class). */
    private CryptoUtils(){}


    /**
     * Encrypts the given plaintext string using AES-GCM.
     * <p>
     * Generates a random IV (nonce) for each encryption operation and prepends it
     * to the ciphertext before Base64-encoding the final output.
     *
     * @param plaintext the input string to encrypt (must not be {@code null})
     * @return Base64-encoded ciphertext containing the IV + encrypted data
     * @throws IllegalStateException if encryption fails for any reason
     */
    public static String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_BYTES]; RNG.nextBytes(iv);
            Cipher c = Cipher.getInstance(TRANS);
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, ALG), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = c.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] packed = new byte[iv.length + ct.length];
            System.arraycopy(iv,0,packed,0,iv.length);
            System.arraycopy(ct,0,packed,iv.length,ct.length);
            return Base64.getEncoder().encodeToString(packed);
        } catch (Exception e) { throw new IllegalStateException("PAN encrypt failed", e); }
    }


    /**
     * Decrypts a Base64-encoded string produced by {@link #encrypt(String)}.
     * <p>
     * Extracts the IV from the beginning of the decoded byte array and uses it
     * to initialize the AES-GCM cipher for decryption.
     *
     * @param base64 the Base64-encoded ciphertext to decrypt
     * @return the decrypted plaintext string
     * @throws IllegalStateException if decryption fails (e.g., due to tampering or invalid key)
     */
    public static String decrypt(String base64) {
        try {
            byte[] packed = Base64.getDecoder().decode(base64);
            byte[] iv = java.util.Arrays.copyOfRange(packed, 0, IV_BYTES);
            byte[] ct = java.util.Arrays.copyOfRange(packed, IV_BYTES, packed.length);
            Cipher c = Cipher.getInstance(TRANS);
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, ALG), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] pt = c.doFinal(ct);
            return new String(pt, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("PAN decrypt failed", e); }
    }
}
