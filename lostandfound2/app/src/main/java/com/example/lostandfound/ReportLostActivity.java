package com.example.lostandfound;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReportLostActivity extends AppCompatActivity {

    EditText etStudentId, etEmail, etFullName, etPhone;
    EditText etItemName, etCategory, etDateLost, etLocation, etDescription;

    Button btnSubmitLost, btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost);

        // Back Button
        btnBackHome = findViewById(R.id.btnBackHome);

        btnBackHome.setOnClickListener(v -> {
            finish();
        });

        // EditTexts
        etStudentId = findViewById(R.id.etStudentId);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);

        etItemName = findViewById(R.id.etItemName);
        etCategory = findViewById(R.id.etCategory);
        etDateLost = findViewById(R.id.etDateLost);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);

        // Submit Button
        btnSubmitLost = findViewById(R.id.btnSubmitLost);

        btnSubmitLost.setOnClickListener(v -> {

            String studentId = etStudentId.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            String itemName = etItemName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String dateLost = etDateLost.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (studentId.isEmpty() ||
                    email.isEmpty() ||
                    fullName.isEmpty() ||
                    phone.isEmpty() ||
                    itemName.isEmpty() ||
                    category.isEmpty() ||
                    dateLost.isEmpty() ||
                    location.isEmpty() ||
                    description.isEmpty()) {

                Toast.makeText(this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this,
                        "Lost Item Submitted Successfully",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
