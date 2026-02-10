package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.NotificationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class NotifikasiGuruActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "NotifikasiGuru"
    }

    private lateinit var rvHariIni: RecyclerView
    private lateinit var tvHariTanggal: TextView 

    private lateinit var btnHome: ImageButton
    private lateinit var btnCalendar: ImageButton
    private lateinit var btnChart: ImageButton
    private lateinit var btnNotif: ImageButton

    private lateinit var adapterHariIni: NotifikasiAdapter
    private val dataHariIni = mutableListOf<Map<String, Any>>()
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notifikasi_guru)

        initViews()
        setupFooterNavigation()
        setupRecyclerView()
        
        updateTanggalRealTime()
        loadDataFromApi()
    }

    private fun initViews() {
        rvHariIni = findViewById(R.id.rvNotifHariIni)
        tvHariTanggal = findViewById(R.id.tvharitanggal) 

        btnHome = findViewById(R.id.btnHome)
        btnCalendar = findViewById(R.id.btnCalendar)
        btnChart = findViewById(R.id.btnChart)
        btnNotif = findViewById(R.id.btnNotif)
    }

    private fun updateTanggalRealTime() {
        try {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            val formattedDate = sdf.format(calendar.time)
            val finalDate = if (formattedDate.isNotEmpty()) {
                formattedDate[0].uppercaseChar() + formattedDate.substring(1)
            } else {
                formattedDate
            }
            tvHariTanggal.text = finalDate
        } catch (e: Exception) {
            tvHariTanggal.text = "Tanggal error"
        }
    }

    private fun setupFooterNavigation() {
        btnHome.setOnClickListener {
            startActivity(Intent(this, DashboardGuruActivity::class.java))
        }
        btnCalendar.setOnClickListener {
            startActivity(Intent(this, RiwayatKehadiranGuruActivity::class.java))
        }
        btnChart.setOnClickListener {
            startActivity(Intent(this, TindakLanjutGuruActivity::class.java))
        }
        btnNotif.setOnClickListener {
            refreshNotifications()
        }
    }

    private fun refreshNotifications() {
        updateTanggalRealTime()
        loadDataFromApi()
    }

    private fun setupRecyclerView() {
        adapterHariIni = NotifikasiAdapter(dataHariIni, true) 
        rvHariIni.layoutManager = LinearLayoutManager(this)
        rvHariIni.adapter = adapterHariIni
    }

    private fun loadDataFromApi() {
        if (isLoading) return
        isLoading = true
        
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.getNotifications().enqueue(object : Callback<NotificationResponse> {
            override fun onResponse(
                call: Call<NotificationResponse>,
                response: Response<NotificationResponse>
            ) {
                isLoading = false
                if (response.isSuccessful) {
                    response.body()?.let { processApiData(it) }
                } else {
                    Log.d(TAG, "API error: ${response.code()}")
                    // Show empty state instead of error toast
                }
            }

            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                isLoading = false
                Log.e(TAG, "Network error", t)
                // Silently fail - user can pull to refresh
            }
        })
    }

    private fun processApiData(data: NotificationResponse) {
        dataHariIni.clear()
        
        data.notifications.forEach { item ->
            dataHariIni.add(mapOf(
                "type" to item.type,
                "message" to item.message,
                "detail" to item.detail,
                "time" to item.time,
                "date" to data.date
            ))
        }
        
        adapterHariIni.notifyDataSetChanged()
    }
}