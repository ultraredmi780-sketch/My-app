package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.FlashcardItem
import com.example.model.QuestionItem
import com.example.model.TermItem
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        for (item in list) {
            array.put(item)
        }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrEmpty()) return emptyList()
        val result = mutableListOf<String>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                result.add(array.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    @TypeConverter
    fun fromTermList(list: List<TermItem>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("term", item.term)
            obj.put("definition", item.definition)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toTermList(data: String?): List<TermItem> {
        if (data.isNullOrEmpty()) return emptyList()
        val result = mutableListOf<TermItem>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    TermItem(
                        term = obj.optString("term", ""),
                        definition = obj.optString("definition", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    @TypeConverter
    fun fromFlashcardList(list: List<FlashcardItem>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("front", item.front)
            obj.put("back", item.back)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toFlashcardList(data: String?): List<FlashcardItem> {
        if (data.isNullOrEmpty()) return emptyList()
        val result = mutableListOf<FlashcardItem>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    FlashcardItem(
                        front = obj.optString("front", ""),
                        back = obj.optString("back", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    @TypeConverter
    fun fromQuestionList(list: List<QuestionItem>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("question", item.question)
            val optArr = JSONArray()
            item.options.forEach { optArr.put(it) }
            obj.put("options", optArr)
            obj.put("correctAnswerIndex", item.correctAnswerIndex)
            obj.put("explanation", item.explanation)
            obj.put("type", item.type)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toQuestionList(data: String?): List<QuestionItem> {
        if (data.isNullOrEmpty()) return emptyList()
        val result = mutableListOf<QuestionItem>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val optArr = obj.optJSONArray("options") ?: JSONArray()
                val options = mutableListOf<String>()
                for (j in 0 until optArr.length()) {
                    options.add(optArr.getString(j))
                }
                result.add(
                    QuestionItem(
                        question = obj.optString("question", ""),
                        options = options,
                        correctAnswerIndex = obj.optInt("correctAnswerIndex", 0),
                        explanation = obj.optString("explanation", ""),
                        type = obj.optString("type", "mcq")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
