package com.example.lunasreminder.ui.main;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.RingtoneManager;
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
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lunasreminder.DailyDBManager;
import com.example.lunasreminder.MainActivity;
import com.example.lunasreminder.SingleDBManager;
import com.example.lunasreminder.R;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment{
    private SingleDBManager singleDbManager;
    private DailyDBManager dailyDbManager;
    private View root;
    private LinearLayout scrollLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int dailyPrefix = 10000;
    private int repeatPrefix = 20000;
    private int calendarPrefix = 30000;
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
        singleDbManager = new SingleDBManager(getActivity());
        dailyDbManager = new DailyDBManager(getActivity());
        dailyDbManager.open();
        Cursor cursor;
        scrollLayout.removeAllViews();
        LinkedList<Integer> dailyIds = new LinkedList<Integer>();
        createTitle("Daily Tasks");
        cursor = dailyDbManager.fetch();
        if (cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String desc = cursor.getString(cursor.getColumnIndex("description"));
                String date = cursor.getString(cursor.getColumnIndex("lastCompleted"));
                int id = concatInt(dailyPrefix, cursor.getInt(cursor.getColumnIndex("_id")));
                dailyCard(name, desc, date, id);
                dailyIds.add(id);
            }while(cursor.moveToNext());
        }
        cursor.close();
        dailyDbManager.close();
        singleDbManager.open();
        LinkedList<Integer> calendarIds = new LinkedList<Integer>();
        createTitle("Today's Scheduled Tasks");
        cursor = singleDbManager.fetch();
        if (cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String desc = cursor.getString(cursor.getColumnIndex("description"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                int id = concatInt(calendarPrefix, cursor.getInt(cursor.getColumnIndex("_id")));
                Boolean completed = cursor.getInt(cursor.getColumnIndex("completed")) > 0;
                if (!isFuture(date)) {
                    if (completed == Boolean.FALSE) {
                        calendarCard(name, desc, date, completed, id);
                        calendarIds.add(id);
                    }
                }

            }while(cursor.moveToNext());
        }
        createButton(dailyIds, calendarIds);
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

    private void dailyCard(String name, String desc, String date, int id) {
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
        reminder.setText(name);
        reminder.setTextColor(getResources().getColor(R.color.purple_200));
        reminder.setTextSize(25);
        reminder.setPadding(20,10,20,0);
        reminder.setContentDescription(desc);
        int days = daysSince(date);
        String description = desc;
        String colour;
        if (days == 0) {
            colour = "#00FF00";
        } else if (days > 0 && days < 4) {
            colour = "#FFFF00";
        } else {
            colour = "#FF0000";
        }
        description = description + "<br /><font color='" + colour + "'>" + days + " days since last done</font>";
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
                                dailyDbManager.open();
                                dailyDbManager.delete(removePrefix(id));
                                dailyDbManager.close();
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

    private void calendarCard(String name, String desc, String date, Boolean completed, int id) {
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
        String titleText = name;
        if (isPast(date)) {
            titleText = "<font color='#FF0000'>OVERDUE</font><br />" + titleText;
        }
        reminder.setText(Html.fromHtml(titleText));
        reminder.setTextColor(getResources().getColor(R.color.purple_200));
        reminder.setTextSize(25);
        reminder.setPadding(20,10,20,0);
        reminder.setContentDescription(desc);
        reminderDescription.setText(desc);
        reminderDescription.setTextSize(20);
        reminderDescription.setPadding(20,0,20,10);
        reminder.setId(id);
        reminder.setChecked(completed);
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
                                singleDbManager.open();
                                singleDbManager.delete(removePrefix(id));
                                singleDbManager.close();
                                populateList();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
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

    public int daysSince(String day) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
        long date;
        try {
            date = formatter.parse(day).getTime()/1000/60/60/24;
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

    private void createButton(LinkedList dailyIds, LinkedList calendarIds) {
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
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d");
                    String today = df.format(new Date());
                    if (reminder.isChecked()) {
                        dailyDbManager.updateCompleted(removePrefix(id), today);
                    }
                }
                dailyDbManager.close();
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
    }

    private void vibrate(int len) {
        Vibrator vib = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(len, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vib.vibrate(len);
        }
    }
}