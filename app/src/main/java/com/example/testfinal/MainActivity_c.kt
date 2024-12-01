package com.example.testfinal
//玉庭
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.database.sqlite.SQLiteDatabase
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.testfinal.RecodeActivity

class MainActivity_c : AppCompatActivity() {

    private val incomeCategories = listOf("薪水", "獎金", "零用錢", "投資", "其他")
    private val expenseCategories = listOf("飲食", "交通", "娛樂", "日用品", "其他")

    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var db: SQLiteDatabase
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_c)
        //建立資料庫
        db = sql(this).writableDatabase

        //綁定元件
        val date = findViewById<TextView>(R.id.date) //選擇的日期
        val btnClean = findViewById<Button>(R.id.deleted) // 清除所有輸入
        val money = findViewById<EditText>(R.id.textMoney) // 金額
        val iinn = findViewById<Button>(R.id.btnIn) // 收入按鈕
        val out = findViewById<Button>(R.id.btnOut) // 支出按鈕
        var type = "in" // 預設類型為收入
        val choo = findViewById<AutoCompleteTextView>(R.id.Choose) // 收入/支出類別
        val sub = findViewById<Button>(R.id.save) //儲存

        val iback = findViewById<Button>(R.id.back) //回主畫面
        iback.setOnClickListener {
            // 跳轉至 MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, expenseCategories)
        choo.setAdapter(adapter)

        //收入按鈕被點擊，切換成收入
        iinn.setOnClickListener {
            type = "in"
            updateCategories(choo, incomeCategories)
        }
        //支出按鈕被點擊，切換成支出
        out.setOnClickListener {
            type = "out"
            updateCategories(choo, expenseCategories)
        }

        //收主畫面傳來的日期
        val receivedDate = intent.getStringExtra("date_key")
        date.text = receivedDate ?: "No date selected" // 如果沒接收到數據，顯示預設訊息

        //插入一筆資料
        sub.setOnClickListener {
            val moneyText = money.text.toString()
            val chooText = choo.text.toString()
            if (moneyText.isEmpty() || chooText.isEmpty()) {
                showToast("錯誤：請完整填寫所有欄位")
            } else {
                try {
                    //插入一筆紀錄於資料表
                    db.execSQL(
                        "INSERT INTO data (date, clas, type, money) " +
                                "VALUES('${date.text}','${choo.text}','$type','${money.text}')"
                    )
                    showToast(
                        "成功寫入data資料庫以下資料:${date.text},class:${choo.text}," +
                                "money：${money.text},in or out：${type}"
                    )
                    val intent = Intent(this, RecodeActivity::class.java)
                    startActivity(intent)


                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("新增失敗:$e")

                }
            }
        }
        //清除輸入欄
        btnClean.setOnClickListener{
            choo.text=null
            money.text=null
            type="in"
        }

        iback.setOnClickListener {
            finish()
        }

    }



    //建立 showToast 方法顯示 Toast 訊息
    private fun showToast(text: String) =
        Toast.makeText(this,text, Toast.LENGTH_LONG).show()
}


private fun updateCategories(categoryView: AutoCompleteTextView, categories: List<String>) {
    val adapter = ArrayAdapter(categoryView.context, android.R.layout.simple_dropdown_item_1line, categories)
    categoryView.setAdapter(adapter)
    categoryView.setText("", false) // 清空當前選擇
    categoryView.showDropDown()
}
