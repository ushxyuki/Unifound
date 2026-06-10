package com.example.lostandfound;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class ItemDetailsActivity extends AppCompatActivity {

    private static final String OFFICE_NAME = "Lost & Found Office";
    private static final String OFFICE_DESK = "Student Services Desk";
    private static final String OFFICE_EMAIL = "lostandfound@university.ac.uk";
    private static final String OFFICE_PHONE = "Available through university office";

    private View btnBackItemDetails;
    private Button btnContactPerson;

    private ImageView imgDetailImage;
    private TextView tvDetailIcon, tvDetailTitle, tvDetailStatus;
    private TextView tvDetailLocation, tvDetailDate, tvDetailCategory;
    private TextView tvDetailDescription;
    private TextView tvContactName, tvContactOffice, tvContactEmail, tvContactPhone, tvReporterEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        btnBackItemDetails = findViewById(R.id.btnBackItemDetails);
        btnContactPerson = findViewById(R.id.btnContactPerson);

        imgDetailImage = findViewById(R.id.imgDetailImage);
        tvDetailIcon = findViewById(R.id.tvDetailIcon);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);

        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);

        tvDetailDescription = findViewById(R.id.tvDetailDescription);

        tvContactName = findViewById(R.id.tvContactName);
        tvContactOffice = findViewById(R.id.tvContactOffice);
        tvContactEmail = findViewById(R.id.tvContactEmail);
        tvContactPhone = findViewById(R.id.tvContactPhone);
        tvReporterEmail = findViewById(R.id.tvReporterEmail);

        String title = getIntent().getStringExtra("title");
        String itemName = getIntent().getStringExtra("itemName");
        String status = getIntent().getStringExtra("status");
        String icon = getIntent().getStringExtra("icon");
        String location = getIntent().getStringExtra("location");
        String date = getIntent().getStringExtra("date");
        String category = getIntent().getStringExtra("category");
        String description = getIntent().getStringExtra("description");
        String contactName = getIntent().getStringExtra("contactName");
        String contactEmail = getIntent().getStringExtra("contactEmail");
        String contactPhone = getIntent().getStringExtra("contactPhone");
        String reporterEmail = getIntent().getStringExtra("reporterEmail");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        if (title == null || title.trim().isEmpty()) title = itemName;
        if (title == null || title.trim().isEmpty()) title = "Item details unavailable";
        if (status == null || status.trim().isEmpty()) status = "Lost";
        if (icon == null) icon = status.equalsIgnoreCase("Found") ? "Found" : "Lost";
        if (location == null || location.trim().isEmpty()) location = "Location not available";
        if (date == null || date.trim().isEmpty()) date = "Date not available";
        if (category == null || category.trim().isEmpty()) category = "Category not available";
        if (description == null) {
            description = "No description provided.";
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
        tvContactName.setText(OFFICE_NAME);
        tvContactOffice.setText(OFFICE_DESK);
        tvContactEmail.setText(OFFICE_EMAIL);
        tvContactPhone.setText(OFFICE_PHONE);
        showReporterEmail(reporterEmail, contactEmail);

        showImageOrIcon(imageUrl, status, category);
        styleStatus(status);

        if (btnBackItemDetails != null) {
            btnBackItemDetails.setOnClickListener(v -> finish());
        }

        if (btnContactPerson != null) {
            btnContactPerson.setOnClickListener(v ->
                    Toast.makeText(this, "Please contact the Lost & Found Office", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void showImageOrIcon(String imageUrl, String status, String category) {
        int fallbackDrawable = getDefaultIconRes(status, category);

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            showFallbackIcon(status, fallbackDrawable);
            return;
        }

        tvDetailIcon.setVisibility(View.GONE);
        imgDetailImage.setVisibility(View.VISIBLE);
        imgDetailImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgDetailImage.setPadding(0, 0, 0, 0);
        imgDetailImage.setColorFilter(null);
        imgDetailImage.setBackgroundColor(Color.TRANSPARENT);

        Glide.with(this)
                .load(imageUrl.trim())
                .placeholder(fallbackDrawable)
                .error(fallbackDrawable)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource
                    ) {
                        showFallbackIcon(status, fallbackDrawable);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource
                    ) {
                        return false;
                    }
                })
                .into(imgDetailImage);
    }

    private void showFallbackIcon(String status, int fallbackDrawable) {
        tvDetailIcon.setVisibility(View.GONE);
        imgDetailImage.setVisibility(View.VISIBLE);
        imgDetailImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imgDetailImage.setPadding(dp(34), dp(34), dp(34), dp(34));
        imgDetailImage.setImageResource(fallbackDrawable);
        imgDetailImage.setColorFilter(ContextCompat.getColor(this, getStatusTextColor(status)));
        imgDetailImage.setBackgroundResource(status != null && status.equalsIgnoreCase("Found")
                ? R.drawable.bg_icon_soft_green
                : R.drawable.bg_icon_soft_blue);
    }

    private void showReporterEmail(String reporterEmail, String contactEmail) {
        String reporter = cleanText(reporterEmail);
        if (reporter.isEmpty()) {
            reporter = cleanText(contactEmail);
        }

        if (reporter.isEmpty() || reporter.equalsIgnoreCase(OFFICE_EMAIL)) {
            tvReporterEmail.setVisibility(View.GONE);
            return;
        }

        tvReporterEmail.setText("Reporter: " + reporter);
        tvReporterEmail.setVisibility(View.VISIBLE);
    }

    private int getDefaultIconRes(String status, String category) {
        String normalizedCategory = category == null ? "" : category.toLowerCase();
        if (normalizedCategory.contains("key")) {
            return R.drawable.ic_key;
        }
        if (normalizedCategory.contains("bottle")) {
            return R.drawable.ic_bottle;
        }
        if (normalizedCategory.contains("laptop") || normalizedCategory.contains("electronic")) {
            return R.drawable.ic_laptop;
        }
        if (normalizedCategory.contains("bag") || normalizedCategory.contains("backpack")) {
            return R.drawable.ic_bag;
        }
        return status != null && status.equalsIgnoreCase("Found") ? R.drawable.ic_report_found : R.drawable.ic_bag;
    }

    private int getStatusTextColor(String status) {
        return status != null && status.equalsIgnoreCase("Found") ? R.color.status_found : R.color.status_lost;
    }

    private String cleanText(String value) {
        return value == null ? "" : value.trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void styleStatus(String status) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(50);

        if (status != null && status.equalsIgnoreCase("Found")) {
            drawable.setColor(ContextCompat.getColor(this, R.color.app_found_bg));
            tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_found));
        } else {
            drawable.setColor(ContextCompat.getColor(this, R.color.app_lost_bg));
            tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_lost));
        }

        tvDetailStatus.setBackground(drawable);
    }
}
