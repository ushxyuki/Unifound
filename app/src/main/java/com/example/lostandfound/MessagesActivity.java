package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        setupBottomNav();
        
        Button btnBackMessages = findViewById(R.id.btnBackMessages);
        if (btnBackMessages != null) {
            btnBackMessages.setOnClickListener(v -> finish());
        }
    }

    private void setupBottomNav() {
        highlightTab();

        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        });
        findViewById(R.id.navReports).setOnClickListener(v -> {
            startActivity(new Intent(this, ViewReportsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        });
    }

    private void highlightTab() {
        LinearLayout pill = findViewById(R.id.navMessagesPill);
        TextView label = findViewById(R.id.navMessagesLabel);
        ImageView icon = findViewById(R.id.navMessagesIcon);

        if (pill != null) pill.setBackgroundResource(R.drawable.bg_nav_pill);
        if (label != null) {
            label.setVisibility(View.VISIBLE);
            label.setTextColor(ContextCompat.getColor(this, R.color.bottom_nav_selected));
        }
        if (icon != null) {
            icon.setColorFilter(ContextCompat.getColor(this, R.color.bottom_nav_selected));
        }
    }
}