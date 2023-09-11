package com.ndds.freedomclouds.settings;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ndds.freedomclouds.common.ActionButton;
import com.ndds.freedomclouds.R;

public class VoidAwareTextWatcher implements TextWatcher {
    private boolean wasInDelete = false;
    private final String updateText, createText;
    private final EditText editText;
    public final ActionButton actionButton, deleteButton;
    public String value;
    public String inputString;

    VoidAwareTextWatcher(String initialValue, ViewGroup container, int editText, int actionButton, int deleteButton, String[] textLabels) {
        value = initialValue;
        inputString = initialValue;
        this.editText = container.findViewById(editText);
        updateText = textLabels[0];
        createText = textLabels[1];
        this.actionButton = container.findViewById(actionButton);
        this.deleteButton = container.findViewById(deleteButton);

        if (initialValue != null) {
            this.editText.setText(initialValue);
            this.actionButton.setText(updateText);
            this.actionButton.setColorTheme(R.color.beautyBlue);
        } else {
            this.actionButton.setText(createText);
            this.deleteButton.setVisibility(View.GONE);
        }
        this.editText.addTextChangedListener(this);
    }


    void updateValue(String newValue) {
        if (value == null) {
            deleteButton.setVisibility(View.VISIBLE);
            actionButton.setText(updateText);
            actionButton.setColorTheme(R.color.beautyBlue);
        }
        value = newValue;

    }

    boolean clearValue() {
        if (value != null) {
            actionButton.setVisibility(View.VISIBLE);
            actionButton.setText(createText);
            deleteButton.setVisibility(View.GONE);
            actionButton.setColorTheme(R.color.beautyGreen);
            value = null;
            if (!inputString.isEmpty()) {
                editText.setText("");
                inputString = "";
            }
            return true;
        }
        return false;
    }

    protected Boolean onChange(boolean wasInDelete) {
        if (inputString.length() == 0 && value != null) {
            actionButton.setVisibility(View.GONE);
            return true;
        } else if (wasInDelete) {
            if (value == null) {
                actionButton.setText(createText);
            } else {
                actionButton.setText(updateText);
            }
            actionButton.setVisibility(View.VISIBLE);
            return false;
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        inputString = s.toString();
        wasInDelete = onChange(wasInDelete);
    }
}
