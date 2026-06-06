package com.example.lostandfound;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewReportsActivity extends AppCompatActivity {

    private View btnBackReports;
    private TextInputEditText etSearchReports;
    private TextView chipAll, chipLost, chipFound;
    private View cardLostBackpack, cardFoundKeychain, cardLostCharger, cardFoundBottle;
    private TextView tvEmptyState;

    private String selectedFilter = "All";

    private String[] itemNames = {
            "Black Backpack",
            "Silver Keychain",
            "Laptop Charger",
            "Water Bottle"
    };

    private String[] itemLocations = {
            "Library - Floor 2",
            "Main Cafeteria",
            "Computer Lab",
            "Sports Centre"
    };

    private String[] itemTypes = {
            "Lost",
            "Found",
            "Lost",
            "Found"
    };

    private String[] itemDates = {
            "01/06/2026",
            "01/06/2026",
            "31/05/2026",
            "30/05/2026"
    };

    private String[] itemCategories = {
            "Bag",
            "Keys",
            "Electronics",
            "Bottle"
    };

    private String[] itemDescriptions = {
            "Black Nike backpack with a laptop charger inside. Last seen near the library study area.",
            "Silver keychain found near the cafeteria seating area.",
            "Black laptop charger lost in the computer lab.",
            "Water bottle found at the sports centre reception area."
    };

    private String[] itemIcons = {
            "\uD83C\uDF92",
            "\uD83D\uDD11",
            "\uD83D\uDCBB",
            "\uD83D\uDCA7"
    };

    private View[] itemCards;
    private int reportCount = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reports);

        btnBackReports = findViewById(R.id.btnBackReports);
        etSearchReports = findViewById(R.id.etSearchReports);
        chipAll = findViewById(R.id.chipAll);
        chipLost = findViewById(R.id.chipLost);
        chipFound = findViewById(R.id.chipFound);
        cardLostBackpack = findViewById(R.id.cardLostBackpack);
        cardFoundKeychain = findViewById(R.id.cardFoundKeychain);
        cardLostCharger = findViewById(R.id.cardLostCharger);
        cardFoundBottle = findViewById(R.id.cardFoundBottle);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        itemCards = new View[]{
                cardLostBackpack,
                cardFoundKeychain,
                cardLostCharger,
                cardFoundBottle
        };

        setupBottomNav();

        if (btnBackReports != null) {
            btnBackReports.setOnClickListener(v -> finish());
        }

        chipAll.setOnClickListener(v -> {
            selectedFilter = "All";
            updateChipStyle();
            filterReports();
        });

        chipLost.setOnClickListener(v -> {
            selectedFilter = "Lost";
            updateChipStyle();
            filterReports();
        });

        chipFound.setOnClickListener(v -> {
            selectedFilter = "Found";
            updateChipStyle();
            filterReports();
        });

        etSearchReports.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReports();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        cardLostBackpack.setOnClickListener(v -> openItemDetails(
                "🎒",
                "Black Backpack",
                "Lost",
                "Library - Floor 2",
                "01/06/2026",
                "Bag",
                "Black Nike backpack with a laptop charger inside. Last seen near the library study area.",
                "Student Services",
                "lostandfound@university.ac.uk",
                "Available through university office"
        ));

        cardFoundKeychain.setOnClickListener(v -> openItemDetails(
                "🔑",
                "Silver Keychain",
                "Found",
                "Main Cafeteria",
                "01/06/2026",
                "Keys",
                "Silver keychain found near the cafeteria seating area.",
                "Finder / Student Services",
                "lostandfound@university.ac.uk",
                "Available through university office"
        ));

        cardLostCharger.setOnClickListener(v -> openItemDetails(
                "💻",
                "Laptop Charger",
                "Lost",
                "Computer Lab",
                "31/05/2026",
                "Electronics",
                "Black laptop charger lost in the computer lab.",
                "Student Services",
                "lostandfound@university.ac.uk",
                "Available through university office"
        ));

        cardFoundBottle.setOnClickListener(v -> openItemDetails(
                "💧",
                "Water Bottle",
                "Found",
                "Sports Centre",
                "30/05/2026",
                "Bottle",
                "Water bottle found at the sports centre reception area.",
                "Finder / Student Services",
                "lostandfound@university.ac.uk",
                "Available through university office"
        ));

        setupReportCardClicks();
        updateChipStyle();
        filterReports();
        loadReportsFromFirestore();
    }

    private void setupBottomNav() {
        BottomNavHelper.setup(this, BottomNavHelper.Tab.REPORTS);
    }

    private void setupReportCardClicks() {
        for (int i = 0; i < itemCards.length; i++) {
            final int index = i;
            itemCards[i].setOnClickListener(v -> openReportDetails(index));
        }
    }

    private void openReportDetails(int index) {
        if (index < 0 || index >= reportCount) {
            return;
        }

        openItemDetails(
                itemIcons[index],
                itemNames[index],
                itemTypes[index],
                itemLocations[index],
                itemDates[index],
                itemCategories[index],
                itemDescriptions[index],
                "Student Services",
                "lostandfound@university.ac.uk",
                "Available through university office"
        );
    }

    private void loadReportsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<ReportListItem> reports = new ArrayList<>();

        db.collection("lost_reports")
                .get()
                .addOnSuccessListener(lostSnapshot -> {
                    addSnapshotReports(reports, lostSnapshot, "Lost");
                    loadFoundReports(db, reports);
                })
                .addOnFailureListener(e -> loadFoundReports(db, reports));
    }

    private void loadFoundReports(FirebaseFirestore db, List<ReportListItem> reports) {
        db.collection("found_reports")
                .get()
                .addOnSuccessListener(foundSnapshot -> {
                    addSnapshotReports(reports, foundSnapshot, "Found");
                    if (!reports.isEmpty()) {
                        showFirestoreReports(reports);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!reports.isEmpty()) {
                        showFirestoreReports(reports);
                    }
                });
    }

    private void addSnapshotReports(List<ReportListItem> reports, QuerySnapshot snapshot, String fallbackStatus) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            if (reports.size() >= itemCards.length) {
                return;
            }

            String itemName = getDocumentString(document, "itemName", "");
            String location = getDocumentString(document, "location", "");
            if (itemName.isEmpty()) {
                continue;
            }

            String status = normalizeStatus(getDocumentString(document, "status", fallbackStatus));
            reports.add(new ReportListItem(
                    getIconForStatus(status),
                    itemName,
                    location,
                    status,
                    getDocumentString(document, "date", ""),
                    getDocumentString(document, "category", ""),
                    getDocumentString(document, "description", "")
            ));
        }
    }

    private void showFirestoreReports(List<ReportListItem> reports) {
        reportCount = Math.min(reports.size(), itemCards.length);

        for (int i = 0; i < itemCards.length; i++) {
            if (i >= reportCount) {
                itemCards[i].setVisibility(View.GONE);
                continue;
            }

            ReportListItem report = reports.get(i);
            itemIcons[i] = report.icon;
            itemNames[i] = report.itemName;
            itemLocations[i] = report.location;
            itemTypes[i] = report.status;
            itemDates[i] = report.date;
            itemCategories[i] = report.category;
            itemDescriptions[i] = report.description;
            updateReportCard(itemCards[i], report);
        }

        setupReportCardClicks();
        filterReports();
    }

    private void updateReportCard(View card, ReportListItem report) {
        List<TextView> textViews = new ArrayList<>();
        collectTextViews(card, textViews);

        if (textViews.size() > 1) {
            textViews.get(1).setText(report.itemName);
        }
        if (textViews.size() > 2) {
            textViews.get(2).setText(report.location);
        }
        if (textViews.size() > 3 && !report.date.isEmpty()) {
            textViews.get(3).setText(report.date);
        }
        if (textViews.size() > 4) {
            TextView statusView = textViews.get(4);
            statusView.setText(report.status);
            styleStatusView(statusView, report.status);
        }
    }

    private void collectTextViews(View view, List<TextView> textViews) {
        if (view instanceof TextView) {
            textViews.add((TextView) view);
            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                collectTextViews(group.getChildAt(i), textViews);
            }
        }
    }

    private void styleStatusView(TextView statusView, String status) {
        if ("Found".equalsIgnoreCase(status)) {
            statusView.setTextColor(Color.parseColor("#16A34A"));
            statusView.setBackground(makeRoundedBackground("#DCFCE7"));
        } else {
            statusView.setTextColor(Color.parseColor("#DC2626"));
            statusView.setBackground(makeRoundedBackground("#FEE2E2"));
        }
    }

    private String getDocumentString(DocumentSnapshot document, String field, String fallback) {
        String value = document.getString(field);
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String normalizeStatus(String status) {
        if ("Found".equalsIgnoreCase(status)) {
            return "Found";
        }
        return "Lost";
    }

    private String getIconForStatus(String status) {
        if ("Found".equalsIgnoreCase(status)) {
            return "\uD83D\uDD0E";
        }
        return "\uD83D\uDCE6";
    }

    private void filterReports() {
        String searchText = "";
        if (etSearchReports.getText() != null) {
            searchText = etSearchReports.getText().toString().trim().toLowerCase();
        }

        int visibleCount = 0;
        for (int i = 0; i < itemCards.length; i++) {
            if (i >= reportCount) {
                itemCards[i].setVisibility(View.GONE);
                continue;
            }

            boolean matchesType =
                    selectedFilter.equals("All") ||
                            itemTypes[i].equalsIgnoreCase(selectedFilter);

            boolean matchesSearch =
                    itemNames[i].toLowerCase().contains(searchText) ||
                            itemLocations[i].toLowerCase().contains(searchText) ||
                            itemTypes[i].toLowerCase().contains(searchText);

            if (matchesType && matchesSearch) {
                itemCards[i].setVisibility(View.VISIBLE);
                visibleCount++;
            } else {
                itemCards[i].setVisibility(View.GONE);
            }
        }

        if (visibleCount == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void updateChipStyle() {
        resetChip(chipAll);
        resetChip(chipLost);
        resetChip(chipFound);

        if (selectedFilter.equals("All")) {
            selectChip(chipAll);
        } else if (selectedFilter.equals("Lost")) {
            selectChip(chipLost);
        } else {
            selectChip(chipFound);
        }
    }

    private void selectChip(TextView chip) {
        chip.setTextColor(Color.WHITE);
        chip.setBackground(makeRoundedBackground("#2563EB"));
    }

    private void resetChip(TextView chip) {
        chip.setTextColor(Color.parseColor("#2563EB"));
        chip.setBackground(makeRoundedBackground("#DBEAFE"));
    }

    private GradientDrawable makeRoundedBackground(String color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(50);
        return drawable;
    }

    private void openItemDetails(
            String icon,
            String title,
            String status,
            String location,
            String date,
            String category,
            String description,
            String contactName,
            String contactEmail,
            String contactPhone
    ) {
        Intent intent = new Intent(ViewReportsActivity.this, ItemDetailsActivity.class);
        intent.putExtra("icon", icon);
        intent.putExtra("title", title);
        intent.putExtra("status", status);
        intent.putExtra("location", location);
        intent.putExtra("date", date);
        intent.putExtra("category", category);
        intent.putExtra("description", description);
        intent.putExtra("contactName", contactName);
        intent.putExtra("contactEmail", contactEmail);
        intent.putExtra("contactPhone", contactPhone);
        startActivity(intent);
    }

    private static class ReportListItem {
        final String icon;
        final String itemName;
        final String location;
        final String status;
        final String date;
        final String category;
        final String description;

        ReportListItem(
                String icon,
                String itemName,
                String location,
                String status,
                String date,
                String category,
                String description
        ) {
            this.icon = icon;
            this.itemName = itemName;
            this.location = location;
            this.status = status;
            this.date = date;
            this.category = category;
            this.description = description;
        }
    }
}
