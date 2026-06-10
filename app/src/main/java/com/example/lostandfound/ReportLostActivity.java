package com.example.lostandfound;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportLostActivity extends AppCompatActivity {

    private TextInputEditText etStudentId, etEmail, etFullName;
    private TextInputEditText etItemName, etCategory, etDateLost, etLocation, etDescription;

    private Button btnBackHome;
    private Button btnSubmitLost;
    private LinearLayout btnUploadPhoto;
    private ImageView imgSelectedItem;

    private Uri selectedImageUri = null;
    private Bitmap selectedBitmap = null;
    private FirebaseFirestore db;

    private ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    selectedBitmap = null;
                    imgSelectedItem.setImageURI(uri);
                    imgSelectedItem.setVisibility(View.VISIBLE);
                }
            });

    private ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    selectedBitmap = bitmap;
                    selectedImageUri = null;
                    imgSelectedItem.setImageBitmap(bitmap);
                    imgSelectedItem.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost);

        db = FirestoreProvider.getFirestore();

        btnBackHome = findViewById(R.id.btnBackHome);
        btnSubmitLost = findViewById(R.id.btnSubmitLost);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        imgSelectedItem = findViewById(R.id.imgSelectedItem);

        etStudentId = findViewById(R.id.etStudentId);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etItemName = findViewById(R.id.etItemName);
        etCategory = findViewById(R.id.etCategory);
        etDateLost = findViewById(R.id.etDateLost);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);

        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(v -> finish());
        }

        if (btnUploadPhoto != null) {
            btnUploadPhoto.setOnClickListener(v -> showImageOptions());
        }

        if (btnSubmitLost != null) {
            btnSubmitLost.setOnClickListener(v -> submitLostItem());
        }
    }

    private void showImageOptions() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Upload Item Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        cameraLauncher.launch(null);
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void submitLostItem() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to submit a report", Toast.LENGTH_SHORT).show();
            return;
        }

        String itemName = getInputText(etItemName);
        String category = getInputText(etCategory);
        String location = getInputText(etLocation);
        String date = getInputText(etDateLost);

        if (itemName.isEmpty()) {
            Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (category.isEmpty()) {
            Toast.makeText(this, "Please enter category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (location.isEmpty()) {
            Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.isEmpty()) {
            date = getCurrentDate();
        }

        // Disable submit button to prevent double-tap
        btnSubmitLost.setEnabled(false);

        String enteredEmail = getInputText(etEmail);
        String reporterEmail = enteredEmail.isEmpty() ? cleanText(currentUser.getEmail()) : enteredEmail;
        String contactName = getInputText(etFullName);
        if (contactName.isEmpty()) {
            contactName = cleanText(currentUser.getDisplayName());
        }
        if (contactName.isEmpty()) {
            contactName = "Student Services";
        }

        DocumentReference reportRef = db.collection("lost_reports").document();

        Map<String, Object> report = new HashMap<>();
        report.put("reportId", reportRef.getId());
        report.put("itemName", itemName);
        report.put("category", category);
        report.put("location", location);
        report.put("date", date);
        report.put("description", getInputText(etDescription));
        report.put("status", "Lost");
        report.put("userId", currentUser.getUid());
        report.put("reporterEmail", reporterEmail);
        report.put("createdAt", FieldValue.serverTimestamp());
        report.put("imageUrl", ""); // Default empty

        // Logic for image handling
        processImageAndSave(reportRef, report);
    }

    private void processImageAndSave(DocumentReference reportRef, Map<String, Object> report) {
        if (selectedBitmap != null) {
            // Camera image
            uploadImageBytes(reportRef, report, selectedBitmap);
        } else if (selectedImageUri != null) {
            // Gallery image
            String mimeType = getContentResolver().getType(selectedImageUri);
            Log.d("FirestoreDebug", "Selected gallery URI: " + selectedImageUri + " MIME: " + mimeType);
            
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    uploadImageBytes(reportRef, report, bitmap);
                } else {
                    Log.e("FirestoreDebug", "Failed to decode gallery URI into bitmap");
                    Toast.makeText(this, "Unsupported image type. Report saved without image.", Toast.LENGTH_SHORT).show();
                    saveReport(reportRef, report, "");
                }
            } catch (Exception e) {
                Log.e("FirestoreDebug", "Error reading gallery image URI", e);
                Toast.makeText(this, "Image upload failed. Report saved without image.", Toast.LENGTH_SHORT).show();
                saveReport(reportRef, report, "");
            }
        } else {
            // No image
            saveReport(reportRef, report, "");
        }
    }

    private void uploadImageBytes(DocumentReference reportRef, Map<String, Object> report, Bitmap bitmap) {
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("report_images/lost/" + reportRef.getId() + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        byte[] data = baos.toByteArray();

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        imageRef.putBytes(data, metadata)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    String url = uri.toString();
                    Log.d("FirestoreDebug", "Image upload successful. URL: " + url);
                    saveReport(reportRef, report, url);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Image upload failed, saving without image", e);
                    Toast.makeText(this, "Image upload failed. Report saved without image.", Toast.LENGTH_SHORT).show();
                    saveReport(reportRef, report, "");
                });
    }

    private void saveReport(DocumentReference reportRef, Map<String, Object> report, String imageUrl) {
        report.put("imageUrl", imageUrl);
        reportRef.set(report)
                .addOnSuccessListener(unused -> {
                    Log.d("FirestoreDebug", "Report saved successfully. ID: " + reportRef.getId());
                    Toast.makeText(this, "Lost report submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Firestore save failed", e);
                    btnSubmitLost.setEnabled(true); // Re-enable only on Firestore failure
                    Toast.makeText(this, "Failed to save report", Toast.LENGTH_SHORT).show();
                });
    }

    private String getInputText(TextInputEditText input) {
        if (input == null || input.getText() == null) return "";
        return input.getText().toString().trim();
    }

    private String cleanText(String value) {
        return value == null ? "" : value.trim();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
}
