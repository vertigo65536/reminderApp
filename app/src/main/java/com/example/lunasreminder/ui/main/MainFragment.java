package com.example.lunasreminder.ui.main;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lunasreminder.SingleDBManager;
import com.example.lunasreminder.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment{
    private SingleDBManager singleDbManager;
    private View root;
    private LinearLayout scrollLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
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
        singleDbManager.open();
        Cursor cursor = singleDbManager.fetch();
        scrollLayout.removeAllViews();
        Switch reminder;
        CardView cardView;
        TextView reminderDescription;
        View reminderSeparator;
        LinearLayout cardLayout;
        int i = 0;
        if (cursor.moveToFirst()){
            do{
                cardView = new CardView(getActivity());
                cardView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                cardLayout = new LinearLayout(getActivity());
                cardLayout.setOrientation(LinearLayout.VERTICAL);
                cardLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                reminder = new Switch(getActivity());
                reminderDescription = new TextView(getActivity());
                reminderSeparator = new View(getActivity());
                reminder.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                reminderDescription.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                reminderSeparator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20));
                //reminderSeparator.setBackgroundColor(Color.parseColor("#C0C0C0"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String desc = cursor.getString(cursor.getColumnIndex("description"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                Boolean completed = cursor.getInt(cursor.getColumnIndex("completed")) > 0;
                reminder.setText(name);
                reminder.setTextColor(getResources().getColor(R.color.purple_200));
                reminder.setTextSize(25);
                reminder.setPadding(20,10,20,0);
                reminder.setContentDescription(desc);
                reminderDescription.setText(desc);
                reminderDescription.setTextSize(20);
                reminderDescription.setPadding(20,0,20,10);
                reminder.setId(i);
                reminder.setChecked(completed);
                /*scrollLayout.addView(reminder);
                scrollLayout.addView(reminderDescription);
                scrollLayout.addView(reminderSeparator);*/
                scrollLayout.addView(cardView);
                cardView.addView(cardLayout);
                cardLayout.addView(reminder);
                cardLayout.addView(reminderDescription);
                scrollLayout.addView(reminderSeparator);

            }while(cursor.moveToNext());
        }
        cursor.close();
        singleDbManager.close();
    }
}