package com.example.lostandfound;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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

        db = FirebaseFirestore.getInstance();

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

        btnSubmitLost.setEnabled(false);

        Map<String, Object> report = new HashMap<>();
        report.put("itemName", itemName);
        report.put("category", category);
        report.put("description", getInputText(etDescription));
        report.put("location", location);
        report.put("date", date);
        report.put("time", getCurrentTime());
        report.put("status", "Lost");
        report.put("createdAt", FieldValue.serverTimestamp());

        db.collection("lost_reports")
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Lost report saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitLost.setEnabled(true);
                    Toast.makeText(this, "Failed to save report", Toast.LENGTH_SHORT).show();
                });
    }

    private String getInputText(TextInputEditText input) {
        if (input == null || input.getText() == null) {
            return "";
        }
        return input.getText().toString().trim();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
}
