package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private var cardReportLost: MaterialCardView? = null
    private var cardReportFound: MaterialCardView? = null
    private var btnBrowse: Button? = null
    private var btnMessages: Button? = null
    private var btnMyReports: Button? = null
    private var tvProfileInitial: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Null-safe findViewById lookups
        cardReportLost = findViewById(R.id.cardReportLost)
        cardReportFound = findViewById(R.id.cardReportFound)
        btnBrowse = findViewById(R.id.btnBrowse)
        btnMessages = findViewById(R.id.btnMessages)
        btnMyReports = findViewById(R.id.btnMyReports)
        tvProfileInitial = findViewById(R.id.tvProfileInitial)

        cardReportLost?.setOnClickListener {
            startActivity(Intent(this, ReportLostActivity::class.java))
        }

        cardReportFound?.setOnClickListener {
            startActivity(Intent(this, ReportFoundActivity::class.java))
        }

        btnBrowse?.setOnClickListener {
            startActivity(Intent(this, ViewReportsActivity::class.java))
        }

        btnMessages?.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }

        btnMyReports?.setOnClickListener {
            startActivity(Intent(this, MyReportsActivity::class.java))
        }

        tvProfileInitial?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setupBottomNav()
    }

    private fun setupBottomNav() {
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navReports = findViewById<LinearLayout>(R.id.navReports)
        val navMessages = findViewById<LinearLayout>(R.id.navMessages)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)

        // Highlight Home
        highlightTab(R.id.navHome)

        navHome?.setOnClickListener { /* Already here */ }

        navReports?.setOnClickListener {
            startActivity(Intent(this, ViewReportsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }

        navMessages?.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }

        navProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
    }

    private fun highlightTab(tabId: Int) {
        val pill = when (tabId) {
            R.id.navHome -> findViewById<LinearLayout>(R.id.navHomePill)
            R.id.navReports -> findViewById<LinearLayout>(R.id.navReportsPill)
            R.id.navMessages -> findViewById<LinearLayout>(R.id.navMessagesPill)
            R.id.navProfile -> findViewById<LinearLayout>(R.id.navProfilePill)
            else -> null
        }

        val label = when (tabId) {
            R.id.navHome -> findViewById<TextView>(R.id.navHomeLabel)
            R.id.navReports -> findViewById<TextView>(R.id.navReportsLabel)
            R.id.navMessages -> findViewById<TextView>(R.id.navMessagesLabel)
            R.id.navProfile -> findViewById<TextView>(R.id.navProfileLabel)
            else -> null
        }

        val icon = when (tabId) {
            R.id.navHome -> findViewById<ImageView>(R.id.navHomeIcon)
            R.id.navReports -> findViewById<ImageView>(R.id.navReportsIcon)
            R.id.navMessages -> findViewById<ImageView>(R.id.navMessagesIcon)
            R.id.navProfile -> findViewById<ImageView>(R.id.navProfileIcon)
            else -> null
        }

        pill?.setBackgroundResource(R.drawable.bg_nav_pill)
        label?.visibility = View.VISIBLE
        label?.setTextColor(ContextCompat.getColor(this, R.color.bottom_nav_selected))
        icon?.setColorFilter(ContextCompat.getColor(this, R.color.bottom_nav_selected))
    }
}