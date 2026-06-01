package com.example.lostandfound;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ReportLostActivity extends AppCompatActivity {

    TextInputEditText etStudentId, etEmail, etFullName;
    TextInputEditText etItemName, etCategory, etDateLost, etLocation, etDescription;

    Button btnSubmitLost;
    TextView btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost);

        btnBackHome = findViewById(R.id.btnBackHome);
        btnSubmitLost = findViewById(R.id.btnSubmitLost);

        etStudentId = findViewById(R.id.etStudentId);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);

        etItemName = findViewById(R.id.etItemName);
        etCategory = findViewById(R.id.etCategory);
        etDateLost = findViewById(R.id.etDateLost);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);

        btnBackHome.setOnClickListener(v -> finish());

        btnSubmitLost.setOnClickListener(v -> {

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

            } else {
                Toast.makeText(this, "Lost item submitted successfully", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}