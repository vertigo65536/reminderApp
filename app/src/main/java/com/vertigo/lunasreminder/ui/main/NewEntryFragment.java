package com.vertigo.lunasreminder.ui.main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.vertigo.lunasreminder.DailyDBManager;
import com.vertigo.lunasreminder.RepeatDBManager;
import com.vertigo.lunasreminder.SingleDBManager;
import com.vertigo.lunasreminder.R;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewEntryFragment extends Fragment {
    private static final String TAG = "lunasreminder.NewEntryFragment";
    private SingleDBManager singleDbManager;
    private DailyDBManager dailyDbManager;
    private RepeatDBManager repeatDbManager;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d");
    Spinner dropdown;
    Spinner dropdownDay;
    LinearLayout dropdownLayout;
    DatePickerDialog picker;
    Button btn;
    EditText name;
    EditText description;
    TextView dateView;
    RadioGroup radioGroup;
    View root;
    LinearLayout variableLayout;
    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public static NewEntryFragment newInstance(int index) {
        NewEntryFragment fragment = new NewEntryFragment();
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
        super.onCreate(savedInstanceState);
        singleDbManager = new SingleDBManager(getActivity());
        dailyDbManager = new DailyDBManager(getActivity());
        repeatDbManager = new RepeatDBManager(getActivity());
        root = inflater.inflate(R.layout.fragment_newentry, container, false);

        btn = (Button) root.findViewById(R.id.button);
        name = (EditText) root.findViewById(R.id.editTextName);
        description = (EditText) root.findViewById(R.id.editTextDescription);
        radioGroup = (RadioGroup) root.findViewById(R.id.radioGroup);
        variableLayout = (LinearLayout) root.findViewById(R.id.variableLayout);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.radioButton1:
                        drawDailyReminder();
                        break;
                    case R.id.radioButton2:
                        drawRepeating();
                        break;
                    case R.id.radioButton3:
                        variableLayout.removeAllViews();
                        drawCalendarEvent();
                        break;
                }
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int checkedId = radioGroup.getCheckedRadioButtonId();
                Boolean success = Boolean.FALSE;
                switch(checkedId) {
                    case R.id.radioButton1:
                        success = insertDailyEntry();
                        break;
                    case R.id.radioButton2:
                        success = insertRepeatEntry();
                        break;
                    case R.id.radioButton3:
                        success = insertSingleEntry();
                        break;
                }
                Snackbar mySnackbar;
                if (success == Boolean.TRUE) {
                    mySnackbar = Snackbar.make(root, "Saved", 5000);
                    clear();
                } else {
                    mySnackbar = Snackbar.make(root, "You have left some empty forms", 5000);
                }
                mySnackbar.show();
            }
        });
        return root;
    }

    private Boolean insertDailyEntry() {
        try {
            dailyDbManager.open();
            final String nameStr = name.getText().toString();
            final String descStr = description.getText().toString();
            final String dateStr = today();
            dailyDbManager.insert(
                    nameStr,
                    descStr,
                    dateStr
            );
            dailyDbManager.close();
            return Boolean.TRUE;
        } catch (Exception e) {
            Log.d(TAG, e.getStackTrace().toString());
            return Boolean.FALSE;
        }
    }

    private Boolean insertRepeatEntry() {
        try {
            repeatDbManager.open();
            final String nameStr = name.getText().toString();
            final String descStr = description.getText().toString();
            final TextView date = variableLayout.findViewById(R.id.date);
            String dateStr;
            try {
                dateStr = date.getText().toString();
            } catch (Exception e) {
                dateStr = "";
            }
            final String type = dropdown.getSelectedItem().toString();

            repeatDbManager.insert(
                    nameStr,
                    descStr,
                    type,
                    dateStr,
                    dateFormat.format(new Date().getTime() - Long.parseLong("86400000"))
            );

            repeatDbManager.close();
            return Boolean.TRUE;
        } catch (Exception e) {
            Log.d(TAG, e.getStackTrace().toString());
            return Boolean.FALSE;
        }
    }

    private Boolean insertSingleEntry() {
        try {
            singleDbManager.open();
            final TextView date = (TextView) root.findViewById(R.id.date);
            final String nameStr = name.getText().toString();
            final String descStr = description.getText().toString();
            final String dateStr = date.getText().toString();

            singleDbManager.insert(
                    nameStr,
                    descStr,
                    dateStr,
                    Boolean.FALSE
            );
            singleDbManager.close();
            return Boolean.TRUE;
        } catch (Exception e) {
            Log.d(TAG, e.getStackTrace().toString());
            return Boolean.FALSE;
        }
    }

    private void drawDailyReminder(){
        variableLayout.removeAllViews();
    }

    private void drawRepeating() {
        variableLayout.removeAllViews();
        drawCalendarEvent();
        dropdown = new Spinner(getActivity());
        dropdown.setId((int)69);
        //String[] values = new String[] {"Weekly","Biweekly","Monthly"};
        String[] values = new String[] {"Weekly", "Monthly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            getActivity(),
            android.R.layout.simple_spinner_dropdown_item,
            values
        );
        dropdown.setAdapter(adapter);
        dropdown.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        variableLayout.addView(dropdown);
        dropdownLayout = new LinearLayout(getActivity());
        dropdownLayout.setOrientation(LinearLayout.VERTICAL);
        dropdownLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        variableLayout.addView(dropdownLayout);
        TextView newTextView = new TextView(getActivity());
        newTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        dropdownLayout.addView(newTextView);
    }

    private void drawDayView() {
        dropdownDay = new Spinner(getActivity());
        dropdownDay.setId((int)420);
        String[] values = new String[]{
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                values
        );
        dropdownDay.setAdapter(adapter);
        dropdownLayout.addView(dropdownDay);
    }

    private void drawCalendarEvent() {
        TextView dateView = new TextView(getActivity());
        dateView.setId(R.id.date);
        dateView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        dateView.setText(today());
        dateView.setTextSize(25);
        dateView.setPadding(30, 20, 30, 20);
        variableLayout.addView(dateView);
        variableLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                dateView.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
    }

    private void clear()
    {
        if (name != null) name.setText("");
        if (description != null) description.setText("");
        if (dateView != null) dateView.setText("");
    }

    private String today () {
        final String dateStr = dateFormat.format(new Date());
        return dateStr;
    }
}