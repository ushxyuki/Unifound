package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private var cardReportLost: MaterialCardView? = null
    private var cardReportFound: MaterialCardView? = null
    private var tvProfileInitial: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Null-safe findViewById lookups
        cardReportLost = findViewById(R.id.cardReportLost)
        cardReportFound = findViewById(R.id.cardReportFound)
        tvProfileInitial = findViewById(R.id.tvProfileInitial)

        cardReportLost?.setOnClickListener {
            startActivity(Intent(this, ReportLostActivity::class.java))
        }

        cardReportFound?.setOnClickListener {
            startActivity(Intent(this, ReportFoundActivity::class.java))
        }

        tvProfileInitial?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        BottomNavHelper.setup(this, BottomNavHelper.Tab.HOME)
    }
}
