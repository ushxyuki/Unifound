package com.example.lostandfound;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewReportsActivity extends AppCompatActivity {

    private static final String TAG = "ViewReportsActivity";
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

    private View btnBackReports;
    private TextInputEditText etSearchReports;
    private TextView chipAll;
    private TextView chipLost;
    private TextView chipFound;


    private LinearLayout reportsContainer;
    private TextView tvEmptyState;

    private final List<ReportListItem> allReports = new ArrayList<>();
    private String selectedFilter = "All";
    private String currentUid = "";
    private int loadedLostReportCount = 0;
    private int loadedFoundReportCount = 0;
    private boolean lostReportsLoaded = false;
    private boolean foundReportsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reports);

        btnBackReports = findViewById(R.id.btnBackReports);
        etSearchReports = findViewById(R.id.etSearchReports);
        chipAll = findViewById(R.id.chipAll);
        chipLost = findViewById(R.id.chipLost);
        chipFound = findViewById(R.id.chipFound);
        reportsContainer = findViewById(R.id.reportsContainer);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        setupBottomNav();

        if (btnBackReports != null) {
            btnBackReports.setOnClickListener(v -> finish());
        }

        chipAll.setOnClickListener(v -> setFilter("All"));
        chipLost.setOnClickListener(v -> setFilter("Lost"));
        chipFound.setOnClickListener(v -> setFilter("Found"));

        etSearchReports.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderReports();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        updateChipStyle();
        loadReportsFromFirestore();
    }

    private void setupBottomNav() {
        BottomNavHelper.setup(this, BottomNavHelper.Tab.REPORTS);
    }

    private void setFilter(String filter) {
        selectedFilter = filter;
        updateChipStyle();
        renderReports();
    }

    private void loadReportsFromFirestore() {
        Log.d(TAG, "Starting loadReportsFromFirestore");

        lostReportsLoaded = false;
        foundReportsLoaded = false;
        loadedLostReportCount = 0;
        loadedFoundReportCount = 0;
        currentUid = "";
        allReports.clear();
        reportsContainer.removeAllViews();
        tvEmptyState.setVisibility(View.GONE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user. Showing empty My Reports state.");
            Toast.makeText(this, "Please log in to view your reports", Toast.LENGTH_SHORT).show();
            lostReportsLoaded = true;
            foundReportsLoaded = true;
            renderReports();
            return;
        }

        currentUid = currentUser.getUid();
        Log.d(TAG, "Loading my reports for currentUid=" + currentUid);

        FirebaseFirestore db = FirestoreProvider.getFirestore();

        db.collection("lost_reports")
                .whereEqualTo("userId", currentUid)
                .get()
                .addOnSuccessListener(lostSnapshot -> {
                    loadedLostReportCount = lostSnapshot.size();
                    Log.d(TAG, "My lost reports loaded: " + loadedLostReportCount);
                    addSnapshotReports(allReports, lostSnapshot, "Lost", "lost_reports");
                    lostReportsLoaded = true;
                    if (foundReportsLoaded) {
                        onBothQueriesComplete();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lost reports query failed: " + e.getMessage(), e);
                    Toast.makeText(ViewReportsActivity.this, "Error loading lost reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    lostReportsLoaded = true;
                    if (foundReportsLoaded) {
                        onBothQueriesComplete();
                    }
                });

        db.collection("found_reports")
                .whereEqualTo("userId", currentUid)
                .get()
                .addOnSuccessListener(foundSnapshot -> {
                    loadedFoundReportCount = foundSnapshot.size();
                    Log.d(TAG, "My found reports loaded: " + loadedFoundReportCount);
                    addSnapshotReports(allReports, foundSnapshot, "Found", "found_reports");
                    foundReportsLoaded = true;
                    if (lostReportsLoaded) {
                        onBothQueriesComplete();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Found reports query failed: " + e.getMessage(), e);
                    Toast.makeText(ViewReportsActivity.this, "Error loading found reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    foundReportsLoaded = true;
                    if (lostReportsLoaded) {
                        onBothQueriesComplete();
                    }
                });
    }

    private void onBothQueriesComplete() {
        Collections.sort(allReports, (first, second) ->
                Long.compare(second.getSortMillis(), first.getSortMillis())
        );
        Log.d(TAG, "currentUid=" + currentUid
                + ", my lost reports=" + loadedLostReportCount
                + ", my found reports=" + loadedFoundReportCount
                + ", my total reports=" + allReports.size());
        renderReports();
    }

    private void addSnapshotReports(
            List<ReportListItem> reports,
            QuerySnapshot snapshot,
            String fallbackStatus,
            String collectionName
    ) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String title = getDocumentString(document, "title", "");
            String itemName = firstDocumentString(document, "itemName", "title");
            if (itemName.isEmpty()) {
                itemName = "Untitled item";
            }
            if (title.isEmpty()) {
                title = itemName;
            }

            String status = normalizeStatus(getDocumentString(document, "status", fallbackStatus));
            String reporterEmail = getDocumentString(document, "reporterEmail", "");
            String contactName = getDocumentString(document, "contactName", "Lost & Found Office");
            String contactEmail = getDocumentString(document, "contactEmail", "");
            if (contactEmail.isEmpty()) {
                contactEmail = reporterEmail;
            }
            long createdAtMillis = getMillisFromFields(document, "createdAt", "createdAtMillis");
            long timestampMillis = getMillisFromFields(document, "timestamp", "uploadedAt", "uploadTimestamp", "timestampMillis");

            reports.add(new ReportListItem(
                    document.getId(),
                    collectionName,
                    title,
                    itemName,
                    status,
                    getDocumentString(document, "location", ""),
                    getBestDateFromDocument(document),
                    getDocumentString(document, "time", ""),
                    getDocumentString(document, "category", ""),
                    getDocumentString(document, "description", ""),
                    contactName,
                    contactEmail,
                    getDocumentString(document, "contactPhone", ""),
                    reporterEmail,
                    getDocumentString(document, "imageUrl", ""),
                    timestampMillis,
                    createdAtMillis
            ));
        }
    }

    private void renderReports() {
        reportsContainer.removeAllViews();

        String searchText = "";
        if (etSearchReports.getText() != null) {
            searchText = etSearchReports.getText().toString().trim().toLowerCase();
        }

        int visibleCount = 0;
        for (ReportListItem report : allReports) {
            if (!matchesFilter(report) || !matchesSearch(report, searchText)) {
                continue;
            }

            reportsContainer.addView(createReportCard(report));
            visibleCount++;
        }

        boolean bothQueriesFinished = lostReportsLoaded && foundReportsLoaded;
        tvEmptyState.setVisibility(bothQueriesFinished && visibleCount == 0 ? View.VISIBLE : View.GONE);
        Log.d(TAG, "Visible reports after filtering: " + visibleCount);
    }

    private boolean matchesFilter(ReportListItem report) {
        return "All".equals(selectedFilter)
                || report.status.equalsIgnoreCase(selectedFilter);
    }

    private boolean matchesSearch(ReportListItem report, String searchText) {
        if (searchText.isEmpty()) {
            return true;
        }

        return report.itemName.toLowerCase().contains(searchText)
                || report.location.toLowerCase().contains(searchText)
                || report.status.toLowerCase().contains(searchText)
                || report.category.toLowerCase().contains(searchText)
                || report.description.toLowerCase().contains(searchText);
    }

    private MaterialCardView createReportCard(ReportListItem report) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.app_card_white));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.card_stroke));
        card.setStrokeWidth(dp(1));
        card.setRadius(dp(16));
        card.setCardElevation(dp(3));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> openItemDetails(report));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, dp(14), 0, 0);
        card.setLayoutParams(cardParams);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(16), dp(16), dp(16), dp(16));

        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(60), dp(60));
        imageView.setLayoutParams(imageParams);
        imageView.setContentDescription("Item image");
        bindReportImage(imageView, report);
        row.addView(imageView);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        textParams.setMargins(dp(14), 0, dp(12), 0);
        textColumn.setLayoutParams(textParams);

        TextView titleView = makeTextView(report.itemName, R.color.text_dark, 16, true);
        textColumn.addView(titleView);

        TextView locationView = makeTextView(emptyFallback(report.location, "Location not added"), R.color.text_medium, 13, false);
        LinearLayout.LayoutParams locationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        locationParams.setMargins(0, dp(4), 0, 0);
        locationView.setLayoutParams(locationParams);
        textColumn.addView(locationView);

        TextView dateView = makeTextView(formatDateTime(report.date, report.time), R.color.text_medium, 12, false);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dateParams.setMargins(0, dp(3), 0, 0);
        dateView.setLayoutParams(dateParams);
        textColumn.addView(dateView);

        row.addView(textColumn);

        TextView statusView = makeTextView(report.status, getStatusTextColor(report.status), 12, true);
        statusView.setGravity(Gravity.CENTER);
        statusView.setMinWidth(dp(74));
        statusView.setMinHeight(dp(34));
        statusView.setPadding(dp(14), 0, dp(14), 0);
        statusView.setBackground(makeRoundedBackground(getStatusBackgroundColor(report.status)));
        row.addView(statusView);

        card.addView(row);
        return card;
    }

    private void bindReportImage(ImageView imageView, ReportListItem report) {
        int defaultIconRes = getDefaultIconRes(report.status, report.category);

        if (report.imageUrl.isEmpty()) {
            showDefaultImage(imageView, report.status, defaultIconRes);
            return;
        }

        imageView.setBackgroundColor(Color.TRANSPARENT);
        imageView.setColorFilter(null);
        imageView.setPadding(0, 0, 0, 0);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this)
                .load(report.imageUrl)
                .placeholder(defaultIconRes)
                .error(defaultIconRes)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource
                    ) {
                        showDefaultImage(imageView, report.status, defaultIconRes);
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
                .into(imageView);
    }

    private void showDefaultImage(ImageView imageView, String status, int drawableRes) {
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setPadding(dp(14), dp(14), dp(14), dp(14));
        imageView.setImageResource(drawableRes);
        imageView.setColorFilter(ContextCompat.getColor(this, getStatusTextColor(status)));
        imageView.setBackgroundResource("Found".equalsIgnoreCase(status)
                ? R.drawable.bg_icon_soft_green
                : R.drawable.bg_icon_soft_blue);
    }

    private TextView makeTextView(String text, int colorRes, int textSizeSp, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(this, colorRes));
        textView.setTextSize(textSizeSp);
        if (bold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return textView;
    }

    private void updateChipStyle() {
        resetChip(chipAll);
        resetChip(chipLost);
        resetChip(chipFound);

        if ("All".equals(selectedFilter)) {
            selectChip(chipAll);
        } else if ("Lost".equals(selectedFilter)) {
            selectChip(chipLost);
        } else {
            selectChip(chipFound);
        }
    }

    private void selectChip(TextView chip) {
        chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        chip.setBackground(makeRoundedBackground(R.color.app_blue));
    }

    private void resetChip(TextView chip) {
        chip.setTextColor(ContextCompat.getColor(this, R.color.text_blue));
        chip.setBackground(makeRoundedBackground(R.color.app_soft_blue));
    }

    private GradientDrawable makeRoundedBackground(int colorRes) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(this, colorRes));
        drawable.setCornerRadius(dp(50));
        return drawable;
    }

    private int getStatusTextColor(String status) {
        return "Found".equalsIgnoreCase(status) ? R.color.status_found : R.color.status_lost;
    }

    private int getStatusBackgroundColor(String status) {
        return "Found".equalsIgnoreCase(status) ? R.color.app_found_bg : R.color.app_lost_bg;
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
        return "Found".equalsIgnoreCase(status) ? R.drawable.ic_report_found : R.drawable.ic_bag;
    }

    private void openItemDetails(ReportListItem report) {
        Intent intent = new Intent(ViewReportsActivity.this, ItemDetailsActivity.class);
        intent.putExtra("documentId", report.documentId);
        intent.putExtra("collectionName", report.collectionName);
        intent.putExtra("date", formatDateTime(report.date, report.time));
        intent.putExtra("timestampMillis", report.timestampMillis);
        intent.putExtra("createdAtMillis", report.createdAtMillis);
        intent.putExtra("title", report.title);
        intent.putExtra("itemName", report.itemName);
        intent.putExtra("status", report.status);
        intent.putExtra("location", report.location);
        intent.putExtra("time", report.time);
        intent.putExtra("category", report.category);
        intent.putExtra("description", report.description);
        intent.putExtra("contactName", report.contactName);
        intent.putExtra("contactEmail", report.contactEmail);
        intent.putExtra("contactPhone", report.contactPhone);
        intent.putExtra("reporterEmail", report.reporterEmail);
        intent.putExtra("imageUrl", report.imageUrl);
        startActivity(intent);
    }

    private String getDocumentString(DocumentSnapshot document, String field, String fallback) {
        Object rawValue = document.get(field);
        if (rawValue == null) {
            return fallback;
        }
        String value = String.valueOf(rawValue).trim();
        if (value.isEmpty()) {
            return fallback;
        }
        return value;
    }

    private String firstDocumentString(DocumentSnapshot document, String... fields) {
        for (String field : fields) {
            String value = getDocumentString(document, field, "");
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String getBestDateFromDocument(DocumentSnapshot document) {
        for (String fieldName : DATE_FIELD_NAMES) {
            String value = getDateField(document, fieldName);
            if (!value.isEmpty()) {
                return value;
            }
        }

        for (String fieldName : TIMESTAMP_FIELD_NAMES) {
            String value = getDateField(document, fieldName);
            if (!value.isEmpty()) {
                return value;
            }
        }

        for (String fieldName : MILLIS_FIELD_NAMES) {
            String value = getDateField(document, fieldName);
            if (!value.isEmpty()) {
                return value;
            }
        }

        return "";
    }

    private String getDateField(DocumentSnapshot document, String fieldName) {
        Object value = document.get(fieldName);
        if (value == null) {
            return "";
        }

        if (value instanceof Timestamp) {
            return formatDateTime(((Timestamp) value).toDate().getTime());
        }

        if (value instanceof Date) {
            return formatDateTime(((Date) value).getTime());
        }

        if (value instanceof Number) {
            long millis = ((Number) value).longValue();
            return millis > 0 ? formatDateTime(millis) : "";
        }

        String text = String.valueOf(value).trim();
        if (!isUsableDate(text)) {
            return "";
        }

        if (isMillisField(fieldName)) {
            long millis = parseMillis(text);
            return millis > 0 ? formatDateTime(millis) : "";
        }

        return text;
    }

    private long getMillisFromFields(DocumentSnapshot document, String... fieldNames) {
        for (String fieldName : fieldNames) {
            long millis = getMillisFromField(document.get(fieldName));
            if (millis > 0) {
                return millis;
            }
        }
        return 0L;
    }

    private long getMillisFromField(Object value) {
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toDate().getTime();
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        if (value instanceof Number) {
            return Math.max(0L, ((Number) value).longValue());
        }
        if (value instanceof String) {
            return parseMillis((String) value);
        }
        return 0L;
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

    private String normalizeStatus(String status) {
        if ("Found".equalsIgnoreCase(status)) {
            return "Found";
        }
        return "Lost";
    }

    private String formatDateTime(String date, String time) {
        boolean hasDate = isUsableDate(date);
        boolean hasTime = time != null && !time.trim().isEmpty();
        if (hasDate && date.trim().contains(":")) {
            return date.trim();
        }
        if (hasDate && hasTime) {
            return date.trim() + " at " + time.trim();
        }
        if (hasDate) {
            return date.trim();
        }
        if (hasTime) {
            return time.trim();
        }
        return "Date not available";
    }

    private String formatDateTime(long millis) {
        return new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    private String emptyFallback(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class ReportListItem {
        final String documentId;
        final String collectionName;
        final String title;
        final String itemName;
        final String status;
        final String location;
        final String date;
        final String time;
        final String category;
        final String description;
        final String contactName;
        final String contactEmail;
        final String contactPhone;
        final String reporterEmail;
        final String imageUrl;
        final long timestampMillis;
        final long createdAtMillis;

        ReportListItem(
                String documentId,
                String collectionName,
                String title,
                String itemName,
                String status,
                String location,
                String date,
                String time,
                String category,
                String description,
                String contactName,
                String contactEmail,
                String contactPhone,
                String reporterEmail,
                String imageUrl,
                long timestampMillis,
                long createdAtMillis
        ) {
            this.documentId = documentId == null ? "" : documentId.trim();
            this.collectionName = collectionName == null ? "" : collectionName.trim();
            this.title = title == null ? "" : title.trim();
            this.itemName = itemName;
            this.status = status;
            this.location = location;
            this.date = date;
            this.time = time;
            this.category = category;
            this.description = description;
            this.contactName = contactName;
            this.contactEmail = contactEmail;
            this.contactPhone = contactPhone;
            this.reporterEmail = reporterEmail;
            this.imageUrl = imageUrl == null ? "" : imageUrl.trim();
            this.timestampMillis = timestampMillis;
            this.createdAtMillis = createdAtMillis;
        }

        long getSortMillis() {
            if (createdAtMillis > 0) {
                return createdAtMillis;
            }
            return timestampMillis;
        }
    }
}
