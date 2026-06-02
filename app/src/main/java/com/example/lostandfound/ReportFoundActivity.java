package com.example.lostandfound;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ReportFoundActivity extends AppCompatActivity {

    TextView btnBackHomeFound;
    Button btnSubmitFound;

    TextInputEditText etFinderName, etFinderPhone;
    TextInputEditText etFoundItemName, etFoundCategory, etFoundLocation, etFoundDescription;

    LinearLayout btnUploadFoundPhoto;
    ImageView imgSelectedFoundItem;

    Uri selectedImageUri = null;
    Bitmap selectedBitmap = null;

    ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    selectedBitmap = null;
                    imgSelectedFoundItem.setImageURI(uri);
                    imgSelectedFoundItem.setVisibility(ImageView.VISIBLE);
                    Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show();
                }
            });

    ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    selectedBitmap = bitmap;
                    selectedImageUri = null;
                    imgSelectedFoundItem.setImageBitmap(bitmap);
                    imgSelectedFoundItem.setVisibility(ImageView.VISIBLE);
                    Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_found);

        btnBackHomeFound = findViewById(R.id.btnBackHomeFound);
        btnSubmitFound = findViewById(R.id.btnSubmitFound);

        etFinderName = findViewById(R.id.etFinderName);
        etFinderPhone = findViewById(R.id.etFinderPhone);
        etFoundItemName = findViewById(R.id.etFoundItemName);
        etFoundCategory = findViewById(R.id.etFoundCategory);
        etFoundLocation = findViewById(R.id.etFoundLocation);
        etFoundDescription = findViewById(R.id.etFoundDescription);

        btnUploadFoundPhoto = findViewById(R.id.btnUploadFoundPhoto);
        imgSelectedFoundItem = findViewById(R.id.imgSelectedFoundItem);

        btnBackHomeFound.setOnClickListener(v -> {
            Intent intent = new Intent(ReportFoundActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnUploadFoundPhoto.setOnClickListener(v -> showImageOptions());

        btnSubmitFound.setOnClickListener(v -> submitFoundItem());
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
        String finderName = getText(etFinderName);
        String finderPhone = getText(etFinderPhone);
        String itemName = getText(etFoundItemName);
        String category = getText(etFoundCategory);
        String location = getText(etFoundLocation);
        String description = getText(etFoundDescription);

        if (finderName.isEmpty() ||
                finderPhone.isEmpty() ||
                itemName.isEmpty() ||
                category.isEmpty() ||
                location.isEmpty() ||
                description.isEmpty()) {

            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null && selectedBitmap == null) {
            Toast.makeText(this, "Please upload an item photo", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Found item submitted successfully", Toast.LENGTH_LONG).show();
        finish();
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}