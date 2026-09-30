package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class TestQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
) {
    companion object {
        fun fromJson(jsonStr: String): List<TestQuestion> {
            val list = mutableListOf<TestQuestion>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val optsArray = obj.getJSONArray("options")
                    val options = mutableListOf<String>()
                    for (j in 0 until optsArray.length()) {
                        options.add(optsArray.getString(j))
                    }
                    list.add(
                        TestQuestion(
                            id = obj.optInt("id", i + 1),
                            question = obj.optString("question", ""),
                            options = options,
                            correctIndex = obj.optInt("correctIndex", 0),
                            explanation = obj.optString("explanation", "")
                        )
                    )
                }
            } catch (e: Exception) {
                // Fallback default sample questions
                list.add(
                    TestQuestion(
                        id = 1,
                        question = "What is the SI unit of electric charge?",
                        options = listOf("Coulomb (C)", "Ampere (A)", "Volt (V)", "Farad (F)"),
                        correctIndex = 0,
                        explanation = "Coulomb (C) is the standard SI unit of electric charge, defined as 1 Ampere x 1 Second."
                    )
                )
            }
            return list
        }

        fun toJson(questions: List<TestQuestion>): String {
            val array = JSONArray()
            for (q in questions) {
                val obj = JSONObject()
                obj.put("id", q.id)
                obj.put("question", q.question)
                val optsArray = JSONArray()
                for (opt in q.options) {
                    optsArray.put(opt)
                }
                obj.put("options", optsArray)
                obj.put("correctIndex", q.correctIndex)
                obj.put("explanation", q.explanation)
                array.put(obj)
            }
            return array.toString()
        }
    }
}
