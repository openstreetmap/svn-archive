package org.osmu.gosmore;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class Preferences extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setPreferenceScreen(createPreferenceHierarchy());
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
/*        PreferenceCategory inlinePrefCat = new PreferenceCategory(this);
        inlinePrefCat.setTitle(R.string.inline_preferences);
        root.addPreference(inlinePrefCat);*/
        
        CheckBoxPreference fastest = new CheckBoxPreference(this);
        fastest.setDefaultValue(true);
        fastest.setKey("fastest");
        fastest.setTitle(R.string.fastest);
        fastest.setSummary(R.string.summary_fastest);
        root.addPreference(fastest);

        CheckBoxPreference north2d = new CheckBoxPreference(this);
        north2d.setKey("north2d");
        north2d.setTitle(R.string.north2d);
        north2d.setSummary(R.string.summary_north2d);
        root.addPreference(north2d);
 
        ListPreference vehicle = new ListPreference(this);
        vehicle.setEntries(R.array.vehicle_entries);
        vehicle.setEntryValues(R.array.vehicle_values);
        vehicle.setDialogTitle(R.string.vehicle);
        vehicle.setKey("vehicle");
        vehicle.setTitle(R.string.vehicle);
        vehicle.setSummary(R.string.summary_vehicle);
        /*dialogBasedPrefCat*/ root.addPreference(vehicle);

        ListPreference ttsLocale = new ListPreference(this);
        ttsLocale.setEntries(R.array.tts_entries);
        ttsLocale.setEntryValues(R.array.tts_values);
        ttsLocale.setDialogTitle(R.string.tts);
        ttsLocale.setKey("tts");
        ttsLocale.setTitle(R.string.tts);
        ttsLocale.setSummary(R.string.summary_tts);
        /*dialogBasedPrefCat*/ root.addPreference(ttsLocale);
        return root;
    }
}