package com.example.lunasreminder.ui.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lunasreminder.DailyDBManager;
import com.example.lunasreminder.DatabaseHelper;
import com.example.lunasreminder.RepeatDBManager;
import com.example.lunasreminder.SingleDBManager;
import com.example.lunasreminder.R;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment{
    final String[] daysOfWeek = new String[] {
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday"
    };
    private SingleDBManager singleDbManager;
    private RepeatDBManager repeatDbManager;
    private DailyDBManager dailyDbManager;
    private View root;
    private LinearLayout scrollLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    final private int dailyPrefix = 10000;
    final private int repeatPrefix = 20000;
    final private int calendarPrefix = 30000;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public static MainFragment newInstance(int index) {
        MainFragment fragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        singleDbManager = new SingleDBManager(getActivity());
        repeatDbManager = new RepeatDBManager(getActivity());
        dailyDbManager = new DailyDBManager(getActivity());
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
        root = inflater.inflate(R.layout.fragment_main, container, false);
        scrollLayout = (LinearLayout) root.findViewById(R.id.scrollLayout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.scrollView);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateList();
                mSwipeRefreshLayout.setRefreshing(Boolean.FALSE);
            }
        });
        populateList();
        return root;
    }

    @Override
    public void onResume() {
        populateList();
        super.onResume();
    }

    private void populateList() {
        dailyDbManager.open();
        Cursor cursor;
        scrollLayout.removeAllViews();
        LinkedList<Integer> dailyIds = new LinkedList<Integer>();
        createTitle("Daily Tasks");
        cursor = dailyDbManager.fetch();
        if (cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DAILY_NAME));
                String desc = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DAILY_DESC));
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DAILY_COMPLETED));
                int id = concatInt(dailyPrefix, cursor.getInt(cursor.getColumnIndex(DatabaseHelper.DAILY__ID)));
                //dailyCard(name, desc, date, id);
                drawCard(name, desc, date, id);
                dailyIds.add(id);
            }while(cursor.moveToNext());
        }
        cursor.close();
        dailyDbManager.close();
        repeatDbManager.open();
        LinkedList<Integer> repeatIds = new LinkedList<Integer>();
        createTitle("Today's Repeated Tasks");
        cursor = repeatDbManager.fetch();
        if (cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_NAME));
                String desc = cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_DESC));
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_DATE));
                String type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_TYPE));
                String completed = cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_COMPLETED));
                int id = concatInt(repeatPrefix, cursor.getInt(cursor.getColumnIndex(DatabaseHelper.REPEAT__ID)));
                int intervalSize = 1;
                if (today().equals(completed)) {
                    break;
                }
                if (type.equals("Monthly")) {
                    String[] completedSplit = completed.split("-");
                    YearMonth yearMonthObject = YearMonth.of(
                            Integer.parseInt(completedSplit[0]),
                            Integer.parseInt(completedSplit[1]));
                    intervalSize = yearMonthObject.lengthOfMonth()-1;
                    if ((daysBetween(completed, today()) > intervalSize) | (date.split("-")[2].equals(today().split("-")[2]))) {
                        drawCard(name, desc, date, id, completed, intervalSize);
                        repeatIds.add(id);
                    }
                } else if (type.equals("Weekly")) {
                    intervalSize = 6;
                    if ((daysBetween(completed, today()) > intervalSize) | (getDayOfWeek(today()).equals(getDayOfWeek(date)))) {
                        drawCard(name, desc, date, id, completed, intervalSize);
                        repeatIds.add(id);
                    }
                }
            }while(cursor.moveToNext());
        }
        cursor.close();
        repeatDbManager.close();
        singleDbManager.open();
        LinkedList<Integer> calendarIds = new LinkedList<Integer>();
        createTitle("Today's Scheduled Tasks");
        cursor = singleDbManager.fetch();
        if (cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SINGLE_NAME));
                String desc = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SINGLE_DESC));
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SINGLE_DATE));
                int id = concatInt(calendarPrefix, cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SINGLE__ID)));
                Boolean completed = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SINGLE_COMPLETED)) > 0;
                if (!isFuture(date)) {
                    if (completed == Boolean.FALSE) {
                        //calendarCard(name, desc, date, completed, id);
                        drawCard(name, desc, date, id, completed);
                        calendarIds.add(id);
                    }
                }

            }while(cursor.moveToNext());
        }
        createButton(dailyIds, repeatIds, calendarIds);
        cursor.close();
        singleDbManager.close();
    }

    private void createTitle(String titleText) {
        TextView title = new TextView(getActivity());
        title.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        title.setText(titleText);
        title.setTextSize(30);
        title.setPadding(10, 30, 10, 30);
        scrollLayout.addView(title);
    }

    private void drawCard(String name, String desc, String date, int id) {
        drawCard(name, desc, date, id, 0, null, 0);
    }

    private void drawCard(String name, String desc, String date, int id, String completed, int intervalSize) {
        drawCard(name, desc, date, id, 1, completed, intervalSize);
    }

    private void drawCard(String name, String desc, String date, int id, Boolean completed) {
        drawCard(name, desc, date, id, 2, completed.toString(), 0);
    }

    private void drawCard(String name, String desc, String date, int id, int type, String completed, int intervalSize) {
        CardView cardView = new CardView(getActivity());
        cardView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout cardLayout = new LinearLayout(getActivity());
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        Switch reminder = new Switch(getActivity());
        TextView reminderDescription = new TextView(getActivity());
        View reminderSeparator = new View(getActivity());
        reminder.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        reminderDescription.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        reminderSeparator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20));
        reminder.setTextColor(getResources().getColor(R.color.purple_200));
        reminder.setTextSize(25);
        reminder.setPadding(20,10,20,0);
        reminder.setContentDescription(desc);
        int days = daysSince(date);
        String titleText = name;
        String description = desc;
        switch (type) {
            case 0:
                String colour;
                if (days == 0) {
                    colour = "#00FF00";
                } else if (days > 0 && days < 4) {
                    colour = "#FFFF00";
                } else {
                    colour = "#FF0000";
                }
                description = description + "<br /><font color='" + colour + "'>" + days + " days since last done</font>";
                break;
            case 1:
                 int interval = daysBetween(completed, today());
                 if (interval > intervalSize) {
                     titleText = "<font color='#FF0000'>OVERDUE</font><br />" + titleText;
                 }
                break;
            case 2:
                if (isPast(date)) {
                    titleText = "<font color='#FF0000'>OVERDUE</font><br />" + titleText;
                }
                break;
        }
        reminder.setText(Html.fromHtml(titleText));
        reminderDescription.setText(Html.fromHtml(description));
        reminderDescription.setTextSize(20);
        reminderDescription.setPadding(20,0,20,10);
        reminder.setId(id);
        scrollLayout.addView(cardView);
        cardView.addView(cardLayout);
        cardLayout.addView(reminder);
        cardLayout.addView(reminderDescription);
        scrollLayout.addView(reminderSeparator);
        cardLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                vibrate(200);
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                switch (type) {
                                    case 0:
                                        dailyDbManager.open();
                                        dailyDbManager.delete(removePrefix(id));
                                        dailyDbManager.close();
                                         break;
                                    case 1:
                                        repeatDbManager.open();
                                        repeatDbManager.delete(removePrefix(id));
                                        repeatDbManager.close();
                                        break;
                                    case 2:
                                        singleDbManager.open();
                                        singleDbManager.delete(removePrefix(id));
                                        singleDbManager.close();
                                        break;

                                }
                                populateList();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Delete " + name + "?").setPositiveButton("Yes", dialogClickListener).show();
                return false;
            }
        });
    }

    public int daysBetween(String day1, String day2) {
        try {
            long dayStamp1 = dateFormat.parse(day1).getTime() / (24 * 60 * 60 * 1000);
            long dayStamp2 = dateFormat.parse(day2).getTime() / (24 * 60 * 60 * 1000);
            return (int)(dayStamp2 - dayStamp1);
        } catch (Exception e) {
            return -1;
        }
    }

    public int daysSince(String day) {
        long date;
        try {
            date = dateFormat.parse(day).getTime()/1000/60/60/24;
        } catch (Exception e) {
            return 0;
        }
        long today = new Date().getTime()/1000/60/60/24;
        return (int)(today - date);
    }

    public boolean isFuture(String date) {
        date = date.replace("-", "");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMd");
        String today = df.format(new Date());
        if (Integer.parseInt(date) - Integer.parseInt(today) > 0) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public boolean isPast(String date) {
        date = date.replace("-", "");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMd");
        String today = df.format(new Date());
        if (Integer.parseInt(today) - Integer.parseInt(date) > 0) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    private int concatInt(int prefix, int suffix) {
        return Integer.parseInt((Integer.toString(prefix) + Integer.toString(suffix)));
    }

    private int removePrefix(int number) {
        return Integer.parseInt(Integer.toString(number).substring(4));
    }

    private void createButton(LinkedList dailyIds, LinkedList repeatIds, LinkedList calendarIds) {
        Button btn = new Button(getActivity());
        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setText("Submit");
        btn.setBackgroundColor(getResources().getColor(R.color.purple_200));
        btn.setTextColor(Color.parseColor("#000000"));
        scrollLayout.addView(btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Switch reminder;
                dailyDbManager.open();
                for (int i = 0; i < dailyIds.size(); i++) {
                    int id = (int) dailyIds.get(i);
                    reminder = root.findViewById(id);
                    String today = today();
                    if (reminder.isChecked()) {
                        dailyDbManager.updateCompleted(removePrefix(id), today);
                    }
                }
                dailyDbManager.close();
                repeatDbManager.open();
                for (int i = 0; i < repeatIds.size(); i++) {
                    int id = (int) repeatIds.get(i);
                    reminder = root.findViewById(id);
                    if (reminder.isChecked()) {
                        repeatDbManager.updateCompleted(removePrefix(id), today());
                    }
                }
                repeatDbManager.close();
                singleDbManager.open();
                for (int i = 0; i < calendarIds.size(); i++) {
                    int id = (int) calendarIds.get(i);
                    reminder = root.findViewById(id);
                    if (reminder.isChecked()) {
                        singleDbManager.updateCompleted(removePrefix(id), Boolean.TRUE);
                    }
                }
                singleDbManager.close();
                populateList();
            }
        });
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                vibrate(5000);
                return true;
            }
        });
    }

    private void vibrate(int len) {
        Vibrator vib = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(len, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vib.vibrate(len);
        }
    }

    private String today() {
        return dateFormat.format(new Date());
    }

    private String getDayOfWeek(String date) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(dateFormat.parse(date));
        } catch (Exception e) {
            return "Send string in form yyyy-M-d";
        }
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return daysOfWeek[dayOfWeek-1];
    }

    private String getLastDayTimestamp(String date) {
        if (getDayOfWeek(today()).equals(getDayOfWeek(date))) {
            return today();
        } else {
            int todayDayKey = getDayIndex(getDayOfWeek(today()));
            int dateDayKey = getDayIndex(getDayOfWeek(date));
            int difference = todayDayKey - dateDayKey;
            if (difference <= 0) {
                difference = 7 + difference - 1;
            }
            try {
                return dateFormat.format(dateFormat.parse(today()).getTime() - (difference * 24 * 60 * 60 * 1000));
            } catch (Exception e) {
                return "";
            }
        }
    }

    private int getDayIndex(String day) {
        for (int i = 1; i<=daysOfWeek.length; i++) {
            if (daysOfWeek[i].equals(day)) {
                return i;
            }
        }
        return -1;
    }
}