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

public class ReportLostActivity extends AppCompatActivity {

    TextInputEditText etStudentId, etEmail, etFullName;
    TextInputEditText etItemName, etCategory, etDateLost, etLocation, etDescription;

    Button btnBackHome, btnSubmitLost;
    LinearLayout btnUploadPhoto;
    ImageView imgSelectedItem;

    Uri selectedImageUri = null;
    Bitmap selectedBitmap = null;

    ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    selectedBitmap = null;

                    imgSelectedItem.setImageURI(uri);
                    imgSelectedItem.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show();
                }
            });

    ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    selectedBitmap = bitmap;
                    selectedImageUri = null;

                    imgSelectedItem.setImageBitmap(bitmap);
                    imgSelectedItem.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost);

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

        btnBackHome.setOnClickListener(v -> finish());

        btnUploadPhoto.setOnClickListener(v -> showImageOptions());

        btnSubmitLost.setOnClickListener(v -> submitLostItem());
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
        String studentId = getText(etStudentId);
        String email = getText(etEmail);
        String fullName = getText(etFullName);
        String itemName = getText(etItemName);
        String category = getText(etCategory);
        String dateLost = getText(etDateLost);
        String location = getText(etLocation);
        String description = getText(etDescription);

        if (studentId.isEmpty() ||
                email.isEmpty() ||
                fullName.isEmpty() ||
                itemName.isEmpty() ||
                category.isEmpty() ||
                dateLost.isEmpty() ||
                location.isEmpty() ||
                description.isEmpty()) {

            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null && selectedBitmap == null) {
            Toast.makeText(this, "Please upload an item photo", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Lost report submitted to Lost & Found Office", Toast.LENGTH_LONG).show();

        finish();
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}