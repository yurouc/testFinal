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

class YearAnalyze : AppCompatActivity() {

    private lateinit var db: sql
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_year_analyze)

        db = sql(this)



        val chooseBtnW = findViewById<Button>(R.id.choose_btn_w)
        chooseBtnW.setOnClickListener {
            showYearPickerDialog(chooseBtnW)
        }

        val barChart = findViewById<BarChart>(R.id.barChart)
        val barChart2 = findViewById<BarChart>(R.id.barChart2)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        updateUI(currentYear)

        setupBarChart(barChart, "in", currentYear)  // 預設顯示當前年份
        setupBarChart(barChart2, "out", currentYear)

        findViewById<Button>(R.id.week_btn2).setOnClickListener {
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

    private fun setupBarChart(barChart: BarChart, type: String, year: Int) {
        val entries = mutableListOf<BarEntry>()
        val xLabels = mutableListOf<String>()  // X 軸標籤


        // 改善SQL查詢，避免不必要的日期處理
        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT strftime('%m', date) AS month, 
                   SUM(CAST(money AS INTEGER)) AS total 
            FROM data 
            WHERE type = ? 
            AND strftime('%Y', date) = ? 
            GROUP BY month
            """.trimIndent(),
            arrayOf(type, year.toString())
        )
        Log.d("SQLQuery", "Running query for type: $type, year: $year")

        var hasData = false
        while (cursor.moveToNext()) {
            hasData = true
            val month = cursor.getString(0)?.toIntOrNull() ?: continue
            val total = cursor.getInt(1)
            Log.d("SQLQueryResult", "Month: $month, Total: $total")
            xLabels.add("${month.toString().padStart(2, '0')}")
            entries.add(BarEntry(xLabels.size - 1f, total.toFloat()))
        }
        if (!hasData) {
            Log.d("SQLQueryResult", "No data found for type: $type, year: $year")
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

    private fun showYearPickerDialog(chooseBtn: Button) {
        val dialog = Dialog(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_year_month_picker2, null)
        dialog.setContentView(view)

        val yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 1900
        yearPicker.maxValue = currentYear
        yearPicker.value = currentYear

        confirmButton.setOnClickListener {
            val selectedYear = yearPicker.value
            showToast("你選擇的是：${selectedYear}年")
            chooseBtn.text = "${selectedYear}年"

            updateBarCharts(selectedYear)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateBarCharts(year: Int) {
        val barChart = findViewById<BarChart>(R.id.barChart)
        val barChart2 = findViewById<BarChart>(R.id.barChart2)

        setupBarChart(barChart, "in", year)
        setupBarChart(barChart2, "out", year)
        updateUI(year)
    }

    private fun updateUI(year: Int) {
        val totalOutcome = db.getTotalAmountForYear("out", year)
        val totalIncome = db.getTotalAmountForYear("in", year)

        findViewById<TextView>(R.id.outcome_num).text = totalOutcome.toInt().toString()
        findViewById<TextView>(R.id.income_num).text = totalIncome.toInt().toString()
        findViewById<TextView>(R.id.balance_num).text = (totalIncome - totalOutcome).toInt().toString()
    }

    private fun showToast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}
