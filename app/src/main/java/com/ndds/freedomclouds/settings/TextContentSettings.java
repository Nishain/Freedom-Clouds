package com.ndds.freedomclouds.settings;


import android.view.ViewGroup;
import com.ndds.freedomclouds.R;
import com.ndds.freedomclouds.common.Message;

public class TextContentSettings extends SettingsPage {
    public final static String EVENT_TITLE = "eventTitle";
    public final static String EVENT_DESCRIPTION = "eventDescription";
    public TextContentSettings(Settings.Bundle bundle) {
        super(bundle.activity, bundle.sharedPreferences, R.layout.settings_text_content, "Text content", bundle.dialog);
    }

    private void onCreateOrUpdateEventTitle(VoidAwareTextWatcher textWatcher) {
        String eventTitle = textWatcher.inputString;
        if (eventTitle.length() < 5) {
            Message.show(activity, "title is too short!");
            return;
        }
        sharedPreferences.edit().putString(EVENT_TITLE, eventTitle).apply();
        textWatcher.updateValue(eventTitle);
        if (textWatcher.value == null)
            Message.show(activity, "Event title added");
        else Message.show(activity, "Event title updated");
        activity.onUpdateEventTitle(eventTitle);
    }

    private void onDeleteEventTitle(VoidAwareTextWatcher textWatcher) {
        textWatcher.clearValue();
        Message.show(activity, "Event title is removed");
        sharedPreferences.edit().remove(EVENT_TITLE).apply();
        activity.onUpdateEventTitle(null);
    }

    private void onCreateOrUpdateEventDescription(VoidAwareTextWatcher textWatcher) {
        String eventTitle = textWatcher.inputString;
        if (eventTitle.length() < 5) {
            Message.show(activity, "description is too short!");
            return;
        }
        sharedPreferences.edit().putString(EVENT_DESCRIPTION, eventTitle).apply();
        textWatcher.updateValue(eventTitle);
        if (textWatcher.value == null)
            Message.show(activity, "Description added");
        else Message.show(activity, "Description updated");
        activity.onUpdateEventTitle(eventTitle);
    }

    private void onDeleteEventDescription(VoidAwareTextWatcher textWatcher) {
        textWatcher.clearValue();
        Message.show(activity, "Description is removed");
        sharedPreferences.edit().remove(EVENT_DESCRIPTION).apply();
        activity.onUpdateEventTitle(null);
    }

    @Override
    void onCreate(ViewGroup settingsView) {
        String eventTitle = sharedPreferences.getString(EVENT_TITLE, null);
        String eventDescription = sharedPreferences.getString(EVENT_DESCRIPTION, null);
        VoidAwareTextWatcher titleTextWatcher = new VoidAwareTextWatcher(eventTitle, settingsView,
                R.id.eventTitleEdit, R.id.enableEventTitle, R.id.removeEventTitle,
                new String[] {
                        "Update title",
                        "Add title"
                });

        VoidAwareTextWatcher descriptionTextWatcher = new VoidAwareTextWatcher(eventDescription, settingsView,
                R.id.eventDescriptionEdit, R.id.enableEventDescription, R.id.removeEventDescription,
                new String[] {
                        "Update description",
                        "Add description"
                });

        setListener(R.id.enableEventTitle, this::onCreateOrUpdateEventTitle, titleTextWatcher);
        setListener(R.id.removeEventTitle, this::onDeleteEventTitle, titleTextWatcher);

        setListener(R.id.enableEventDescription, this::onCreateOrUpdateEventDescription, descriptionTextWatcher);
        setListener(R.id.removeEventDescription, this::onDeleteEventDescription, descriptionTextWatcher);
    }
}
