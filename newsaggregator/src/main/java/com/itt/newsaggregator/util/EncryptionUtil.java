package com.itt.newsaggregator.util;

import java.util.Base64;

public class EncryptionUtil {
    public String encrypt(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes());
    }

    public String decrypt(String encrypted) {
        return new String(Base64.getDecoder().decode(encrypted));
    }
}
