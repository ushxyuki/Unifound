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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemDetailsActivity extends AppCompatActivity {

    private static final String OFFICE_NAME = "Lost & Found Office";
    private static final String OFFICE_DESK = "Student Services Desk";
    private static final String OFFICE_EMAIL = "lostandfound@university.ac.uk";
    private static final String OFFICE_PHONE = "Available through university office";
    private static final String[] DATE_FIELD_NAMES = {
            "date", "dateTime", "selectedDate", "userDate", "lostDate",
            "foundDate", "itemDate", "reportDate", "uploadDate"
    };
    private static final String[] TIMESTAMP_FIELD_NAMES = {
            "createdAt", "timestamp", "uploadedAt", "uploadTimestamp"
    };
    private static final String[] MILLIS_FIELD_NAMES = {
            "timestampMillis", "createdAtMillis"
    };

    private View btnBackItemDetails;
    private Button btnContactPerson;

    private ImageView imgDetailImage;
    private TextView tvDetailIcon, tvDetailTitle, tvDetailStatus;
    private TextView tvDetailLocation, tvDetailDate, tvDetailCategory;
    private TextView tvDetailDescription;
    private TextView tvContactName, tvContactOffice, tvContactEmail, tvContactPhone, tvReporterEmail;

    private FirebaseFirestore db;

    private String title;
    private String itemName;
    private String status;
    private String icon;
    private String location;
    private String date;
    private String category;
    private String description;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String reporterEmail;
    private String imageUrl;
    private String documentId;
    private String collectionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        db = FirebaseFirestore.getInstance();

        initialiseViews();
        readIntentData();
        applyDefaultValues();
        displayData();

        if (isDateMissing()) {
            loadExactDateFromFirestore();
        }

        if (btnBackItemDetails != null) {
            btnBackItemDetails.setOnClickListener(v -> finish());
        }

        if (btnContactPerson != null) {
            btnContactPerson.setOnClickListener(v ->
                    Toast.makeText(this, "Please contact the Lost & Found Office", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void initialiseViews() {
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
    }

    private void readIntentData() {
        title = getCleanExtra("title");
        itemName = getCleanExtra("itemName");
        status = getCleanExtra("status");
        icon = getCleanExtra("icon");
        location = getCleanExtra("location");
        date = getBestDateFromIntent();
        category = getCleanExtra("category");
        description = getCleanExtra("description");
        contactName = getCleanExtra("contactName");
        contactEmail = getCleanExtra("contactEmail");
        contactPhone = getCleanExtra("contactPhone");
        reporterEmail = getCleanExtra("reporterEmail");
        imageUrl = getCleanExtra("imageUrl");

        documentId = getCleanExtra("documentId");
        collectionName = getCleanExtra("collectionName");
    }

    private void applyDefaultValues() {
        if (title.isEmpty()) title = itemName;
        if (title.isEmpty()) title = "Item details unavailable";

        if (itemName.isEmpty()) itemName = title;

        if (status.isEmpty()) status = "Lost";

        if (collectionName.isEmpty()) {
            collectionName = status.equalsIgnoreCase("Found") ? "found_reports" : "lost_reports";
        }

        if (icon.isEmpty()) {
            icon = status.equalsIgnoreCase("Found") ? "Found" : "Lost";
        }

        if (location.isEmpty()) location = "Location not available";
        if (date.isEmpty()) date = "Date not available";
        if (category.isEmpty()) category = "Category not available";
        if (description.isEmpty()) description = "No description provided.";

        if (contactName.isEmpty()) contactName = "Student Services";
        if (contactEmail.isEmpty()) contactEmail = OFFICE_EMAIL;
        if (contactPhone.isEmpty()) contactPhone = OFFICE_PHONE;
    }

    private void displayData() {
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
    }

    private boolean isDateMissing() {
        return date == null
                || date.trim().isEmpty()
                || date.equalsIgnoreCase("Date not available");
    }

    private void loadExactDateFromFirestore() {
        if (documentId == null || documentId.trim().isEmpty()) {
            tryLoadDateByMatchingItem();
            return;
        }

        db.collection(collectionName)
                .document(documentId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String firestoreDate = getBestDateFromDocument(document);

                        if (!firestoreDate.isEmpty()) {
                            date = firestoreDate;
                            tvDetailDate.setText(date);
                        } else {
                            tvDetailDate.setText("Date not available");
                        }
                    }
                })
                .addOnFailureListener(e -> tvDetailDate.setText("Date not available"));
    }

    private void tryLoadDateByMatchingItem() {
        if (itemName == null || itemName.trim().isEmpty()
                || itemName.equalsIgnoreCase("Item details unavailable")) {
            tvDetailDate.setText("Date not available");
            return;
        }

        db.collection(collectionName)
                .limit(100)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        if (isMatchingItem(document)) {
                            String firestoreDate = getBestDateFromDocument(document);

                            if (!firestoreDate.isEmpty()) {
                                date = firestoreDate;
                                tvDetailDate.setText(date);
                                return;
                            }
                        }
                    }

                    tvDetailDate.setText("Date not available");
                })
                .addOnFailureListener(e -> tvDetailDate.setText("Date not available"));
    }

    private boolean isMatchingItem(DocumentSnapshot document) {
        String docItemName = getStringFromDocument(document, "itemName");
        String docTitle = getStringFromDocument(document, "title");
        String docName = getStringFromDocument(document, "name");
        String docLocation = getStringFromDocument(document, "location");
        String docCategory = getStringFromDocument(document, "category");

        if (matches(itemName, docItemName)) return true;
        if (matches(itemName, docTitle)) return true;
        if (matches(itemName, docName)) return true;

        return !location.equalsIgnoreCase("Location not available")
                && !category.equalsIgnoreCase("Category not available")
                && matches(location, docLocation)
                && matches(category, docCategory);
    }

    private String getBestDateFromIntent() {
        String dateValue = getCleanExtra("date");
        if (isUsableDate(dateValue)) return dateValue;

        for (String fieldName : DATE_FIELD_NAMES) {
            if ("date".equals(fieldName)) {
                continue;
            }
            String value = getCleanExtra(fieldName);
            if (isUsableDate(value)) {
                return value;
            }
        }

        long timestampMillis = getLongExtra("timestampMillis");
        if (timestampMillis > 0) return formatDateTime(timestampMillis);

        long createdAtMillis = getLongExtra("createdAtMillis");
        if (createdAtMillis > 0) return formatDateTime(createdAtMillis);

        long uploadTimestampMillis = getLongExtra("uploadTimestampMillis");
        if (uploadTimestampMillis > 0) return formatDateTime(uploadTimestampMillis);

        return "";
    }

    private String getBestDateFromDocument(DocumentSnapshot document) {
        for (String fieldName : DATE_FIELD_NAMES) {
            String value = getDateField(document, fieldName);
            if (!value.isEmpty()) return value;
        }

        for (String fieldName : TIMESTAMP_FIELD_NAMES) {
            String value = getDateField(document, fieldName);
            if (!value.isEmpty()) return value;
        }

        for (String fieldName : MILLIS_FIELD_NAMES) {
            String value = getDateField(document, fieldName);
            if (!value.isEmpty()) return value;
        }

        return "";
    }

    private String getDateField(DocumentSnapshot document, String fieldName) {
        Object value = document.get(fieldName);

        if (value == null) {
            return "";
        }

        if (value instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) value;
            return formatDateTime(timestamp.toDate().getTime());
        }

        if (value instanceof Date) {
            Date dateValue = (Date) value;
            return formatDateTime(dateValue.getTime());
        }

        if (value instanceof Number) {
            long millis = ((Number) value).longValue();
            if (millis > 0) return formatDateTime(millis);
            return "";
        }

        if (value instanceof String) {
            String text = ((String) value).trim();
            if (!isUsableDate(text)) {
                return "";
            }
            if (isMillisField(fieldName)) {
                long millis = parseMillis(text);
                return millis > 0 ? formatDateTime(millis) : "";
            }
            return text;
        }

        String text = value.toString().trim();
        return isUsableDate(text) ? text : "";
    }

    private String getCleanExtra(String key) {
        String value = getIntent().getStringExtra(key);
        return value == null ? "" : value.trim();
    }

    private long getLongExtra(String key) {
        long value = getIntent().getLongExtra(key, -1);
        if (value > 0) {
            return value;
        }
        return parseMillis(getCleanExtra(key));
    }

    private long parseMillis(String value) {
        try {
            long millis = Long.parseLong(value.trim());
            return millis > 0 ? millis : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private boolean isMillisField(String fieldName) {
        return "timestampMillis".equals(fieldName) || "createdAtMillis".equals(fieldName);
    }

    private boolean isUsableDate(String value) {
        return value != null
                && !value.trim().isEmpty()
                && !value.trim().equalsIgnoreCase("Date not available");
    }

    private String getStringFromDocument(DocumentSnapshot document, String fieldName) {
        String value = document.getString(fieldName);
        return value == null ? "" : value.trim();
    }

    private boolean matches(String first, String second) {
        if (first == null || second == null) return false;
        return first.trim().equalsIgnoreCase(second.trim());
    }

    private String formatDateTime(long millis) {
        Date dateValue = new Date(millis);
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return formatter.format(dateValue);
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

        return status != null && status.equalsIgnoreCase("Found")
                ? R.drawable.ic_report_found
                : R.drawable.ic_bag;
    }

    private int getStatusTextColor(String status) {
        return status != null && status.equalsIgnoreCase("Found")
                ? R.color.status_found
                : R.color.status_lost;
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
