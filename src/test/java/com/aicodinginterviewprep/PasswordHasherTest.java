package com.aicodinginterviewprep;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashVerifiesCorrectPassword() {
        String stored = PasswordHasher.hash("Hunter2!");

        assertTrue(PasswordHasher.verify("Hunter2!", stored));
    }

    @Test
    void verifyRejectsWrongPassword() {
        String stored = PasswordHasher.hash("Hunter2!");

        assertFalse(PasswordHasher.verify("hunter2!", stored));
    }

    @Test
    void hashIsSaltedAndFitsTheColumn() {
        String first = PasswordHasher.hash("Hunter2!");
        String second = PasswordHasher.hash("Hunter2!");

        assertNotEquals(first, second);
        assertTrue(first.length() <= 255);
        assertFalse(first.contains("Hunter2!"));
    }

    @Test
    void verifyRejectsMalformedOrNullInput() {
        assertFalse(PasswordHasher.verify("x", null));
        assertFalse(PasswordHasher.verify(null, PasswordHasher.hash("x")));
        assertFalse(PasswordHasher.verify("x", ""));
        assertFalse(PasswordHasher.verify("x", "plaintext"));
        assertFalse(PasswordHasher.verify("x", "pbkdf2$abc$!!$!!"));
        assertFalse(PasswordHasher.verify("x", "md5$1$AA==$AA=="));
    }
}
