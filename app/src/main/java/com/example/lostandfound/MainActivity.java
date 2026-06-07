package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView cardReportLost;
    private MaterialCardView cardReportFound;
    private TextView tvProfileInitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardReportLost = findViewById(R.id.cardReportLost);
        cardReportFound = findViewById(R.id.cardReportFound);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);

        if (cardReportLost != null) {
            cardReportLost.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, ReportLostActivity.class))
            );
        }

        if (cardReportFound != null) {
            cardReportFound.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, ReportFoundActivity.class))
            );
        }

        if (tvProfileInitial != null) {
            tvProfileInitial.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class))
            );
        }

        BottomNavHelper.setup(this, BottomNavHelper.Tab.HOME);
    }
}
