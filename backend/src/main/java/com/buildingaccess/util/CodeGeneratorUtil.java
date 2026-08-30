package com.buildingaccess.util;

import java.security.SecureRandom;

/** Generiše čitljive, jedinstvene kodove za bedževe i propusnice (npr. RES-4F7A2C1B). */
public final class CodeGeneratorUtil {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // bez lako zamenljivih znakova (0/O, 1/I)
    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeGeneratorUtil() {
    }

    public static String generate(String prefix, int randomLength) {
        StringBuilder sb = new StringBuilder(prefix).append('-');
        for (int i = 0; i < randomLength; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
