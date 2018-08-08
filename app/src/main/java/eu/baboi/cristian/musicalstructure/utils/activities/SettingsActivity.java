package eu.baboi.cristian.musicalstructure.utils.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import eu.baboi.cristian.musicalstructure.R;
import eu.baboi.cristian.musicalstructure.utils.net.Model;
import eu.baboi.cristian.musicalstructure.utils.secret.DataStore;


public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }

    @Override
    public void onBackPressed() {
        //needed to restore full sensor orientation change for the main activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onBackPressed();
    }

    // the settings fragment
    public static class MusicalPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

        private Preference password;

        private DataStore dataStore;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            dataStore = new DataStore(getContext(), Model.PASSWORD);

            addPreferencesFromResource(R.xml.settings);

            password = findPreference(getString(R.string.password));
            bindPreferenceSummaryToValue(password);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            //newValue cannot be null
            String stringValue = newValue.toString().trim();
            if (preference == password) {
                return true;
            }
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else preference.setSummary(stringValue);
            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {

            preference.setPreferenceDataStore(dataStore);
            preference.setOnPreferenceChangeListener(this);

            // get clear text value
            String preferenceString = dataStore.getString(preference.getKey(), "");

            // update to clear text value
            if (preference instanceof EditTextPreference)
                ((EditTextPreference) preference).setText(preferenceString);
            else if (preference instanceof ListPreference)
                ((ListPreference) preference).setValue(preferenceString);

            //update summary
            onPreferenceChange(preference, preferenceString);
        }
    }

}
