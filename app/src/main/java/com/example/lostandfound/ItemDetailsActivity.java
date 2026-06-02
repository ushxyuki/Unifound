package com.example.lostandfound;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailsActivity extends AppCompatActivity {

    Button btnBackItemDetails, btnContactPerson;

    TextView tvDetailIcon, tvDetailTitle, tvDetailStatus;
    TextView tvDetailLocation, tvDetailDate, tvDetailCategory;
    TextView tvDetailDescription;
    TextView tvContactName, tvContactEmail, tvContactPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        btnBackItemDetails = findViewById(R.id.btnBackItemDetails);
        btnContactPerson = findViewById(R.id.btnContactPerson);

        tvDetailIcon = findViewById(R.id.tvDetailIcon);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);

        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);

        tvDetailDescription = findViewById(R.id.tvDetailDescription);

        tvContactName = findViewById(R.id.tvContactName);
        tvContactEmail = findViewById(R.id.tvContactEmail);
        tvContactPhone = findViewById(R.id.tvContactPhone);

        String icon = getIntent().getStringExtra("icon");
        String title = getIntent().getStringExtra("title");
        String status = getIntent().getStringExtra("status");
        String location = getIntent().getStringExtra("location");
        String date = getIntent().getStringExtra("date");
        String category = getIntent().getStringExtra("category");
        String description = getIntent().getStringExtra("description");
        String contactName = getIntent().getStringExtra("contactName");
        String contactEmail = getIntent().getStringExtra("contactEmail");
        String contactPhone = getIntent().getStringExtra("contactPhone");

        if (icon == null) icon = "🎒";
        if (title == null) title = "Black Backpack";
        if (status == null) status = "Lost";
        if (location == null) location = "Library - Floor 2";
        if (date == null) date = "01/06/2026";
        if (category == null) category = "Bag";
        if (description == null) {
            description = "Black Nike backpack with a laptop charger inside. The item was last seen near the library study area.";
        }
        if (contactName == null) contactName = "Student Services";
        if (contactEmail == null) contactEmail = "lostandfound@university.ac.uk";
        if (contactPhone == null) contactPhone = "Available through university office";

        tvDetailIcon.setText(icon);
        tvDetailTitle.setText(title);
        tvDetailStatus.setText(status);

        tvDetailLocation.setText(location);
        tvDetailDate.setText(date);
        tvDetailCategory.setText(category);

        tvDetailDescription.setText(description);

        tvContactName.setText(contactName);
        tvContactEmail.setText(contactEmail);
        tvContactPhone.setText(contactPhone);

        styleStatus(status);

        btnBackItemDetails.setOnClickListener(v -> finish());

        btnContactPerson.setOnClickListener(v ->
                Toast.makeText(this, "Contact request opened", Toast.LENGTH_SHORT).show()
        );
    }

    private void styleStatus(String status) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(50);

        if (status.equalsIgnoreCase("Found")) {
            drawable.setColor(Color.parseColor("#DCFCE7"));
            tvDetailStatus.setTextColor(Color.parseColor("#047857"));
        } else {
            drawable.setColor(Color.parseColor("#FEE2E2"));
            tvDetailStatus.setTextColor(Color.parseColor("#B91C1C"));
        }

        tvDetailStatus.setBackground(drawable);
    }
}