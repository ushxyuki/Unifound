package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private lateinit var cardReportLost: MaterialCardView
    private lateinit var cardReportFound: MaterialCardView
    private lateinit var btnBrowse: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardReportLost = findViewById(R.id.cardReportLost)
        cardReportFound = findViewById(R.id.cardReportFound)
        btnBrowse = findViewById(R.id.btnBrowse)

        cardReportLost.setOnClickListener {
            val intent = Intent(this, ReportLostActivity::class.java)
            startActivity(intent)
        }

        cardReportFound.setOnClickListener {
            val intent = Intent(this, ReportFoundActivity::class.java)
            startActivity(intent)
        }

        btnBrowse.setOnClickListener {
            val intent = Intent(this, ViewReportsActivity::class.java)
            startActivity(intent)
        }
    }
}