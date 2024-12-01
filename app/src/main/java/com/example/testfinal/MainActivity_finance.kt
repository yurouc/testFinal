package com.example.testfinal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity_finance : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_finance_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //點擊每週分析按鈕後換頁
        val btnWeek = findViewById<Button>(R.id.btn_week)
        btnWeek.setOnClickListener {
            val intent = Intent(this, WeekAnalyze::class.java)
            startActivity(intent)
        }
        //  下方要改成嘉佩做的頁面
        val btnMonth = findViewById<Button>(R.id.btn_month)
        btnMonth.setOnClickListener {
            val intent = Intent(this, MonthAnalyze::class.java)
            startActivity(intent)
        }
        //  下方要改成嘉佩做的頁面
        val btnYear = findViewById<Button>(R.id.btn_year)
        btnYear.setOnClickListener {
            val intent = Intent(this, YearAnalyze::class.java)
            startActivity(intent)
        }
    }
}