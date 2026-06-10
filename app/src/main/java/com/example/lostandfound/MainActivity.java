package com.example.lostandfound;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MaterialCardView cardReportLost;
    private MaterialCardView cardReportFound;
    private EditText etSearch;
    private TextView tvProfileInitial;
    private TextView tvRecentlyReportedTitle;
    private LinearLayout recentlyReportedContainer;
    private TextView tvNoRecentReports;

    private final List<RecentReportItem> allPublicReports = new ArrayList<>();
    private String selectedCategoryFilter = "";
    private String lastSearchQuery = "";
    private boolean preservingCategoryWhileClearingSearch = false;
    private boolean lostReportsLoaded = false;
    private boolean foundReportsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardReportLost = findViewById(R.id.cardReportLost);
        cardReportFound = findViewById(R.id.cardReportFound);
        etSearch = findViewById(R.id.etSearch);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        tvRecentlyReportedTitle = findViewById(R.id.tvRecentlyReportedTitle);
        recentlyReportedContainer = findViewById(R.id.recentlyReportedContainer);
        tvNoRecentReports = findViewById(R.id.tvNoRecentReports);

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

        setupSearchAndCategoryFilters();

        BottomNavHelper.setup(this, BottomNavHelper.Tab.HOME);
        
        // 5. Prefer loading from onResume to avoid duplicate load.
        // Removed loadRecentReports() from here.
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: reloading reports");
        // 5. Perform load only here.
        loadRecentReports();
    }

    private void setupSearchAndCategoryFilters() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = normalize(s == null ? "" : s.toString());
                    if (query.isEmpty() && !lastSearchQuery.isEmpty() && !preservingCategoryWhileClearingSearch) {
                        selectedCategoryFilter = "";
                    }
                    lastSearchQuery = query;
                    if (preservingCategoryWhileClearingSearch && query.isEmpty()) {
                        preservingCategoryWhileClearingSearch = false;
                    }
                    renderReports();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        setCategoryClickListener(R.id.chipCategoryBags, "Bags");
        setCategoryClickListener(R.id.chipCategoryStudentId, "Student ID");
        setCategoryClickListener(R.id.chipCategoryElectronics, "Electronics");
        setCategoryClickListener(R.id.chipCategoryKeys, "Keys");
        setCategoryClickListener(R.id.chipCategoryPhone, "Phone");
        setCategoryClickListener(R.id.chipCategoryBottles, "Bottles");

        if (tvRecentlyReportedTitle != null) {
            tvRecentlyReportedTitle.setOnClickListener(v -> resetReportFilters());
        }
    }

    private void setCategoryClickListener(int viewId, String category) {
        View chip = findViewById(viewId);
        if (chip == null) {
            return;
        }
        chip.setOnClickListener(v -> selectCategoryFilter(category));
    }

    private void selectCategoryFilter(String category) {
        selectedCategoryFilter = category;
        if (etSearch != null && !getSearchQuery().isEmpty()) {
            preservingCategoryWhileClearingSearch = true;
            etSearch.setText("");
            return;
        }
        renderReports();
    }

    private void resetReportFilters() {
        selectedCategoryFilter = "";
        preservingCategoryWhileClearingSearch = false;
        if (etSearch != null && !getSearchQuery().isEmpty()) {
            etSearch.setText("");
            return;
        }
        lastSearchQuery = "";
        renderReports();
    }

    private void loadRecentReports() {
        Log.d(TAG, "Starting loadRecentReports");
        FirebaseFirestore db = FirestoreProvider.getFirestore();

        // 2. Clear state at start to prevent duplicates
        lostReportsLoaded = false;
        foundReportsLoaded = false;
        allPublicReports.clear();
        if (recentlyReportedContainer != null) {
            recentlyReportedContainer.removeAllViews();
        }
        renderReports();

        // Load lost reports
        db.collection("lost_reports")
                .get()
                .addOnSuccessListener(lostSnapshot -> {
                    int lostCount = lostSnapshot.size();
                    // 10. Debug log
                    Log.d("FirestoreDebug", "Home lost docs loaded: " + lostCount);
                    addSnapshotReports(allPublicReports, lostSnapshot, "Lost", "lost_reports");
                    lostReportsLoaded = true;
                    if (foundReportsLoaded) {
                        onBothQueriesComplete();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Home failed to load lost_reports", e);
                    lostReportsLoaded = true;
                    if (foundReportsLoaded) {
                        onBothQueriesComplete();
                    }
                });

        // Load found reports
        db.collection("found_reports")
                .get()
                .addOnSuccessListener(foundSnapshot -> {
                    int foundCount = foundSnapshot.size();
                    // 10. Debug log
                    Log.d("FirestoreDebug", "Home found docs loaded: " + foundCount);
                    addSnapshotReports(allPublicReports, foundSnapshot, "Found", "found_reports");
                    foundReportsLoaded = true;
                    if (lostReportsLoaded) {
                        onBothQueriesComplete();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Home failed to load found_reports", e);
                    foundReportsLoaded = true;
                    if (lostReportsLoaded) {
                        onBothQueriesComplete();
                    }
                });
    }

    private void onBothQueriesComplete() {
        // 8. Deduplicate based on unique key in memory
        Set<String> seenKeys = new HashSet<>();
        List<RecentReportItem> uniqueReports = new ArrayList<>();
        
        for (RecentReportItem item : allPublicReports) {
            // 6. Deduplication using unique key
            String key = item.sourceCollection + "_" + item.documentId;
            if (!seenKeys.contains(key)) {
                seenKeys.add(key);
                uniqueReports.add(item);
                // 10. Log unique report key
                Log.d("FirestoreDebug", "Home report key: " + key);
            }
        }
        
        allPublicReports.clear();
        allPublicReports.addAll(uniqueReports);
        
        // 10. Log unique reports count
        Log.d("FirestoreDebug", "Home unique reports count: " + allPublicReports.size());

        // Sort by createdAtMillis descending
        Collections.sort(allPublicReports, (first, second) -> {
            if (first.createdAtMillis == 0 && second.createdAtMillis == 0) return 0;
            if (first.createdAtMillis == 0) return 1;
            if (second.createdAtMillis == 0) return -1;
            return Long.compare(second.createdAtMillis, first.createdAtMillis);
        });
        
        renderReports();
    }

    private void renderReports() {
        // 3. Always clear views before adding
        if (recentlyReportedContainer != null) {
            recentlyReportedContainer.removeAllViews();
        }

        if (!lostReportsLoaded || !foundReportsLoaded) {
            tvNoRecentReports.setVisibility(View.GONE);
            return;
        }

        String query = getSearchQuery();
        boolean hasSearch = !query.isEmpty();
        boolean hasCategory = !selectedCategoryFilter.isEmpty();
        List<RecentReportItem> visibleReports = new ArrayList<>();

        for (RecentReportItem report : allPublicReports) {
            if (hasCategory && !matchesCategory(report, selectedCategoryFilter)) {
                continue;
            }
            if (hasSearch && !matchesSearch(report, query)) {
                continue;
            }
            visibleReports.add(report);
        }

        // 9. Show latest 3 reports by default when search/category is empty
        if (!hasSearch && !hasCategory && visibleReports.size() > 3) {
            visibleReports = new ArrayList<>(visibleReports.subList(0, 3));
        }

        if (visibleReports.isEmpty()) {
            tvNoRecentReports.setText(getEmptyStateText(hasSearch, hasCategory));
            tvNoRecentReports.setVisibility(View.VISIBLE);
        } else {
            tvNoRecentReports.setVisibility(View.GONE);
            // 10. Log rendered reports count
            Log.d("FirestoreDebug", "Home rendered reports: " + visibleReports.size());
            for (int i = 0; i < visibleReports.size(); i++) {
                recentlyReportedContainer.addView(createReportCard(visibleReports.get(i), i < visibleReports.size() - 1));
            }
        }
    }

    private void addSnapshotReports(List<RecentReportItem> reports, QuerySnapshot snapshot, String fallbackStatus, String collectionName) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            // 7. Store documentId and sourceCollection
            String documentId = document.getId();
            String title = getDocumentString(document, "title", "");
            String itemName = firstDocumentString(document, "itemName", "title");
            if (itemName.isEmpty()) itemName = "Untitled item";
            if (title.isEmpty()) title = itemName;

            String status = normalizeStatus(getDocumentString(document, "status", fallbackStatus));
            String location = getDocumentString(document, "location", "");
            String imageUrl = getDocumentString(document, "imageUrl", "");
            String category = getDocumentString(document, "category", "");
            String description = getDocumentString(document, "description", "");

            long createdAtMillis = getCreatedAtMillis(document);

            reports.add(new RecentReportItem(
                    documentId,
                    collectionName,
                    title,
                    itemName,
                    status,
                    location,
                    imageUrl,
                    category,
                    description,
                    createdAtMillis
            ));
        }
    }

    private MaterialCardView createReportCard(RecentReportItem report, boolean addMarginBottom) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.app_card_white));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.app_border));
        card.setStrokeWidth(dp(1));
        card.setRadius(dp(20));
        card.setCardElevation(dp(3));
        card.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ItemDetailsActivity.class);
            intent.putExtra("itemName", report.itemName);
            intent.putExtra("status", report.status);
            intent.putExtra("location", report.location);
            intent.putExtra("imageUrl", report.imageUrl);
            intent.putExtra("category", report.category);
            intent.putExtra("description", report.description);
            startActivity(intent);
        });

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = addMarginBottom ? dp(12) : 0;
        card.setLayoutParams(cardParams);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));

        // Icon/Image
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(56), dp(56));
        imageView.setLayoutParams(imageParams);
        imageView.setContentDescription(report.itemName);
        bindReportImage(imageView, report);
        row.addView(imageView);

        // Text column
        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        textParams.setMargins(dp(14), 0, dp(12), 0);
        textColumn.setLayoutParams(textParams);

        TextView titleView = makeTextView(report.itemName, R.color.app_text_dark, 16, true);
        textColumn.addView(titleView);

        TextView locationView = makeTextView(
                report.location.isEmpty() ? "Location not added" : report.location,
                R.color.app_text_light,
                13,
                false
        );
        LinearLayout.LayoutParams locParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        locParams.topMargin = dp(4);
        locationView.setLayoutParams(locParams);
        textColumn.addView(locationView);

        row.addView(textColumn);

        // Status badge
        int statusTextColor = "Found".equalsIgnoreCase(report.status) ? R.color.success_green : R.color.danger_red;
        int statusBgColor = "Found".equalsIgnoreCase(report.status) ? R.drawable.bg_status_found : R.drawable.bg_status_lost;

        TextView statusView = makeTextView(report.status, statusTextColor, 12, true);
        statusView.setGravity(Gravity.CENTER);
        statusView.setMinWidth(dp(60));
        statusView.setMinHeight(dp(34));
        statusView.setPadding(dp(14), 0, dp(14), 0);
        statusView.setBackground(ContextCompat.getDrawable(this, statusBgColor));
        row.addView(statusView);

        card.addView(row);
        return card;
    }

    private void bindReportImage(ImageView imageView, RecentReportItem report) {
        int defaultIconRes = getDefaultIconRes(report.status,
                report.category + " " + report.itemName + " " + report.title);

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
                            GlideException e,
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
        int colorRes = "Found".equalsIgnoreCase(status) ? R.color.success_green : R.color.app_blue;
        imageView.setColorFilter(ContextCompat.getColor(this, colorRes));
        int bgRes = "Found".equalsIgnoreCase(status) ? R.drawable.bg_icon_soft_green : R.drawable.bg_icon_soft_blue;
        imageView.setBackgroundResource(bgRes);
    }

    private int getDefaultIconRes(String status, String category) {
        String normalizedCategory = normalize(category);
        if (normalizedCategory.contains("key")) return R.drawable.ic_key;
        if (normalizedCategory.contains("bottle")) return R.drawable.ic_bottle;
        if (normalizedCategory.contains("student id") || normalizedCategory.contains("id card")) return R.drawable.ic_id_card;
        if (normalizedCategory.contains("phone") || normalizedCategory.contains("mobile")) return R.drawable.ic_phone;
        if (normalizedCategory.contains("laptop") || normalizedCategory.contains("electronic")) return R.drawable.ic_laptop;
        if (normalizedCategory.contains("bag") || normalizedCategory.contains("backpack")) return R.drawable.ic_bag;
        return "Found".equalsIgnoreCase(status) ? R.drawable.ic_report_found : R.drawable.ic_bag;
    }

    private boolean matchesSearch(RecentReportItem report, String query) {
        return containsNormalized(report.title, query)
                || containsNormalized(report.itemName, query)
                || containsNormalized(report.category, query)
                || containsNormalized(report.location, query)
                || containsNormalized(report.description, query)
                || containsNormalized(report.status, query);
    }

    private boolean matchesCategory(RecentReportItem report, String category) {
        String reportText = normalize(report.title + " " + report.itemName + " " + report.category);
        switch (category) {
            case "Bags": return containsAny(reportText, "bags", "bag", "backpack", "rucksack");
            case "Student ID": return containsAny(reportText, "student id", "id card", "student card", "id badge");
            case "Electronics": return containsAny(reportText, "electronics", "electronic", "laptop", "charger", "computer", "tablet", "phone", "mobile", "headphone", "earphone", "earbuds", "camera", "keyboard", "mouse", "watch", "device");
            case "Keys": return containsAny(reportText, "keys", "key", "keychain", "key ring");
            case "Phone": return containsAny(reportText, "phone", "mobile", "iphone", "android");
            case "Bottles": return containsAny(reportText, "bottles", "bottle", "water bottle", "flask");
            default: return containsNormalized(report.category, category) || containsNormalized(report.itemName, category) || containsNormalized(report.title, category);
        }
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(normalize(keyword))) return true;
        }
        return false;
    }

    private boolean containsNormalized(String value, String query) {
        return normalize(value).contains(query);
    }

    private String getSearchQuery() {
        if (etSearch == null || etSearch.getText() == null) return "";
        return normalize(etSearch.getText().toString());
    }

    private String getEmptyStateText(boolean hasSearch, boolean hasCategory) {
        if (hasSearch) return "No matching reports found";
        if (hasCategory) return "No reports found in this category";
        return "No recent reports";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    private TextView makeTextView(String text, int colorRes, int textSizeSp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, colorRes));
        tv.setTextSize(textSizeSp);
        if (bold) tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return tv;
    }

    private String getDocumentString(DocumentSnapshot document, String field, String fallback) {
        Object rawValue = document.get(field);
        if (rawValue == null) return fallback;
        String value = String.valueOf(rawValue).trim();
        return value.isEmpty() ? fallback : value;
    }

    private String firstDocumentString(DocumentSnapshot document, String... fields) {
        for (String field : fields) {
            String value = getDocumentString(document, field, "");
            if (!value.isEmpty()) return value;
        }
        return "";
    }

    private long getCreatedAtMillis(DocumentSnapshot document) {
        try {
            Object ts = document.get("createdAt");
            if (ts != null) {
                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) ts;
                return timestamp.toDate().getTime();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse createdAt timestamp", e);
        }
        return 0L;
    }

    private String normalizeStatus(String status) {
        return "Found".equalsIgnoreCase(status) ? "Found" : "Lost";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class RecentReportItem {
        final String documentId;
        final String sourceCollection;
        final String title;
        final String itemName;
        final String status;
        final String location;
        final String imageUrl;
        final String category;
        final String description;
        final long createdAtMillis;

        RecentReportItem(
                String documentId,
                String sourceCollection,
                String title,
                String itemName,
                String status,
                String location,
                String imageUrl,
                String category,
                String description,
                long createdAtMillis
        ) {
            this.documentId = documentId;
            this.sourceCollection = sourceCollection;
            this.title = title == null ? "" : title;
            this.itemName = itemName;
            this.status = status;
            this.location = location;
            this.imageUrl = imageUrl == null ? "" : imageUrl.trim();
            this.category = category == null ? "" : category;
            this.description = description == null ? "" : description;
            this.createdAtMillis = createdAtMillis;
        }
    }
}
