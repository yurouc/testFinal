package com.example.testfinal
//玉庭
import android.os.Bundle
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class RecodeActivity : AppCompatActivity() {

    private lateinit var rdb: SQLiteDatabase
    private var items1: ArrayList<String> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recode_activity)

        val home = findViewById<Button>(R.id.rback) //回主畫面

        //取得資料庫實體
        rdb = sql(this).writableDatabase

        //宣告 list用的Adapter 並連結 resultList
        adapter = CustomAdapter(this, items1)
        findViewById<ListView>(R.id.resultList).adapter = adapter
        Log.d("RecodeActivity", "Adapter initialized successfully")

        //顯示全部資料
        query("select * from data")

        //回主畫面
        home.setOnClickListener {
            finish()
        }

    }

    private fun query(queryString: String) {
        try {
            val c = rdb.rawQuery(queryString, null)
            items1.clear()

            if (c.moveToFirst()) {
                do {
                    val date = c.getString(c.getColumnIndexOrThrow("date"))
                    val clas = c.getString(c.getColumnIndexOrThrow("clas"))
                    val type = c.getString(c.getColumnIndexOrThrow("type"))
                    val money = c.getString(c.getColumnIndexOrThrow("money"))

                    items1.add("日期 : $date\n類別 : $clas\n金額 : $money")
                    Log.d("RecodeActivity", "Record added: 日期 : $date, 類別 : $clas, 金額 : $money")
                } while (c.moveToNext())
            } else {
                Log.d("RecodeActivity", "No data found.")
            }

            c.close()
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("RecodeActivity", "查詢失敗", e)
        }
    }

    private class CustomAdapter(context: RecodeActivity, private val items: ArrayList<String>) :ArrayAdapter<String>(context, 0, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
            val item = getItem(position)
            val dateTextView = view.findViewById<TextView>(R.id.date)
            val clasTextView = view.findViewById<TextView>(R.id.clas)
            val moneyTextView = view.findViewById<TextView>(R.id.money)

            val parts = item!!.split("\n")
            dateTextView.text = parts[0]
            clasTextView.text = parts[1]
            moneyTextView.text = parts[2]
            return view
        }
    }
}

