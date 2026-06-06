package com.example.lostandfound;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        setupBottomNav();
        
        Button btnBackMessages = findViewById(R.id.btnBackMessages);
        if (btnBackMessages != null) {
            btnBackMessages.setOnClickListener(v -> finish());
        }
    }

    private void setupBottomNav() {
        BottomNavHelper.setup(this, BottomNavHelper.Tab.MESSAGES);
    }
}
