package com.example.lostandfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String DEFAULT_NAME = "UniFound User";
    private static final String PROFILE_PREFS = "profile_details";
    private static final String KEY_STUDENT_ID = "studentId";
    private static final String KEY_COURSE = "course";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SharedPreferences profilePrefs;

    private TextView tvProfileInitialLarge;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvStudentId;
    private TextView tvCourse;
    private TextView tvLostReportsCount;
    private TextView tvFoundReportsCount;
    private EditText etProfileStudentId;
    private EditText etProfileCourse;
    private Button btnEditProfileDetails;
    private Button btnSaveProfileDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirestoreProvider.getFirestore();
        profilePrefs = getSharedPreferences(PROFILE_PREFS, MODE_PRIVATE);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }

        bindViews();
        setupBottomNav();
        showFirebaseUser(currentUser);
        loadCachedProfile(currentUser.getUid());
        loadUserProfile(currentUser);
        loadReportCounts(currentUser.getUid());
        switchToDisplayMode();

        Button btnBackProfile = findViewById(R.id.btnBackProfile);
        if (btnBackProfile != null) {
            btnBackProfile.setOnClickListener(v -> finish());
        }

        if (btnEditProfileDetails != null) {
            btnEditProfileDetails.setOnClickListener(v -> switchToEditMode());
        }

        if (btnSaveProfileDetails != null) {
            btnSaveProfileDetails.setOnClickListener(v -> saveProfileDetails(currentUser));
        }

        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                auth.signOut();
                Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                goToLogin();
            });
        }
    }

    private void bindViews() {
        tvProfileInitialLarge = findViewById(R.id.tvProfileInitialLarge);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvCourse = findViewById(R.id.tvCourse);
        tvLostReportsCount = findViewById(R.id.tvLostReportsCount);
        tvFoundReportsCount = findViewById(R.id.tvFoundReportsCount);
        etProfileStudentId = findViewById(R.id.etProfileStudentId);
        etProfileCourse = findViewById(R.id.etProfileCourse);
        btnEditProfileDetails = findViewById(R.id.btnEditProfileDetails);
        btnSaveProfileDetails = findViewById(R.id.btnSaveProfileDetails);
    }

    private void showFirebaseUser(FirebaseUser currentUser) {
        String name = cleanText(currentUser.getDisplayName());
        if (name.isEmpty()) {
            name = DEFAULT_NAME;
        }

        setUserName(name);

        String email = cleanText(currentUser.getEmail());
        if (email.isEmpty()) {
            email = "Email not available";
        }
        tvUserEmail.setText(email);

        tvStudentId.setText("Student ID: Not added");
        tvCourse.setText("Course: Not added");
        tvLostReportsCount.setText("Lost Reports: 0");
        tvFoundReportsCount.setText("Found Reports: 0");
    }

    private void loadUserProfile(FirebaseUser currentUser) {
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = getDocumentString(documentSnapshot, "fullName");
                    if (name.isEmpty()) {
                        name = cleanText(currentUser.getDisplayName());
                    }
                    if (name.isEmpty()) {
                        name = DEFAULT_NAME;
                    }
                    setUserName(name);

                    String cachedStudentId = getCachedValue(currentUser.getUid(), KEY_STUDENT_ID);
                    String cachedCourse = getCachedValue(currentUser.getUid(), KEY_COURSE);

                    String studentId = firstDocumentString(
                            documentSnapshot,
                            "studentId",
                            "studentID",
                            "student_id",
                            "studentNumber"
                    );
                    if (studentId.isEmpty()) {
                        studentId = cachedStudentId;
                    }

                    String course = firstDocumentString(documentSnapshot, "course", "courseName");
                    if (course.isEmpty()) {
                        course = cachedCourse;
                    }

                    applyProfileDetails(studentId, course);
                    if (!studentId.isEmpty() || !course.isEmpty()) {
                        saveCachedProfile(currentUser.getUid(), studentId, course);
                    }
                })
                .addOnFailureListener(e -> {
                    loadCachedProfile(currentUser.getUid());
                });
    }

    private void saveProfileDetails(FirebaseUser currentUser) {
        String studentId = cleanText(etProfileStudentId.getText() == null
                ? ""
                : etProfileStudentId.getText().toString());
        String course = cleanText(etProfileCourse.getText() == null
                ? ""
                : etProfileCourse.getText().toString());

        if (studentId.isEmpty()) {
            Toast.makeText(ProfileActivity.this, "Please enter Student ID", Toast.LENGTH_SHORT).show();
            return;
        }
        if (course.isEmpty()) {
            Toast.makeText(ProfileActivity.this, "Please enter Course", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = cleanText(tvUserName.getText() == null
                ? ""
                : tvUserName.getText().toString());
        if (fullName.isEmpty()) {
            fullName = DEFAULT_NAME;
        }

        btnSaveProfileDetails.setEnabled(false);
        saveCachedProfile(currentUser.getUid(), studentId, course);
        applyProfileDetails(studentId, course);

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("userId", currentUser.getUid());
        userUpdates.put("fullName", fullName);
        userUpdates.put("email", cleanText(currentUser.getEmail()));
        userUpdates.put("studentId", studentId);
        userUpdates.put("course", course);
        userUpdates.put("role", "user");
        userUpdates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(currentUser.getUid())
                .set(userUpdates, SetOptions.merge())
                .addOnSuccessListener(avoided -> {
                    btnSaveProfileDetails.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Profile details updated", Toast.LENGTH_SHORT).show();
                    switchToDisplayMode();
                })
                .addOnFailureListener(e -> {
                    btnSaveProfileDetails.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Failed to update profile details", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCachedProfile(String userId) {
        applyProfileDetails(
                getCachedValue(userId, KEY_STUDENT_ID),
                getCachedValue(userId, KEY_COURSE)
        );
    }

    private void saveCachedProfile(String userId, String studentId, String course) {
        profilePrefs.edit()
                .putString(createProfileKey(userId, KEY_STUDENT_ID), studentId)
                .putString(createProfileKey(userId, KEY_COURSE), course)
                .apply();
    }

    private String getCachedValue(String userId, String fieldName) {
        return cleanText(profilePrefs.getString(createProfileKey(userId, fieldName), ""));
    }

    private String createProfileKey(String userId, String fieldName) {
        return userId + "_" + fieldName;
    }

    private void applyProfileDetails(String studentId, String course) {
        tvStudentId.setText(studentId.isEmpty()
                ? "Student ID: Not added"
                : "Student ID: " + studentId);
        etProfileStudentId.setText(studentId);

        tvCourse.setText(course.isEmpty()
                ? "Course: Not added"
                : "Course: " + course);
        etProfileCourse.setText(course);
    }

    private void switchToEditMode() {
        tvStudentId.setVisibility(View.GONE);
        tvCourse.setVisibility(View.GONE);
        btnEditProfileDetails.setVisibility(View.GONE);

        etProfileStudentId.setVisibility(View.VISIBLE);
        etProfileCourse.setVisibility(View.VISIBLE);
        btnSaveProfileDetails.setVisibility(View.VISIBLE);
    }

    private void switchToDisplayMode() {
        tvStudentId.setVisibility(View.VISIBLE);
        tvCourse.setVisibility(View.VISIBLE);
        btnEditProfileDetails.setVisibility(View.VISIBLE);

        etProfileStudentId.setVisibility(View.GONE);
        etProfileCourse.setVisibility(View.GONE);
        btnSaveProfileDetails.setVisibility(View.GONE);
    }

    private void loadReportCounts(String userId) {
        loadReportCount("lost_reports", "Lost Reports", tvLostReportsCount, userId);
        loadReportCount("found_reports", "Found Reports", tvFoundReportsCount, userId);
    }

    private void loadReportCount(String collectionName, String label, TextView targetView, String userId) {
        db.collection(collectionName)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        targetView.setText(label + ": " + queryDocumentSnapshots.size()))
                .addOnFailureListener(e -> targetView.setText(label + ": 0"));
    }

    private void setUserName(String name) {
        tvUserName.setText(name);
        tvProfileInitialLarge.setText(createInitials(name));
    }

    private String firstDocumentString(DocumentSnapshot documentSnapshot, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = getDocumentString(documentSnapshot, fieldName);
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String getDocumentString(DocumentSnapshot documentSnapshot, String fieldName) {
        if (documentSnapshot == null || !documentSnapshot.exists()) {
            return "";
        }

        Object value = documentSnapshot.get(fieldName);
        if (value == null) {
            return "";
        }

        return cleanText(String.valueOf(value));
    }

    private String cleanText(String value) {
        return value == null ? "" : value.trim();
    }

    private String createInitials(String name) {
        String cleanName = cleanText(name);
        if (cleanName.isEmpty()) {
            return "UF";
        }

        String[] parts = cleanName.split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.substring(0, 1));
            }
            if (initials.length() == 2) {
                break;
            }
        }

        if (initials.length() == 1 && parts[0].length() > 1) {
            initials.append(parts[0].substring(1, 2));
        }

        return initials.toString().toUpperCase(Locale.US);
    }

    private void setupBottomNav() {
        BottomNavHelper.setup(this, BottomNavHelper.Tab.PROFILE);
    }

    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
