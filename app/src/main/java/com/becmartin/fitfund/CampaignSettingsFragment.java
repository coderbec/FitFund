package com.becmartin.fitfund;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Bec Martin.
 */
public class CampaignSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
