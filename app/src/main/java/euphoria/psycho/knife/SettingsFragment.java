package euphoria.psycho.knife;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    public static final String KEY_SSH_SERVER_ADDRESS = "ssh_server_address";
    public static final String KEY_SSH_USERNAME = "ssh_username";
    public static final String KEY_SSH_PASSWORD = "ssh_password";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences_layout);
    }


}