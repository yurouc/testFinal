package com.example.testfinal

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.*

class MonthAnalyze : AppCompatActivity() {

    private lateinit var db: sql
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_month_analyze)

        db = sql(this)
        db.writableDatabase.execSQL("INSERT INTO data (date, clas, type, money) VALUES ('2024-12-01', 'Food', '收入', '1000')")
        db.fixDateFormat(db.writableDatabase)



        val chooseBtnW = findViewById<Button>(R.id.choose_btn_w2)
        chooseBtnW.setOnClickListener {
            showYearMonthPickerDialog(chooseBtnW)
        }

        val barChart = findViewById<BarChart>(R.id.barChart)
        val barChart2 = findViewById<BarChart>(R.id.barChart2)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        updateUI(currentYear, currentMonth)

        setupBarChart(barChart, "in", currentYear, currentMonth)  // 預設顯示當前年度和月份
        setupBarChart(barChart2, "out", currentYear, currentMonth)

        findViewById<Button>(R.id.week_btn1).setOnClickListener {
            startActivity(Intent(this, WeekAnalyze::class.java))
        }

        findViewById<Button>(R.id.month_btn).setOnClickListener {
            startActivity(Intent(this, MonthAnalyze::class.java))
        }

        findViewById<Button>(R.id.year_btn).setOnClickListener {
            startActivity(Intent(this, YearAnalyze::class.java))
        }

        findViewById<Button>(R.id.home_btn).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun setupBarChart(barChart: BarChart, type: String, year: Int, month: Int) {
        val entries = mutableListOf<BarEntry>()
        val xLabels = mutableListOf<String>()  // X 軸標籤

        // 確保月份是兩位數
        val formattedMonth = month.toString().padStart(2, '0')

        // 改善SQL查詢，避免不必要的日期處理
        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT strftime('%d', date) AS day, 
                   SUM(CAST(money AS INTEGER)) AS total 
            FROM data 
            WHERE type = ? 
            AND strftime('%Y', date) = ? 
            AND strftime('%m', date) = ? 
            GROUP BY day
            """.trimIndent(),
            arrayOf(type, year.toString(), formattedMonth)
        )
        Log.d("SQLQuery", "Running query for type: $type, year: $year, month: $formattedMonth")

        var hasData = false
        while (cursor.moveToNext()) {
            hasData = true
            val day = cursor.getString(0)?.toIntOrNull() ?: continue
            val total = cursor.getInt(1)
            Log.d("SQLQueryResult", "Day: $day, Total: $total")
            xLabels.add("$day")
            entries.add(BarEntry(xLabels.size - 1f, total.toFloat()))
        }
        if (!hasData) {
            Log.d("SQLQueryResult", "No data found for type: $type, year: $year, month: $formattedMonth")
        }
        cursor.close()

        if (entries.isEmpty()) {
            barChart.clear()
            barChart.invalidate()
            return
        }

        val label = if (type == "in") "收入趨勢" else "支出趨勢"
        val dataSet = BarDataSet(entries, label)
        dataSet.color = if (type == "in") getColor(R.color.teal_200) else getColor(R.color.red_500)

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.description.isEnabled = false

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(xLabels)
            granularity = 1f
            isGranularityEnabled = true
        }

        barChart.animateY(1000)
        barChart.invalidate()
    }


    /**
     * 顯示年份與月份選擇器的對話框
     */
    private fun showYearMonthPickerDialog(chooseBtn: Button) {
        val dialog = Dialog(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_year_month_picker, null)
        dialog.setContentView(view)

        val yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.month_picker)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 1900
        yearPicker.maxValue = currentYear
        yearPicker.value = currentYear

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = Calendar.getInstance().get(Calendar.MONTH) + 1

        confirmButton.setOnClickListener {
            val selectedYear = yearPicker.value
            val selectedMonth = monthPicker.value
            showToast("你選擇的是：${selectedYear}年 ${selectedMonth}月")
            chooseBtn.text = "${selectedYear}年 ${selectedMonth}月"

            updateBarCharts(selectedYear, selectedMonth)
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * 更新柱狀圖
     */
    private fun updateBarCharts(year: Int, month: Int) {
        val barChart = findViewById<BarChart>(R.id.barChart)
        val barChart2 = findViewById<BarChart>(R.id.barChart2)

        setupBarChart(barChart, "in", year, month)
        setupBarChart(barChart2, "out", year, month)
        updateUI(year, month)
    }

    /**
     * 更新頁面上的統計數據
     */
    private fun updateUI(year: Int, month: Int) {
        val totalOutcome = db.getTotalAmount("out", year, month)
        val totalIncome = db.getTotalAmount("in", year, month)

        findViewById<TextView>(R.id.outcome_num).text = totalOutcome.toInt().toString()
        findViewById<TextView>(R.id.income_num).text = totalIncome.toInt().toString()
        findViewById<TextView>(R.id.balance_num).text = (totalIncome - totalOutcome).toInt().toString()
    }

    private fun showToast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}