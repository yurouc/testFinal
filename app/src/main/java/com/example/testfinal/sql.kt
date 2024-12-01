package com.example.testfinal

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.getIntOrNull

class sql(
    context: Context,
    name: String = database,
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = ver
) : SQLiteOpenHelper(context, name, factory, version) {
    companion object {
        private const val database = "data" //資料庫名稱
        private const val ver = 2 //資料庫版本
    }
    override fun onCreate(db: SQLiteDatabase) {
        //建立data 資料表，表內有 date, class, type, money 欄位，分別存日期、類別(食物、交通、娛樂等)、類型(收入/支出 )、金額
        db.execSQL("""
            CREATE TABLE data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                clas TEXT NOT NULL,
                type TEXT NOT NULL,
                money TEXT NOT NULL
            )
            """.trimIndent()
        )
        fixDateFormat(db)  // 確保資料庫中的日期格式正確
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int,
                           newVersion: Int) {
        //升級資料庫版本時，刪除舊資料表，並重新執行 onCreate()，建立新資料表
        db.execSQL("DROP TABLE IF EXISTS data")
        onCreate(db)
    }
    /**
     * 獲取支出或收入總金額
     * @param type 收入或支出的類型 ("收入" 或 "支出")
     * @return 總金額 (INT)
     */
    fun getTotalAmount(type: String, year: Int, month: Int): Double {
        val db = readableDatabase

        // 格式化月份为两位数
        val formattedMonth = month.toString().padStart(2, '0')

        val cursor = db.rawQuery(
            """
        SELECT SUM(CAST(money AS INTEGER)) 
        FROM data 
        WHERE type = ? 
        AND strftime('%Y', date) = ? 
        AND strftime('%m', date) = ? 
        """.trimIndent(),
            arrayOf(type, year.toString(), formattedMonth)
        )

        var totalAmount = 0.0
        if (cursor.moveToFirst()) {
            totalAmount = cursor.getIntOrNull(0)?.toDouble() ?: 0.0
        }
        cursor.close()
        return totalAmount
    }
    fun getTotalAmountForYear(type: String, year: Int): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT SUM(CAST(money AS INTEGER)) FROM data 
        WHERE type = ? 
        AND strftime('%Y', date) = ?
        """.trimIndent(),
            arrayOf(type, year.toString())
        )
        var totalAmount = 0.0
        if (cursor.moveToFirst()) {
            totalAmount = cursor.getIntOrNull(0)?.toDouble() ?: 0.0
        }
        cursor.close()
        return totalAmount
    }

    /**
     * 修正日期格式
     */
    fun fixDateFormat(db: SQLiteDatabase) {
        val updateQuery = """
        UPDATE data
        SET date = 
            CASE
                -- 如果格式為 YYYY-M-D，補零為 YYYY-MM-DD
                WHEN date LIKE '____-_-_' THEN 
                    substr(date, 1, 5) || '0' || substr(date, 6, 1) || '-0' || substr(date, 8, 1)

                -- 如果格式為 YYYY-MM-D，補零為 YYYY-MM-DD
                WHEN date LIKE '____-__-_' THEN 
                    substr(date, 1, 8) || '0' || substr(date, 9, 1)

                -- 如果格式為 YYYY-M-DD，補零為 YYYY-MM-DD
                WHEN date LIKE '____-_-__' THEN 
                    substr(date, 1, 5) || '0' || substr(date, 6)

                -- 如果格式為 YYYY-MM-DD，保持原樣
                ELSE date
            END
    """.trimIndent()

        db.execSQL(updateQuery)

        // 檢查更新後資料是否正確
        val cursor = db.rawQuery("SELECT date FROM data", null)
        while (cursor.moveToNext()) {
            val date = cursor.getString(0)
            Log.d("SQLFixDateCheck", "Updated Date: $date")
        }
        cursor.close()
    }
}