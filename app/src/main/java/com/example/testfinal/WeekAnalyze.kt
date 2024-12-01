package com.example.testfinal

import android.app.DatePickerDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeekAnalyze : AppCompatActivity() {

    private lateinit var pieChart1: PieChart
    private lateinit var pieChart2: PieChart
    private lateinit var dbHelper: sql

    private fun setupPieChart(pieChart: PieChart) {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 35f
            transparentCircleRadius = 40f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            legend.apply {
                isEnabled = true
                textSize = 12f
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
            }
            setUsePercentValues(true)
        }
    }

    private fun updatePieChart(pieChart: PieChart, entries: List<PieEntry>, label: String, colors: List<Int>) {
        val dataSet = PieDataSet(entries, label).apply {
            this.colors = colors
        }
        pieChart.data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
            setValueTextSize(12f)
            setValueTextColor(Color.BLACK)
        }
        pieChart.invalidate()
    }

    private fun fetchDataFromDatabase(startDate: String, endDate: String): Pair<List<PieEntry>, List<PieEntry>> {
        val db = dbHelper.readableDatabase
        val incomeData = mutableListOf<PieEntry>()
        val expenseData = mutableListOf<PieEntry>()

        // 支出類別
        val expenseCategories = listOf("飲食", "交通", "娛樂", "日用品", "其他")
        // 收入類別
        val incomeCategories = listOf("薪水", "獎金", "零用錢", "投資", "其他")

        // 初始化類別金額 Map
        val incomeCategoryAmounts = incomeCategories.associateWith { 0.0f }.toMutableMap()
        val expenseCategoryAmounts = expenseCategories.associateWith { 0.0f }.toMutableMap()

        // 查詢特定日期範圍的資料
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(
                "SELECT clas, type, money FROM data WHERE date BETWEEN ? AND ?",
                arrayOf(startDate, endDate)
            )

            // 驗證列名稱
            val clasColumnIndex = cursor.getColumnIndex("clas")
            val typeColumnIndex = cursor.getColumnIndex("type")
            val moneyColumnIndex = cursor.getColumnIndex("money")

            // 檢查列索引是否有效
            if (clasColumnIndex == -1 || typeColumnIndex == -1 || moneyColumnIndex == -1) {
                Log.e("WeekAnalyze", "Invalid column names. Check your database schema.")
                return Pair(emptyList(), emptyList())
            }

            // 統計各類別金額
            while (cursor.moveToNext()) {
                val category = cursor.getString(clasColumnIndex)
                val type = cursor.getString(typeColumnIndex)
                val money = cursor.getString(moneyColumnIndex).toFloatOrNull() ?: 0.0f

                when (type) {
                    "收入" -> {
                        if (category in incomeCategories) {
                            incomeCategoryAmounts[category] = incomeCategoryAmounts.getOrDefault(category, 0.0f) + money
                        }
                    }
                    "支出" -> {
                        if (category in expenseCategories) {
                            expenseCategoryAmounts[category] = expenseCategoryAmounts.getOrDefault(category, 0.0f) + money
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WeekAnalyze", "Error fetching data from database", e)
        } finally {
            cursor?.close()
            db.close()
        }

        // 將統計結果轉換為 PieEntry
        incomeCategoryAmounts.forEach { (category, amount) ->
            if (amount > 0) {
                incomeData.add(PieEntry(amount, category))
            }
        }

        expenseCategoryAmounts.forEach { (category, amount) ->
            if (amount > 0) {
                expenseData.add(PieEntry(amount, category))
            }
        }

        return Pair(incomeData, expenseData)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_week_analyze)

        // 初始化資料庫幫助類
        dbHelper = sql(this)

        pieChart1 = findViewById(R.id.pieChart1)
        pieChart2 = findViewById(R.id.pieChart2)

        // 初始化圓餅圖設置
        setupPieChart(pieChart1)
        setupPieChart(pieChart2)

        // 設置返回按鈕行為
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 設置日期選擇
        val tvDate = findViewById<TextView>(R.id.tv_date)
        tvDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            // 顯示日期選擇器
            DatePickerDialog(this, { _, year, month, day ->
                // 更新 calendar 為使用者選擇的日期
                calendar.set(year, month, day)
                // 格式化日期
                val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                val startDate = dateFormat.format(calendar.time)
                // 計算選擇日期加 6 天
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                val endDate = dateFormat.format(calendar.time)
                // 顯示範圍日期
                tvDate.text = "$startDate 到 $endDate"

                // 更新圖表數據
                val (incomeEntries, expenseEntries) = fetchDataFromDatabase(startDate, endDate)
                updatePieChart(pieChart1, incomeEntries, "收入數據", ColorTemplate.MATERIAL_COLORS.toList())
                updatePieChart(pieChart2, expenseEntries, "支出數據", ColorTemplate.JOYFUL_COLORS.toList())
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}