package com.example.medbook

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    var phone: String? = null
    var message: String? = null
    var disease: String = ""
    var name: String = ""
    var cnt: Int = 0
    var selectedDate: String? = null
    var cur_date: String? = null
    var time: String? = null
    var f: Boolean = true
    val timings: Array<String> = arrayOf(
        "10 A.M - 11 A.M",
        "11.30 A.M - 12.30 P.M",
        "2 P.M - 3 P.M",
        "4 P.M - 5 P.M",
        "6 P.M - 7 P.M"
    )

    // Static register object to store appointment data
    companion object {
        var register: MutableMap<String?, String?> = HashMap()
    }

    // Function to send SMS
    fun sendMessage() {
        try {
            val sms = SmsManager.getDefault()
            sms.sendTextMessage(phone, null, message, null, null)
            cnt += 1
            findViewById<TextView>(R.id.textView)?.text = cnt.toString()
            register[time] = selectedDate

            findViewById<EditText>(R.id.editTextTextPersonName)?.setText("")
            findViewById<EditText>(R.id.editTextPhone)?.setText("")
            findViewById<EditText>(R.id.editTextTextPersonName2)?.setText("")
            findViewById<Button>(R.id.button2)?.text = "Select Date"

            Toast.makeText(
                applicationContext,
                "Appointment Booked Successfully :)",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Message Not Sent :(", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val na = findViewById<EditText>(R.id.editTextTextPersonName)
        val di = findViewById<EditText>(R.id.editTextTextPersonName2)
        val t = findViewById<TextView>(R.id.textView)
        val ph = findViewById<EditText>(R.id.editTextPhone)
        val dateButton = findViewById<Button>(R.id.button2)
        val spinner = findViewById<Spinner>(R.id.spinner)

        // Request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                0
            )
        }

        // Spinner - Timings
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, timings
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                time = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Button - Date picker
        dateButton?.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH]
            val day = calendar[Calendar.DAY_OF_MONTH]
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            cur_date = sdf.format(calendar.time)

            val datePickerDialog = DatePickerDialog(
                this@MainActivity,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                    val cDate: Date? = sdf.parse(cur_date)
                    val sDate: Date? = sdf.parse(selectedDate)

                    if (sDate?.compareTo(cDate) ?: 0 > 0) {
                        dateButton.text = selectedDate
                        f = true
                    } else {
                        dateButton.text = "Invalid Date"
                        f = false
                    }
                }, year, month, day
            )
            datePickerDialog.show()
        }

        // Appointment Booking Button
        findViewById<Button>(R.id.button)?.setOnClickListener {
            var n = false
            var p = false
            var d = false

            // Name
            if (na?.text.toString().trim().isNotEmpty()) {
                name = na.text.toString()
                n = true
            } else {
                Toast.makeText(this, "Enter name :(", Toast.LENGTH_SHORT).show()
            }

            // Phone
            if (ph?.text.toString().length == 10) {
                phone = ph.text.toString()
                p = true
            } else {
                Toast.makeText(this, "Enter valid phone number :(", Toast.LENGTH_SHORT).show()
            }

            // Disease
            if (di?.text.toString().trim().isNotEmpty()) {
                disease = di.text.toString()
                d = true
            } else {
                Toast.makeText(this, "Enter Disease :(", Toast.LENGTH_SHORT).show()
            }

            // Message format
            message = """
                --- MEPCO CLINIC ---
                
                Appointment Confirmation
                
                Name: $name
                Phone Number: $phone
                Date: $selectedDate
                Timing: $time
                Disease: $disease
                
                THANK YOU :) 
            """.trimIndent()

            // Check if time slot already booked
            for ((key, value) in register) {
                if (selectedDate == value && time == key) {
                    f = false
                    Toast.makeText(applicationContext, "Already Booked :)", Toast.LENGTH_SHORT)
                        .show()
                    break
                } else {
                    f = true
                }
            }

            // Send message if everything is correct
            if (p && f && d && n) sendMessage()
        }
    }
}
