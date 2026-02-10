package com.example.ritamesa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.StudentScheduleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class JadwalHarianSiswaActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var txtTanggal: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var iconCalendar: ImageView

    private var isPengurus = false
    private var selectedDate = Calendar.getInstance()
    private val jadwalList = mutableListOf<JadwalHarianItem>()
    private lateinit var adapter: JadwalHarianAdapter

    companion object {
        private const val TAG = "JadwalHarian"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            isPengurus = intent.getBooleanExtra("IS_PENGURUS", false)
            setContentView(R.layout.jadwal_harian_siswa)

            initViews()
            setupCalendarButton()
            setupBackPressedHandler()
            
            loadSchedulesFromApi()

        } catch (e: Exception) {
            Log.e(TAG, "ERROR: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        txtTanggal = findViewById(R.id.TextTanggalTerkini)
        recyclerView = findViewById(R.id.recycler_jadwal_now)
        iconCalendar = findViewById(R.id.icon_calendar)

        updateTanggalDisplay()

        adapter = JadwalHarianAdapter(jadwalList, isPengurus)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener {
            navigateToRiwayatKehadiran()
        }
    }

    private fun setupCalendarButton() {
        iconCalendar.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateTanggalDisplay()
                loadSchedulesFromApi()
            }, year, month, day
        )
        datePickerDialog.show()
    }

    private fun getFormattedDate(): String {
        return try {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val formatted = sdf.format(selectedDate.time)
            if (formatted.isNotEmpty()) {
                formatted[0].uppercaseChar() + formatted.substring(1)
            } else {
                formatted
            }
        } catch (e: Exception) {
            "-"
        }
    }

    private fun updateTanggalDisplay() {
        txtTanggal.text = getFormattedDate()
    }

    private fun loadSchedulesFromApi() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateStr = dateFormat.format(selectedDate.time)

        // Try the wrapped response first
        apiService.getStudentSchedules(date = dateStr).enqueue(object : Callback<StudentScheduleResponse> {
            override fun onResponse(call: Call<StudentScheduleResponse>, response: Response<StudentScheduleResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        processApiData(data)
                    } else {
                        jadwalList.clear()
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    // Try list format as fallback
                    loadSchedulesAsList(dateStr)
                }
            }

            override fun onFailure(call: Call<StudentScheduleResponse>, t: Throwable) {
                Log.e(TAG, "Schedule load error: ${t.message}", t)
                // Try list format as fallback
                loadSchedulesAsList(dateStr)
            }
        })
    }

    private fun loadSchedulesAsList(dateStr: String) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        apiService.getStudentSchedulesList(date = dateStr).enqueue(object : Callback<List<com.example.ritamesa.data.model.StudentScheduleItem>> {
            override fun onResponse(
                call: Call<List<com.example.ritamesa.data.model.StudentScheduleItem>>,
                response: Response<List<com.example.ritamesa.data.model.StudentScheduleItem>>
            ) {
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    processScheduleItems(items)
                } else {
                    Log.d(TAG, "Schedule API error: ${response.code()}")
                    jadwalList.clear()
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<com.example.ritamesa.data.model.StudentScheduleItem>>, t: Throwable) {
                Log.e(TAG, "Schedule list load error: ${t.message}", t)
                jadwalList.clear()
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun processApiData(data: StudentScheduleResponse) {
        processScheduleItems(data.items)
    }

    private fun processScheduleItems(items: List<com.example.ritamesa.data.model.StudentScheduleItem>) {
        jadwalList.clear()
        
        items.forEach { item ->
            val teacherName = item.teacher?.user?.name ?: "Guru"
            val roomName = if (item.room != null) " - Ruang ${item.room}" else ""
            
            jadwalList.add(JadwalHarianItem(
                mataPelajaran = item.displaySubjectName,
                sesi = "${item.startTime} - ${item.endTime} ($teacherName)$roomName"
            ))
        }

        adapter.notifyDataSetChanged()
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToRiwayatKehadiran()
            }
        })
    }

    private fun navigateToRiwayatKehadiran() {
        try {
            // Logic to go back to Dashboard or previous activity
            // Since this activity is usually accessed from DashboardSiswaActivity, we go back there.
             val intent = Intent(this, DashboardSiswaActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            super.onBackPressedDispatcher.onBackPressed()
        }
    }
}