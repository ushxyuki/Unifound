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

public class ReportFoundActivity extends AppCompatActivity {

    private TextInputEditText etFoundItemName, etFoundCategory, etFoundLocation, etFoundDescription;
    private Button btnBackHomeFound;
    private Button btnSubmitFound;
    private LinearLayout btnUploadFoundPhoto;
    private ImageView imgSelectedFoundItem;

    private Uri selectedImageUri = null;
    private Bitmap selectedBitmap = null;

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
        String itemName = etFoundItemName.getText().toString().trim();
        if (itemName.isEmpty()) {
            Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Found report submitted to Lost & Found Office", Toast.LENGTH_LONG).show();
        finish();
    }
}