package com.example.bankcards.util.encryptors;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class CryptoUtils {
    private static final String ALG = "AES";
    private static final String TRANS = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;
    private static final SecureRandom RNG = new SecureRandom();
    private static final byte[] KEY = Base64.getDecoder()
            .decode(System.getenv().getOrDefault("PAN_AES_KEY_B64",
                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")); // fallback for dev

    private CryptoUtils(){}

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
