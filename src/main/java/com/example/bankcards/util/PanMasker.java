package com.example.bankcards.util;


// mask for card numbers
public final class PanMasker {
    private PanMasker() {}
    public static String mask(String pan) {
        if (pan == null || pan.length() < 4) return "****";
        String last4 = pan.substring(pan.length()-4);
        return "**** **** **** " + last4;
    }
}
