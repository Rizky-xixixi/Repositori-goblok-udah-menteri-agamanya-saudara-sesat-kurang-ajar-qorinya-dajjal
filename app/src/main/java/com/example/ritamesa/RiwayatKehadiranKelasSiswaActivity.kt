package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.ritamesa.data.model.StudentAttendanceItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranKelasSiswaActivity : AppCompatActivity() {

    private lateinit var recyclerRiwayat: RecyclerView
    private lateinit var txtHadirCount: TextView
    private lateinit var txtIzinCount: TextView
    private lateinit var txtSakitCount: TextView
    private lateinit var txtAlphaCount: TextView
    private lateinit var txtFilterTanggal: TextView
    private lateinit var btnCalendar: ImageButton

    private val riwayatList = mutableListOf<RiwayatSiswaItem>()

    // Statistics vars
    private var totalHadir = 0
    private var totalIzin = 0
    private var totalSakit = 0
    private var totalAlpha = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.riwayat_kehadiran_kelas_siswa)

        initViews()
        setupRecyclerView()
        setupButtonListeners()
        setupBackPressedHandler()

        // Load data from API
        loadDataFromApi()
    }

    private fun initViews() {
        txtHadirCount = findViewById(R.id.txt_hadir_count)
        txtIzinCount = findViewById(R.id.txt_izin_count)
        txtSakitCount = findViewById(R.id.txt_sakit_count)
        txtAlphaCount = findViewById(R.id.txt_alpha_count)
        
        val txtJumlah: TextView = findViewById(R.id.text_jumlah_siswa)
        txtFilterTanggal = findViewById(R.id.text_filter_tanggal)
        btnCalendar = findViewById(R.id.icon_calendar)

        btnCalendar.visibility = View.GONE
        txtJumlah.text = "Riwayat Kehadiran Anda" 
        
        updateTanggalDisplay()
        
        recyclerRiwayat = findViewById(R.id.recycler_riwayat)
    }

    private fun updateTanggalDisplay() {
        try {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val currentDate = Date()
            val formatted = sdf.format(currentDate)
            val finalDate = if (formatted.isNotEmpty()) formatted[0].uppercaseChar() + formatted.substring(1) else formatted
            txtFilterTanggal.text = finalDate
        } catch (e: Exception) {
            Toast.makeText(this, "Error format tanggal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        recyclerRiwayat.layoutManager = LinearLayoutManager(this)
        recyclerRiwayat.setHasFixedSize(true)
        val adapter = RiwayatSiswaAdapter(riwayatList)
        recyclerRiwayat.adapter = adapter
    }

    private fun loadDataFromApi() {
        Toast.makeText(this, "Memuat data...", Toast.LENGTH_SHORT).show()
        
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        // Fetch current month by default or ALL? Let's fetch ALL (no params)
        apiService.getStudentAttendanceHistory().enqueue(object : Callback<List<StudentAttendanceItem>> {
            override fun onResponse(
                call: Call<List<StudentAttendanceItem>>,
                response: Response<List<StudentAttendanceItem>>
            ) {
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    processApiData(items)
                } else {
                    Toast.makeText(this@RiwayatKehadiranKelasSiswaActivity, "Gagal memuat data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<StudentAttendanceItem>>, t: Throwable) {
                Toast.makeText(this@RiwayatKehadiranKelasSiswaActivity, "Error koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun processApiData(items: List<StudentAttendanceItem>) {
        riwayatList.clear()
        
        totalHadir = 0; totalIzin = 0; totalSakit = 0; totalAlpha = 0

        items.forEach { item ->
            val statusLocal = when (item.status) {
                "present" -> "hadir"
                "late" -> "hadir"
                "sick" -> "sakit"
                "excused" -> "izin"
                "absent" -> "tidak hadir" // Maps to alpha logic in Adapter
                else -> "tidak hadir"
            }

            val keterangan = when (item.status) {
                "present" -> "Hadir Tepat Waktu"
                "late" -> "Terlambat"
                "sick" -> "Sakit"
                "excused" -> "Izin"
                else -> "Tanpa Keterangan"
            }

            // Update stats
            when (statusLocal) {
                "hadir" -> totalHadir++
                "sakit" -> totalSakit++
                "izin" -> totalIzin++
                "tidak hadir" -> totalAlpha++
            }
            
            val timeRange = "${item.schedule?.startTime ?: ""} - ${item.schedule?.endTime ?: ""}"
            
            riwayatList.add(RiwayatSiswaItem(
                id = timeRange, // Replacing "1-2" with time range
                mataPelajaran = item.schedule?.subjectInfo?.name ?: "Unknown",
                keterangan = keterangan,
                status = statusLocal
            ))
        }

        // Update UI
        txtHadirCount.text = totalHadir.toString()
        txtIzinCount.text = totalIzin.toString()
        txtSakitCount.text = totalSakit.toString()
        txtAlphaCount.text = totalAlpha.toString()
        
        recyclerRiwayat.adapter?.notifyDataSetChanged()
        
        val count = riwayatList.size
        findViewById<TextView>(R.id.text_jumlah_siswa).text = "Total Kehadiran: $count"
    }

    private fun setupButtonListeners() {
        findViewById<View>(R.id.btnHome).setOnClickListener { navigateToDashboard() }
        findViewById<View>(R.id.btnAssignment).setOnClickListener { 
            Toast.makeText(this, "Anda sudah di Riwayat Kehadiran", Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.text_navigasi).setOnClickListener { navigateToJadwalHarian() }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { navigateToDashboard() }
        })
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardSiswaActivity::class.java))
        finish()
    }

    private fun navigateToJadwalHarian() {
        val intent = Intent(this, JadwalHarianSiswaActivity::class.java).apply {
            putExtra("IS_PENGURUS", false)
        }
        startActivity(intent)
    }

    private inner class RiwayatSiswaAdapter(
        private val riwayatList: List<RiwayatSiswaItem>
    ) : RecyclerView.Adapter<RiwayatSiswaAdapter.RiwayatViewHolder>() {

        inner class RiwayatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtSession: TextView = itemView.findViewById(R.id.Session)
            val txtMataPelajaran: TextView = itemView.findViewById(R.id.MataPelajaran)
            val txtKeterangan: TextView = itemView.findViewById(R.id.TextKeteranganAbsen)
            val imgBadge: ImageView = itemView.findViewById(R.id.BadgeKehadiran)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_riwayat_kehadiran_siswa, parent, false)
            return RiwayatViewHolder(view)
        }

        override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
            val riwayat = riwayatList[position]
            holder.txtSession.text = riwayat.id // Now shows Time
            holder.txtMataPelajaran.text = riwayat.mataPelajaran
            holder.txtKeterangan.text = riwayat.keterangan

            when (riwayat.status.toLowerCase()) {
                "hadir" -> holder.imgBadge.setImageResource(R.drawable.siswa_hadir_wakel)
                "izin" -> holder.imgBadge.setImageResource(R.drawable.siswa_izin_wakel)
                "sakit" -> holder.imgBadge.setImageResource(R.drawable.siswa_sakit_wakel)
                "tidak hadir" -> holder.imgBadge.setImageResource(R.drawable.siswa_alpha_wakel)
            }
        }

        override fun getItemCount(): Int = riwayatList.size
    }

    data class RiwayatSiswaItem(
        val id: String,
        val mataPelajaran: String,
        val keterangan: String,
        val status: String 
    )
}