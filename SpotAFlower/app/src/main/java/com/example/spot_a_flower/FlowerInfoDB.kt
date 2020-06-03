package com.example.spot_a_flower


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader


class FlowerInfoDB(private val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "FlowerInfoDB"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "FlowerInfo"
        private const val ID = "id"
        private const val NAME = "name"
        private const val INTRO = "intro"
        private const val ICON = "icon"
        private const val WIKI = "link"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE $TABLE_NAME " +
                "($ID Integer PRIMARY KEY, $NAME TEXT, $INTRO TEXT, $WIKI TEXT, $ICON BLOB)"
        db.execSQL(CREATE_TABLE)

        // prepopulate the database
        val reader = BufferedReader(
            InputStreamReader(context.assets.open("flower_labels.txt"))
        )
        for (i in 1..102) {
            val name = reader.readLine()
            val description = reader.readLine()
            val link = reader.readLine()
            val inputStream = context.assets.open("icons/$i.jpg")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            addFlower(name, description, link, bitmap, db)
        }
        //printAllFlowers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    //Inserting (Creating) data
    private fun addFlower(name: String, description: String, link: String, bitmap: Bitmap, db: SQLiteDatabase) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val image = stream.toByteArray()

        val values = ContentValues().apply {
            put(NAME, name)
            put(INTRO, description)
            put(WIKI, link)
            put(ICON, image)
        }
        db.insert(TABLE_NAME, null, values)
    }

    fun getIcon(name: String): Bitmap {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $NAME=?", arrayOf(name))
        cursor.moveToFirst()
        val bitmap = if (cursor.moveToFirst()) {
            val icon = cursor.getBlob(cursor.getColumnIndex(ICON))
            BitmapFactory.decodeByteArray(icon, 0, icon.size)
        } else {
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.logo
            )
        }
        cursor.close()
        db.close()
        return bitmap
    }

    fun getDescription(name: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $NAME=?", arrayOf(name))
        val intro = "                         " + if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex(INTRO))
        } else
            "Lily (members of which are true lilies) is a genus of herbaceous flowering plants growing from bulbs, all with large prominent flowers. Lilies are a group of flowering plants which are important in culture and literature in much of the world. Most species are native to the temperate northern hemisphere, though their range extends into the northern subtropics. Many other plants have \"lily\" in their common name but are not related to true lilies."
        cursor.close()
        db.close()
        return intro
    }

    fun getWikiLink(name: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $NAME=?", arrayOf(name))
        val link = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex(WIKI))
        } else "https://en.wikipedia.org/wiki/$name"
        cursor.close()
        db.close()
        return link
    }

    private fun printAllFlowers(db: SQLiteDatabase) {
        var allFlower = ""
        val selectALLQuery = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getString(cursor.getColumnIndex(ID))
                    val name = cursor.getString(cursor.getColumnIndex(NAME))
                    val description = cursor.getString(cursor.getColumnIndex(INTRO))
                    val link = cursor.getString(cursor.getColumnIndex(INTRO))
                    val icon = cursor.getBlob(cursor.getColumnIndex(ICON))
                    allFlower = "$allFlower\n$id $name $icon $link $description"
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        println(allFlower)
    }

    fun clear() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }
}

