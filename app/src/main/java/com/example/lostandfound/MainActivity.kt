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
            startActivity(Intent(this, ReportLostActivity::class.java))
        }

        cardReportFound.setOnClickListener {
            startActivity(Intent(this, ReportFoundActivity::class.java))
        }

        btnBrowse.setOnClickListener {
            startActivity(Intent(this, ViewReportsActivity::class.java))
        }
    }
}