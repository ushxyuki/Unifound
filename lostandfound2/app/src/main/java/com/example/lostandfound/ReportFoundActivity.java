package com.example.lostandfound;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReportFoundActivity extends AppCompatActivity {

    Button btnBackHomeFound, btnSubmitFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_found);

        // Back Button
        btnBackHomeFound = findViewById(R.id.btnBackHomeFound);

        btnBackHomeFound.setOnClickListener(v -> {
            finish();
        });

        // Submit Button
        btnSubmitFound = findViewById(R.id.btnSubmitFound);

        btnSubmitFound.setOnClickListener(v ->
                Toast.makeText(this,
                        "Found Item Submitted Successfully",
                        Toast.LENGTH_SHORT).show());
    }
}
