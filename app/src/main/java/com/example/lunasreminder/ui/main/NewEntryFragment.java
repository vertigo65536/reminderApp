package com.example.lunasreminder.ui.main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lunasreminder.SingleDBManager;
import com.example.lunasreminder.R;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewEntryFragment extends Fragment {
    private SingleDBManager singleDbManager;
    DatePickerDialog picker;
    Button btn;
    EditText name;
    EditText description;
    TextView date;
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
        singleDbManager.open();
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
                        drawCalendarEvent();
                        break;
                }
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
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
                clear();
                Snackbar mySnackbar = Snackbar.make(root, "Saved", 5000);
                mySnackbar.show();
            }
        });
        return root;
    }

    private void drawDailyReminder(){
        variableLayout.removeAllViews();
    }

    private void drawRepeating() {
        variableLayout.removeAllViews();
    }

    private void drawCalendarEvent() {
        TextView dateView = new TextView(getActivity());
        dateView.setId(R.id.date);
        dateView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        dateView.setText(df.format(new Date()));
        dateView.setTextSize(20);
        variableLayout.addView(dateView);
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
}