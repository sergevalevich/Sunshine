package com.valevich.sunshine.ui.fragments;

import android.os.Bundle;

import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.valevich.sunshine.R;


public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {


    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general); //adds default preferences to sharedPrefs
        bindSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindSummaryToValue(findPreference(getString(R.string.pref_units_key)));
    }

    private void bindSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference, PreferenceManager // immediately set summary when entering fragment not waiting for the preference change
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(),""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String value = o.toString(); //check value in listpreference
        if(preference instanceof ListPreference) {

            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if(prefIndex >= 0) listPreference.setSummary(listPreference.getEntries()[prefIndex]);

        } else {
            preference.setSummary(value);
        }
        return true;
    }
}
