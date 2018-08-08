package eu.baboi.cristian.musicalstructure.utils.secret;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceDataStore;

import java.util.HashSet;
import java.util.Set;

// encrypted data store
public class DataStore extends PreferenceDataStore {
    private final SharedPreferences sharedPreferences;
    private final String password;

    public DataStore(Context context, String password) {
        this.password = password;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void putString(String key, @Nullable String value) {
        if (value == null) {
            sharedPreferences.edit().remove(key).apply();
            return;
        }
        String newValue = Key.getApiKey(Key.getApiKey(key, password), value);
        sharedPreferences.edit().putString(key, newValue).apply();
    }

    public void putStringSet(String key, @Nullable Set<String> values) {
        if (values == null) {
            sharedPreferences.edit().remove(key).apply();
            return;
        }
        Set<String> newValues = new HashSet<>();
        String newPassword = Key.getApiKey(key, password);
        for (String value : values)
            newValues.add(Key.getApiKey(newPassword, value));
        sharedPreferences.edit().putStringSet(key, newValues).apply();
    }

    public void putInt(String key, int value) {
        String newValue = Key.getApiKey(Key.getApiKey(key, password), String.valueOf(value));
        sharedPreferences.edit().putString(key, newValue).apply();
    }

    public void putLong(String key, long value) {
        String newValue = Key.getApiKey(Key.getApiKey(key, password), String.valueOf(value));
        sharedPreferences.edit().putString(key, newValue).apply();
    }


    public void putFloat(String key, float value) {
        String newValue = Key.getApiKey(Key.getApiKey(key, password), String.valueOf(value));
        sharedPreferences.edit().putString(key, newValue).apply();
    }

    public void putBoolean(String key, boolean value) {
        String newValue = Key.getApiKey(Key.getApiKey(key, password), String.valueOf(value));
        sharedPreferences.edit().putString(key, newValue).apply();
    }

    //get
    @Nullable
    public String getString(String key, @Nullable String defValue) {
        String value = sharedPreferences.getString(key, null);
        return value == null ? defValue : Key.getApiKey(Key.getApiKey(key, password), value);
    }


    @Nullable
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        Set<String> values = sharedPreferences.getStringSet(key, null);
        if (values == null) return defValues;
        String newPassword = Key.getApiKey(key, password);
        Set<String> newValues = new HashSet<>();
        for (String value : values)
            newValues.add(Key.getApiKey(newPassword, value));
        return newValues;
    }

    public int getInt(String key, int defValue) {
        String value = sharedPreferences.getString(key, null);
        return value == null ? defValue : Integer.valueOf(Key.getApiKey(Key.getApiKey(key, password), value));
    }


    public long getLong(String key, long defValue) {
        String value = sharedPreferences.getString(key, null);
        return value == null ? defValue : Long.valueOf(Key.getApiKey(Key.getApiKey(key, password), value));
    }

    public float getFloat(String key, float defValue) {
        String value = sharedPreferences.getString(key, null);
        return value == null ? defValue : Float.valueOf(Key.getApiKey(Key.getApiKey(key, password), value));
    }


    public boolean getBoolean(String key, boolean defValue) {
        String value = sharedPreferences.getString(key, null);
        return value == null ? defValue : Boolean.valueOf(Key.getApiKey(Key.getApiKey(key, password), value));
    }
}
