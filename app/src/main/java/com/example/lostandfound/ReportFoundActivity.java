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
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ReportFoundActivity extends AppCompatActivity {

    private TextInputEditText etFinderName, etFinderPhone;
    private TextInputEditText etFoundItemName, etFoundCategory, etFoundLocation, etFoundDescription;
    private Button btnBackHomeFound;
    private Button btnSubmitFound;
    private LinearLayout btnUploadFoundPhoto;
    private ImageView imgSelectedFoundItem;

    private Uri selectedImageUri = null;
    private Bitmap selectedBitmap = null;
    private FirebaseFirestore db;

    private ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    selectedBitmap = null;
                    imgSelectedFoundItem.setImageURI(uri);
                    imgSelectedFoundItem.setVisibility(ImageView.VISIBLE);
                }
            });

    private ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    selectedBitmap = bitmap;
                    selectedImageUri = null;
                    imgSelectedFoundItem.setImageBitmap(bitmap);
                    imgSelectedFoundItem.setVisibility(ImageView.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_found);

        db = FirestoreProvider.getFirestore();

        btnBackHomeFound = findViewById(R.id.btnBackHomeFound);
        btnSubmitFound = findViewById(R.id.btnSubmitFound);
        btnUploadFoundPhoto = findViewById(R.id.btnUploadFoundPhoto);
        imgSelectedFoundItem = findViewById(R.id.imgSelectedFoundItem);

        etFinderName = findViewById(R.id.etFinderName);
        etFinderPhone = findViewById(R.id.etFinderPhone);
        etFoundItemName = findViewById(R.id.etFoundItemName);
        etFoundCategory = findViewById(R.id.etFoundCategory);
        etFoundLocation = findViewById(R.id.etFoundLocation);
        etFoundDescription = findViewById(R.id.etFoundDescription);

        if (btnBackHomeFound != null) {
            btnBackHomeFound.setOnClickListener(v -> finish());
        }

        if (btnUploadFoundPhoto != null) {
            btnUploadFoundPhoto.setOnClickListener(v -> showImageOptions());
        }

        if (btnSubmitFound != null) {
            btnSubmitFound.setOnClickListener(v -> submitFoundItem());
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

    private void submitFoundItem() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to submit a report", Toast.LENGTH_SHORT).show();
            return;
        }

        String itemName = getInputText(etFoundItemName);
        String category = getInputText(etFoundCategory);
        String location = getInputText(etFoundLocation);

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

        btnSubmitFound.setEnabled(false);

        String contactName = getInputText(etFinderName);
        if (contactName.isEmpty()) {
            contactName = cleanText(currentUser.getDisplayName());
        }
        if (contactName.isEmpty()) {
            contactName = "Finder / Student Services";
        }

        String reporterEmail = cleanText(currentUser.getEmail());
        String contactPhone = getInputText(etFinderPhone);
        if (contactPhone.isEmpty()) {
            contactPhone = "Available through university office";
        }

        DocumentReference reportRef = db.collection("found_reports").document();

        Map<String, Object> report = new HashMap<>();
        report.put("reportId", reportRef.getId());
        report.put("title", itemName);
        report.put("itemName", itemName);
        report.put("category", category);
        report.put("description", getInputText(etFoundDescription));
        report.put("location", location);
        report.put("date", getCurrentDate());
        report.put("time", getCurrentTime());
        report.put("status", "Found");
        report.put("reporterEmail", reporterEmail);
        report.put("contactName", contactName);
        report.put("contactEmail", reporterEmail);
        report.put("contactPhone", contactPhone);
        report.put("imageUrl", "");
        report.put("createdAt", FieldValue.serverTimestamp());
        report.put("userId", currentUser.getUid());

        saveReportWithImage(reportRef, report);
    }

    private void saveReportWithImage(DocumentReference reportRef, Map<String, Object> report) {
        if (selectedImageUri == null && selectedBitmap == null) {
            report.put("imageUrl", "");
            saveReport(reportRef, report);
            return;
        }

        // Handle bitmap (from camera) - convert to JPEG
        if (selectedBitmap != null) {
            uploadBitmapImage(reportRef, report, selectedBitmap);
            return;
        }

        // Handle URI (from gallery) - check MIME type first
        String mimeType = getContentResolver().getType(selectedImageUri);
        Log.d("FirestoreDebug", "Selected image Uri: " + selectedImageUri);
        Log.d("FirestoreDebug", "Selected image MIME type: " + mimeType);

        if (!isSupportedImageMimeType(mimeType)) {
            Log.e("FirestoreDebug", "Unsupported image MIME type: " + mimeType);
            Toast.makeText(this, "Unsupported image type. Report saved without image.", Toast.LENGTH_SHORT).show();
            report.put("imageUrl", "");
            saveReport(reportRef, report);
            return;
        }

        // Try to upload the image from URI
        uploadImageFromUri(reportRef, report, selectedImageUri);
    }

    private void uploadBitmapImage(DocumentReference reportRef, Map<String, Object> report, Bitmap bitmap) {
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("report_images/found/" + reportRef.getId() + ".jpg");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);

        imageRef.putBytes(outputStream.toByteArray())
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    String url = downloadUri.toString();
                    Log.d("FirestoreDebug", "Image upload successful. URL: " + url);
                    report.put("imageUrl", url);
                    saveReport(reportRef, report);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Image upload failed, saving without image", e);
                    Toast.makeText(this, "Image upload failed. Report saved without image.", Toast.LENGTH_SHORT).show();
                    report.put("imageUrl", "");
                    saveReport(reportRef, report);
                });
    }

    private void uploadImageFromUri(DocumentReference reportRef, Map<String, Object> report, Uri imageUri) {
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("report_images/found/" + reportRef.getId() + ".jpg");

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            if (bitmap == null) {
                Log.e("FirestoreDebug", "Failed to decode image URI to bitmap: " + imageUri);
                Toast.makeText(this, "Unsupported image type. Report saved without image.", Toast.LENGTH_SHORT).show();
                report.put("imageUrl", "");
                saveReport(reportRef, report);
                return;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            byte[] jpegBytes = outputStream.toByteArray();

            com.google.firebase.storage.StorageMetadata metadata =
                    new com.google.firebase.storage.StorageMetadata.Builder()
                            .setContentType("image/jpeg")
                            .build();

            imageRef.putBytes(jpegBytes, metadata)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUri -> {
                        String url = downloadUri.toString();
                        Log.d("FirestoreDebug", "Image upload successful. URL: " + url);
                        report.put("imageUrl", url);
                        saveReport(reportRef, report);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreDebug", "Image upload failed, saving without image", e);
                        Toast.makeText(this, "Image upload failed. Report saved without image.", Toast.LENGTH_SHORT).show();
                        report.put("imageUrl", "");
                        saveReport(reportRef, report);
                    });
        } catch (Exception e) {
            Log.e("FirestoreDebug", "Failed to read or upload image URI, saving without image", e);
            Toast.makeText(this, "Unsupported image type or read error. Report saved without image.", Toast.LENGTH_SHORT).show();
            report.put("imageUrl", "");
            saveReport(reportRef, report);
        }
    }

    private boolean isSupportedImageMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        Set<String> supportedTypes = new HashSet<>(Arrays.asList(
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/webp",
                "image/heic",
                "image/heif"
        ));
        return supportedTypes.contains(mimeType.toLowerCase());
    }

    private void saveReport(DocumentReference reportRef, Map<String, Object> report) {
        reportRef.set(report)
                .addOnSuccessListener(unused -> {
                    btnSubmitFound.setEnabled(true);
                    String docId = reportRef.getId();
                    Log.d("FirestoreDebug", "Found report saved successfully. Document ID: " + docId);
                    Toast.makeText(this, "Found report submitted successfully", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Saved to Firestore document ID: " + docId, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitFound.setEnabled(true);
                    Log.e("FirestoreDebug", "Found report save failed", e);
                    Toast.makeText(this, "Failed to save report", Toast.LENGTH_SHORT).show();
                });
    }

    private String getInputText(TextInputEditText input) {
        if (input == null || input.getText() == null) {
            return "";
        }
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
