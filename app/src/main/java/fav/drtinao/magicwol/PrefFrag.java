package fav.drtinao.magicwol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * This Fragment allows user to pick preferences.
 */
public class PrefFrag extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs, rootKey);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.pref_frag_title);

        //ping timeout stuff setup - START
        ListPreference pingTimeoutPref = findPreference("ping_timeout_pref");
        pingTimeoutPref.setOnPreferenceChangeListener((preference, newValue) -> { //timeout preference changed by user, apply
            DeviceInfoLogic.pingTimeoutMS = Integer.parseInt(newValue.toString());
            return true;
        });
        if(pingTimeoutPref.getValue() == null){
            pingTimeoutPref.setValueIndex(3); //5000 ms default
        }
        //ping timeout stuff setup - END

        //default fragment stuff setup - START
        ListPreference defFragPref = findPreference("def_frag_pref");
        if(defFragPref.getValue() == null){
            defFragPref.setValueIndex(1); //show add device by default
        }
        //default fragment stuff setup - END
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
