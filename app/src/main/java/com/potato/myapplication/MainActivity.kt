package com.potato.myapplication

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private lateinit var getBillAmount: EditText
    private lateinit var seekBarTip: SeekBar
    private lateinit var tvTipPercentage: TextView
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvTipDescription: TextView
    private lateinit var themeSwitch: MaterialSwitch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Theme switch logic, Apply before setting content view
        val sharedPreferences = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)
        applyTheme(isDarkMode)

        setContentView(R.layout.activity_main)

        // Initialize UI elements
        getBillAmount = findViewById(R.id.getBillAmount)
        seekBarTip = findViewById(R.id.seekBarTip)
        tvTipPercentage = findViewById(R.id.tvTipPercentage)
        tvTipAmount = findViewById(R.id.tvTipAmount)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvTipDescription = findViewById(R.id.tvTipDescription)
        themeSwitch = findViewById(R.id.themeSwitch)

        // Set switch state without triggering listener
        themeSwitch.isChecked = isDarkMode

        // Set initial values
        updateTipUI(seekBarTip.progress)
        computeTipAndTotal()

        // Add a listener to the SeekBar
        seekBarTip.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateTipUI(progress)
                computeTipAndTotal()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Add a listener to the EditText for bill amount
        getBillAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                computeTipAndTotal()
            }
        })

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            applyTheme(isChecked)
            sharedPreferences.edit().putBoolean("isDarkMode", isChecked).apply()
        }
    }

    private fun applyTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun updateTipUI(progress: Int) {
        // Use the string resource with a placeholder
        tvTipPercentage.text = getString(R.string.tip_percentage_label, progress)

        val tipDescription: String
        val colorRes: Int
        when (progress) {
            in 0..9 -> {
                tipDescription = "Poor"
                colorRes = R.color.tip_poor
            }
            in 10..14 -> {
                tipDescription = "Acceptable"
                colorRes = R.color.tip_acceptable
            }
            in 15..19 -> {
                tipDescription = "Good"
                colorRes = R.color.tip_good
            }
            in 20..24 -> {
                tipDescription = "Great"
                colorRes = R.color.tip_great
            }
            else -> {
                tipDescription = "Amazing"
                colorRes = R.color.tip_amazing
            }
        }
        tvTipDescription.text = tipDescription
        tvTipDescription.setTextColor(ContextCompat.getColor(this, colorRes))
    }

    private fun computeTipAndTotal() {
        val billAmountStr = getBillAmount.text.toString()
        if (billAmountStr.isEmpty()) {
            tvTipAmount.text = ""
            tvTotalAmount.text = ""
            return
        }

        val billAmount = billAmountStr.toDoubleOrNull()
        if (billAmount == null) {
            tvTipAmount.text = ""
            tvTotalAmount.text = ""
            return
        }

        // 1. Get tip percentage from the SeekBar
        val tipPercent = seekBarTip.progress

        // 2. Compute the tip and total
        val tipAmount = billAmount * tipPercent / 100
        val totalAmount = billAmount + tipAmount

        // 3. Update the UI using string resources with placeholders
        tvTipAmount.text = getString(R.string.currency_format, tipAmount)
        tvTotalAmount.text = getString(R.string.currency_format, totalAmount)
    }
}