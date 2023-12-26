package com.example.mycalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import android.os.Vibrator
import android.telephony.PhoneNumberUtils.formatNumber
import android.view.View.GONE
import android.view.View.VISIBLE
import java.text.DecimalFormat
import android.util.Log
import java.text.DecimalFormatSymbols
import java.util.*


class MainActivity : AppCompatActivity() {

    private var canAddOperation = false
    private var canAddDecimal = true
    lateinit var vibrator: Vibrator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        val switch = findViewById<Switch>(R.id.swtich)

        switch.setOnCheckedChangeListener { _, _ ->
            if (switch.isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
    fun numberAction(view: View) {
        vibrator.vibrate(50)
        val workingsTV = findViewById<TextView>(R.id.workingsTV)

        if (view is Button) {
            if (workingsTV.text.toString() == "0") {
                workingsTV.text = ""
            }
            if (view.text == ".") {
                if (canAddDecimal)
                    workingsTV.append(view.text)

                canAddDecimal = false
            } else
                workingsTV.append(view.text)

            canAddOperation = true
        }
    }
    fun operationAction(view: View) {
        vibrator.vibrate(50)
        val workingsTV = findViewById<TextView>(R.id.workingsTV)

        if (view is Button && canAddOperation) {
            workingsTV.append(view.text)
            canAddOperation = false
            canAddDecimal = true

        }
    }
    fun allClearAction(view: View) {
        vibrator.vibrate(50)
        val workingsTV = findViewById<TextView>(R.id.workingsTV)
        val resultsTV = findViewById<TextView>(R.id.resultsTV)

        workingsTV.text = ""
        resultsTV.text = ""
    }
    fun backSpaceAction(view: View) {
        vibrator.vibrate(50)
        val workingsTV = findViewById<TextView>(R.id.workingsTV)

        val length = workingsTV.length()
        if (length > 0)
            workingsTV.text = workingsTV.text.subSequence(0, length - 1)
    }
    fun visibleAction(view: View) {
        vibrator.vibrate(50)
        val workingsTV = findViewById<TextView>(R.id.workingsTV)
        val resultsTV = findViewById<TextView>(R.id.resultsTV)

        workingsTV.visibility = VISIBLE
        resultsTV.visibility = VISIBLE

        workingsTV.text = "0"
        resultsTV.text = ""
    }
    fun noVisibleAction(view: View) {
        vibrator.vibrate(50)
        val workingsTV = findViewById<TextView>(R.id.workingsTV)
        val resultsTV = findViewById<TextView>(R.id.resultsTV)

        workingsTV.visibility = GONE
        resultsTV.visibility = GONE
    }
    fun equalsAction(view: View) {
        vibrator.vibrate(50)
        val resultsTV = findViewById<TextView>(R.id.resultsTV)
        val workingsTV = findViewById<TextView>(R.id.workingsTV)

        try {
            val result = calculateAndFormatResult()
            resultsTV.text = result

            // Formatear la pantalla de entrada de datos
            formatWorkingsTV()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Calculator", "Error en equalsAction: ${e.message}")
        }
    }
    private fun calculateResults(): String {
        val digitsOperators = digitsOperators()
        if (digitsOperators.isEmpty()) return ""

        val timesDivision = timesDivisionCalculate(digitsOperators)
        if (timesDivision.isEmpty()) return ""

        val result = addSubtractCalculate(timesDivision)
        return result.toString()
    }
    private fun addSubtractCalculate(passedList: MutableList<Any>): Float {
        var result = passedList[0] as Float

        var i = 1
        while (i < passedList.size) {
            val operator = passedList[i] as Char
            val nextDigit = passedList[i + 1] as Float

            when (operator) {
                '+' -> result += nextDigit
                '-' -> result -= nextDigit
            }

            i += 2
        }
        return result
    }
    private fun timesDivisionCalculate(passedList: MutableList<Any>): MutableList<Any> {
        var list = passedList
        while (list.contains('x') || list.contains('/')) {
            list = calcTimesDiv(list)
        }
        return list
    }
    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()
        var restartIndex = passedList.size

        for (i in passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex && i < restartIndex) {
                val operator = passedList[i]
                val prevDigit = passedList[i - 1] as Float
                val nextDigit = passedList[i + 1] as Float

                when (operator) {
                    'x' -> {
                        newList.add(prevDigit * nextDigit)
                        restartIndex = i + 1
                    }
                    '/' -> {
                        newList.add(prevDigit / nextDigit)
                        restartIndex = i + 1
                    }
                    else -> {
                        newList.add(prevDigit)
                        newList.add(operator)
                    }
                }
            }
            if (i > restartIndex) {
                newList.add(passedList[i])
            }
        }
        return newList
    }
    private fun digitsOperators(): MutableList<Any> {
        val workingsTV = findViewById<TextView>(R.id.workingsTV)

        val list = mutableListOf<Any>()
        var currentDigit = ""
        var decimalAdded = false

        for (character in workingsTV.text) {
            if (character.isDigit() || character == '.') {
                if (character == '.' && decimalAdded) {

                    continue
                } else if (character == '.') {
                    decimalAdded = true
                }
                currentDigit += character
            } else {
                if (currentDigit.isEmpty() && character == '-') {
                    currentDigit = "0"
                }
                if (currentDigit != "") {
                    list.add(currentDigit.toFloat())
                    currentDigit = ""
                    decimalAdded = false
                }
                list.add(character)
            }
        }
        if (currentDigit != "") {
            list.add(currentDigit.toFloat())
        }

        return list
    }
    private fun formatResult(result: Float): String {
        val decimalFormat = DecimalFormat("#,###.#########")
        return decimalFormat.format(result)
    }
    fun calculateAndFormatResult(): String {
        val result = calculateResults()
        return formatResult(result.toFloat())
    }
    private fun formatWorkingsTV() {
        val workingsTV = findViewById<TextView>(R.id.workingsTV)
        val formattedText = formatNumberForDisplay(workingsTV.text.toString())
        workingsTV.text = formattedText
    }
    private fun formatNumberForDisplay(input: String): String {
        val number = input.toFloatOrNull()
        return if (number != null) {
            DecimalFormat("#,###.#########").format(number)
        } else {
            input
        }
    }
}