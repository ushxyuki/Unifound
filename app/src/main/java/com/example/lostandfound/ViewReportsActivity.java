package com.example.lostandfound;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

        cardLostBackpack.setOnClickListener(v ->
                Toast.makeText(this, "Black Backpack selected", Toast.LENGTH_SHORT).show());

        cardFoundKeychain.setOnClickListener(v ->
                Toast.makeText(this, "Silver Keychain selected", Toast.LENGTH_SHORT).show());

        cardLostCharger.setOnClickListener(v ->
                Toast.makeText(this, "Laptop Charger selected", Toast.LENGTH_SHORT).show());

        cardFoundBottle.setOnClickListener(v ->
                Toast.makeText(this, "Water Bottle selected", Toast.LENGTH_SHORT).show());

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
        chip.setTextColor(getResources().getColor(android.R.color.white));
        chip.setBackgroundColor(getResources().getColor(R.color.app_blue));
    }

    private void resetChip(TextView chip) {
        chip.setTextColor(getResources().getColor(R.color.app_blue));
        chip.setBackgroundColor(getResources().getColor(R.color.chip_background));
    }
}