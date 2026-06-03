package com.example.lostandfound;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class BottomNavHelper {

    public enum Tab {
        HOME,
        REPORTS,
        MESSAGES,
        PROFILE
    }

    private BottomNavHelper() {
    }

    public static void setup(AppCompatActivity activity, Tab currentTab) {
        resetTabs(activity);
        selectTab(activity, currentTab);

        bindTab(activity, R.id.navHome, Tab.HOME, currentTab, MainActivity.class);
        bindTab(activity, R.id.navReports, Tab.REPORTS, currentTab, ViewReportsActivity.class);
        bindTab(activity, R.id.navMessages, Tab.MESSAGES, currentTab, MessagesActivity.class);
        bindTab(activity, R.id.navProfile, Tab.PROFILE, currentTab, ProfileActivity.class);

        ImageButton fabReport = activity.findViewById(R.id.fabReport);
        if (fabReport != null) {
            fabReport.setOnClickListener(v -> showReportDialog(activity));
        }
    }

    private static void bindTab(
            AppCompatActivity activity,
            int navId,
            Tab tab,
            Tab currentTab,
            Class<?> targetActivity
    ) {
        View nav = activity.findViewById(navId);
        if (nav == null) {
            return;
        }

        nav.setSelected(tab == currentTab);
        nav.setOnClickListener(v -> {
            if (tab == currentTab) {
                return;
            }

            Intent intent = new Intent(activity, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
        });
    }

    private static void resetTabs(AppCompatActivity activity) {
        resetTab(activity, R.id.navHomePill, R.id.navHomeIcon);
        resetTab(activity, R.id.navReportsPill, R.id.navReportsIcon);
        resetTab(activity, R.id.navMessagesPill, R.id.navMessagesIcon);
        resetTab(activity, R.id.navProfilePill, R.id.navProfileIcon);
    }

    private static void resetTab(AppCompatActivity activity, int pillId, int iconId) {
        LinearLayout pill = activity.findViewById(pillId);
        ImageView icon = activity.findViewById(iconId);
        int unselectedColor = ContextCompat.getColor(activity, R.color.bottom_nav_unselected);

        if (pill != null) {
            pill.setBackground(null);
        }
        if (icon != null) {
            icon.setColorFilter(unselectedColor);
            icon.setScaleX(1f);
            icon.setScaleY(1f);
        }
    }

    private static void selectTab(AppCompatActivity activity, Tab currentTab) {
        switch (currentTab) {
            case HOME:
                selectTab(activity, R.id.navHomePill, R.id.navHomeIcon);
                break;
            case REPORTS:
                selectTab(activity, R.id.navReportsPill, R.id.navReportsIcon);
                break;
            case MESSAGES:
                selectTab(activity, R.id.navMessagesPill, R.id.navMessagesIcon);
                break;
            case PROFILE:
                selectTab(activity, R.id.navProfilePill, R.id.navProfileIcon);
                break;
        }
    }

    private static void selectTab(AppCompatActivity activity, int pillId, int iconId) {
        LinearLayout pill = activity.findViewById(pillId);
        ImageView icon = activity.findViewById(iconId);
        int selectedColor = ContextCompat.getColor(activity, R.color.bottom_nav_selected);

        if (pill != null) {
            pill.setBackgroundResource(R.drawable.bg_nav_pill_selected);
        }
        if (icon != null) {
            icon.setColorFilter(selectedColor);
            icon.setScaleX(1.08f);
            icon.setScaleY(1.08f);
        }
    }

    private static void showReportDialog(AppCompatActivity activity) {
        String[] actions = {"Report Lost", "Report Found"};
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Create report")
                .setItems(actions, (dialog, which) -> {
                    Class<?> target = which == 0 ? ReportLostActivity.class : ReportFoundActivity.class;
                    activity.startActivity(new Intent(activity, target));
                })
                .show();
    }
}
