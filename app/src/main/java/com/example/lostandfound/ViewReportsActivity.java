package com.example.lostandfound;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ViewReportsActivity extends AppCompatActivity {

    View btnBackReports;

    TextInputEditText etSearchReports;

    TextView chipAll, chipLost, chipFound;

    View cardLostBackpack, cardFoundKeychain, cardLostCharger, cardFoundBottle;
    TextView tvEmptyState;

    String selectedFilter = "All";

    String[] itemNames = {
            "Black Backpack",
            "Silver Keychain",
            "Laptop Charger",
            "Water Bottle"
    };

    String[] itemLocations = {
            "Library - Floor 2",
            "Main Cafeteria",
            "Computer Lab",
            "Sports Centre"
    };

    String[] itemTypes = {
            "Lost",
            "Found",
            "Lost",
            "Found"
    };

    View[] itemCards;

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

        btnBackReports.setOnClickListener(v -> finish());

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

        updateChipStyle();
        filterReports();
    }

    private void filterReports() {
        String searchText = "";

        if (etSearchReports.getText() != null) {
            searchText = etSearchReports.getText().toString().trim().toLowerCase();
        }

        int visibleCount = 0;

        for (int i = 0; i < itemCards.length; i++) {
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
}