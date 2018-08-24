package eu.baboi.cristian.musicalstructure.utils.secret;

import android.text.TextUtils;

public class Key {

    private Key() {
        throw new AssertionError();
    }

    // return the password concatenated to itself to the length of key
    private static String getPassword(String password, String key) {
        if (TextUtils.isEmpty(password)) return key;
        if (TextUtils.isEmpty(key)) return password;

        int passwordLength = password.length();
        int keyLength = key.length();

        if (passwordLength >= keyLength) return password.substring(0, keyLength);

        int count = keyLength / passwordLength;
        StringBuilder builder = new StringBuilder(keyLength + passwordLength);

        for (int i = 0; i <= count; i++)
            builder.append(password);

        return builder.substring(0, keyLength);
    }

    // combine password with key and return a new string with the data
    private static String encode(String password, String key) {
        if (password == null) return key;
        if (key == null) return password;

        int passwordLength = password.length();
        int keyLength = key.length();
        int minLength = passwordLength < keyLength ? passwordLength : keyLength;

        StringBuilder builder = new StringBuilder(key);
        for (int i = 0; i < minLength; i++) {
            int ch1 = builder.charAt(i);
            int ch2 = password.charAt(i);
            if (ch1 >= 32 && ch1 <= 126) {
                int x = ch1 + ch2 - 32;
                if (x > 126) x -= 95;
                builder.setCharAt(i, (char) x);
            }
        }
        return builder.toString();
    }

    private static String decode(String password, String key) {
        if (password == null) return key;
        if (key == null) return password;

        int passwordLength = password.length();
        int keyLength = key.length();
        int minLength = passwordLength < keyLength ? passwordLength : keyLength;

        StringBuilder builder = new StringBuilder(key);
        for (int i = 0; i < minLength; i++) {
            int ch1 = builder.charAt(i);
            int ch2 = password.charAt(i);
            if (ch1 >= 32 && ch1 <= 126) {
                int x = 127 + ch1 - ch2;
                if (x > 126) x -= 95;
                builder.setCharAt(i, (char) x);
            }
        }
        return builder.toString();
    }

    // scramble only printable ASCII chars
    public static String decodeApiKey(String password, String code) {
        password = getPassword(password, code);
        return decode(password, code);
    }

    public static String encodeApiKey(String password, String key) {
        password = getPassword(password, key);
        return encode(password, key);
    }
}
