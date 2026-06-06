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

public class ReportFoundActivity extends AppCompatActivity {

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

        db = FirebaseFirestore.getInstance();

        btnBackHomeFound = findViewById(R.id.btnBackHomeFound);
        btnSubmitFound = findViewById(R.id.btnSubmitFound);
        btnUploadFoundPhoto = findViewById(R.id.btnUploadFoundPhoto);
        imgSelectedFoundItem = findViewById(R.id.imgSelectedFoundItem);

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

        Map<String, Object> report = new HashMap<>();
        report.put("itemName", itemName);
        report.put("category", category);
        report.put("description", getInputText(etFoundDescription));
        report.put("location", location);
        report.put("date", getCurrentDate());
        report.put("time", getCurrentTime());
        report.put("status", "Found");
        report.put("createdAt", FieldValue.serverTimestamp());

        db.collection("found_reports")
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Found report saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitFound.setEnabled(true);
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
