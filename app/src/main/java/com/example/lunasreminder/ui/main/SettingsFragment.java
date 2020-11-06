package com.example.lunasreminder.ui.main;

import android.app.TimePickerDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.lunasreminder.DatabaseHelper;
import com.example.lunasreminder.R;
import com.example.lunasreminder.SettingsDBManager;
import com.example.lunasreminder.Util;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

public class SettingsFragment extends Fragment {
    SettingsDBManager settingsDbManager;
    TimePickerDialog picker;
    private View root;
    private LinearLayout scrollLayout;
    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public static SettingsFragment newInstance(int index) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.settings_main, container, false);
        scrollLayout = (LinearLayout) root.findViewById(R.id.scrollLayout);
        populateList();
        return root;
    }

    private void populateList() {
        settingsDbManager = new SettingsDBManager(getActivity());
        Cursor cursor;
        scrollLayout.removeAllViews();
        settingsDbManager.open();
        LinkedList<String> ids = new LinkedList<String>();
        cursor = settingsDbManager.fetch();
        createCard(
                "Temp Disable",
                "Stop notifications until next start",
                "false",
                69,
                "bool");
        if (cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SETTINGS_NAME));
                String value = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SETTINGS_VALUE));
                String type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SETTINGS_TYPE));
                String desc = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SETTINGS_DESC));
                int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SETTINGS__ID));
                createCard(name, desc, value, id, type);
                ids.add(id + ":" + type);

            }while(cursor.moveToNext());
        }
        createButton(ids);
        cursor.close();
        settingsDbManager.close();
    }

    private void createButton(LinkedList ids) {
        Button btn = new Button(getActivity());
        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setText("Submit");
        btn.setBackgroundColor(getResources().getColor(R.color.purple_200));
        btn.setTextColor(Color.parseColor("#000000"));
        scrollLayout.addView(btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Snackbar snackbar;
                try {
                    String value = null;
                    settingsDbManager.open();
                    for (int i = 0; i < ids.size(); i++) {
                        String idsString = (String) (ids.get(i));
                        int id = Integer.parseInt(idsString.split(":")[0]);
                        String type = idsString.split(":")[1];
                        switch (type) {
                            case "bool":
                                Switch settingSwitch = root.findViewById(id);
                                if (settingSwitch.isChecked()) {
                                    value = "true";
                                } else {
                                    value = "false";
                                }
                                break;
                            case "time":
                                TextView settingTime = root.findViewById(id);
                                value = settingTime.getText().toString();
                                break;
                            case "timer":
                                EditText settingTimer = root.findViewById(id);
                                value = settingTimer.getText().toString();
                                if (value.isEmpty()) {
                                    value = "1";
                                }
                                value = Integer.toString((Integer.parseInt(value) * 60000));
                                break;
                        }
                        Snackbar mySnackbar;
                        if (value != null) {
                            settingsDbManager.updateValue(id, value);
                        }
                    }
                    settingsDbManager.close();
                    Switch tempDisable = root.findViewById((int) 69);
                    long nextAlarm;
                    if (tempDisable.isChecked()) {
                        nextAlarm = Util.setToNextStart(getActivity());
                    } else {
                        Util.cancelJobs(getActivity());
                        nextAlarm = Util.scheduleJob(getActivity());
                    }
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                    String notificationMessage;
                    if (nextAlarm == -1) {
                        notificationMessage = "Notifications disabled.";
                    } else {
                        notificationMessage = "Next notification at " + df.format(nextAlarm);
                    }
                    snackbar = Snackbar.make(root, "Settings updated. " + notificationMessage, 5000);
                } catch (Exception e) {
                    snackbar = Snackbar.make(root, "An error occurred", 5000);
                }
                snackbar.show();
            }
        });
    }

    private void createCard(String name, String desc, String value, int id, String type) {
        CardView cardView = new CardView(getActivity());
        cardView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout cardLayout = new LinearLayout(getActivity());
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        scrollLayout.addView(cardView);
        cardView.addView(cardLayout);
        View settingSeparator = new View(getActivity());
        settingSeparator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20));
        Switch settingSwitch;
        switch (type) {
            case "bool":
                settingSwitch = new Switch(getActivity());
                settingSwitch.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                settingSwitch.setText(name);
                settingSwitch.setTextColor(getResources().getColor(R.color.purple_200));
                settingSwitch.setTextSize(25);
                settingSwitch.setPadding(20, 10, 20, 0);
                settingSwitch.setChecked(Boolean.parseBoolean(value));
                settingSwitch.setId(id);
                cardLayout.addView(settingSwitch);
                break;
            case "time":
                TextView settingTime = new TextView(getActivity());
                settingTime.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                settingTime.setPadding(20, 10, 20, 0);
                settingTime.setText(value);
                settingTime.setTextColor(getResources().getColor(R.color.purple_200));
                settingTime.setTextSize(25);
                settingTime.setHint(name);
                settingTime.setId(id);
                cardLayout.addView(settingTime);
                settingTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        final Calendar cldr = Calendar.getInstance();
                        int hour = Integer.parseInt(value.split(":")[0]);
                        int minute = Integer.parseInt(value.split(":")[1]);
                        TimePickerDialog picker;
                        picker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                settingTime.setText( String.format("%02d" , selectedHour) + ":" + String.format("%02d" , selectedMinute));
                            }
                        }, hour, minute, true);
                        picker.show();
                    }
                });
                break;
            case "timer":
                EditText settingTimer = new EditText(getActivity());
                settingTimer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                settingTimer.setInputType(InputType.TYPE_CLASS_NUMBER);
                settingTimer.setTextColor(getResources().getColor(R.color.purple_200));
                settingTimer.setTextSize(25);
                settingTimer.setText(Integer.toString(Integer.parseInt(value)/60000));
                settingTimer.setHint(name);
                settingTimer.setId(id);
                cardLayout.addView(settingTimer);
                break;

        }
        TextView settingDescription = new TextView(getActivity());
        settingDescription.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        settingDescription.setText(Html.fromHtml(desc));
        settingDescription.setTextSize(20);
        settingDescription.setPadding(20,0,20,10);
        cardLayout.addView(settingDescription);
        scrollLayout.addView(settingSeparator);
    }
}
