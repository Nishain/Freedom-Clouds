package com.ndds.freedomclouds.settings;
import static com.ndds.freedomclouds.PasscodeShield.PASSCODE;

import android.view.ViewGroup;

import com.ndds.freedomclouds.R;
import com.ndds.freedomclouds.common.Message;

class PasscodeSettings extends SettingsPage {
    public PasscodeSettings(Settings.Bundle bundle) {
        super(bundle.activity, bundle.sharedPreferences, R.layout.settings_passcode, "Security", bundle.dialog);
    }

    private void onCreateOrUpdatePasscode(VoidAwareTextWatcher textWatcher) {
        String newPasscode = textWatcher.inputString;
        if (newPasscode.length() < 4) {
            Message.show(activity, "Passcode code is too short");
            return;
        }

        sharedPreferences.edit().putString(PASSCODE, newPasscode).apply();
        Message.show(activity, "Passcode is now enabled");
        textWatcher.updateValue(newPasscode);
    }

    private void onRemovePasscode(VoidAwareTextWatcher textWatcher) {
        if(textWatcher.clearValue()) {
            Message.show(activity, "Passcode is disabled");
            sharedPreferences.edit().remove(PASSCODE).apply();
        }
    }

    @Override
    void onCreate(ViewGroup settingsView) {
        String passcode = sharedPreferences.getString(PASSCODE, null);
        VoidAwareTextWatcher stateWatcher = new VoidAwareTextWatcher(passcode, settingsView,
                R.id.passcode_editor, R.id.passcode_enable, R.id.passcode_disable,
                new String[] {
                "Update passcode",
                "Enable passcode"
        });

        setListener(R.id.passcode_enable, this::onCreateOrUpdatePasscode, stateWatcher);
        setListener(R.id.passcode_disable, this::onRemovePasscode, stateWatcher);
    }
}
