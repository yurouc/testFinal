package com.example.testfinal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    // 初始化按鈕和顯示日期的 TextView
    private lateinit var buttonDatePicker: Button
    private lateinit var buttonStartAccounting: Button
    private lateinit var buttonAccountContent: Button
    private lateinit var buttonFinancialAnalysis: Button

    private lateinit var textViewDate: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 UI 元素
        buttonDatePicker = findViewById(R.id.button_date_picker)
        buttonStartAccounting = findViewById(R.id.button_start_accounting)
        buttonAccountContent = findViewById(R.id.button_account_content)
        buttonFinancialAnalysis = findViewById(R.id.button_financial_analysis)

        textViewDate = findViewById(R.id.textView)

        // 設置選擇日期按鈕的點擊事件
        buttonDatePicker.setOnClickListener {
            showDatePickerDialog()
        }

        // 設置其他按鈕的點擊事件 (跳轉頁面)
        buttonStartAccounting.setOnClickListener {
            navigateToAccountingPage()
        }

        buttonAccountContent.setOnClickListener {
            navigateToAccountContentPage()
        }

        buttonFinancialAnalysis.setOnClickListener {
            navigateToFinancialAnalysisPage()
        }

    }

    // 顯示日期選擇器
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                // 將選擇的日期顯示在按鈕上
                buttonDatePicker.text = selectedDate
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    // 模擬跳轉至不同頁面的方法
    private fun navigateToAccountingPage() {
        val intent = Intent(this, MainActivity_c::class.java)
        val selectedDate = buttonDatePicker.text.toString() // 從按鈕中取得日期文字
        intent.putExtra("date_key", selectedDate) // 傳遞日期數據
        startActivity(intent)
    }


    private fun navigateToAccountContentPage() {
        // 這裡可以寫跳轉邏輯
    }

    private fun navigateToFinancialAnalysisPage() {
        val intent = Intent(this, MainActivity_finance::class.java)
        startActivity(intent)
    }
}
