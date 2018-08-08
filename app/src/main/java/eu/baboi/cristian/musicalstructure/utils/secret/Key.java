package eu.baboi.cristian.musicalstructure.utils.secret;

public class Key {

    // return the password concatenated to itself to the length of key
    private static String getPassword(String password, String key) {
        if (password == null || password.isEmpty()) return key;
        if (key == null || key.isEmpty()) return password;

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
    private static String combine(String password, String key) {
        if (password == null) return key;
        if (key == null) return password;
        int passwordLength = password.length();
        int keyLength = key.length();
        int minLength = passwordLength < keyLength ? passwordLength : keyLength;

        StringBuilder builder = new StringBuilder(key);
        for (int i = 0; i < minLength; i++) {
            int ch1 = builder.charAt(i) - 32;
            int ch2 = password.charAt(i) - 32;
            char ch = (char) ((ch1 ^ ch2) + 32);
            builder.setCharAt(i, ch);
        }
        return builder.toString();
    }

    // extract the api key from password and key
    public static String getApiKey(String password, String key) {
        password = getPassword(password, key);//extend or truncate password to the length of key
        return combine(password, key);
    }
}
