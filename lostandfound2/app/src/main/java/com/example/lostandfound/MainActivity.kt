package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Lost Card
        val cardReportLost = findViewById<MaterialCardView>(R.id.cardReportLost)

        cardReportLost.setOnClickListener {
            val intent = Intent(this, ReportLostActivity::class.java)
            startActivity(intent)
        }

        // Found Card
        val cardReportFound = findViewById<MaterialCardView>(R.id.cardReportFound)

        cardReportFound.setOnClickListener {
            val intent = Intent(this, ReportFoundActivity::class.java)
            startActivity(intent)
        }
    }
}