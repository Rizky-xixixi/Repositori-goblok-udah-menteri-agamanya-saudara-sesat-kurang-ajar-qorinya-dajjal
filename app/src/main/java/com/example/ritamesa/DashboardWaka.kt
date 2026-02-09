package com.example.ritamesa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

class DashboardWaka : AppCompatActivity() {

    private val TAG = "DashboardWaka"
    private var barChart: BarChart? = null

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateDateTime()
            val calendar = Calendar.getInstance()
            val seconds = calendar.get(Calendar.SECOND)
            val milliseconds = calendar.get(Calendar.MILLISECOND)
            val delayUntilNextMinute = (60000 - (seconds * 1000 + milliseconds))
            handler.postDelayed(this, delayUntilNextMinute.toLong())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "=== DashboardWaka onCreate MULAI ===")

        try {
            enableEdgeToEdge()
            setContentView(R.layout.dashboard_waka)
            Log.d(TAG, "✓ Layout berhasil di-inflate")
            Toast.makeText(this, "Dashboard Waka dibuka!", Toast.LENGTH_SHORT).show()

            try {
                dateTextView = findViewById(R.id.textView9)
                timeTextView = findViewById(R.id.textView21)
                updateDateTime()
                handler.post(updateTimeRunnable)
                loadDashboardData() // Load data API
            } catch (e: Exception) {
                Log.w(TAG, "⚠ TextView untuk date/time tidak ditemukan", e)
            }

            try {
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠ Window insets listener error (not critical)", e)
            }

            try {
                barChart = findViewById(R.id.barChartBulanan)
                if (barChart != null) {
                    Log.d(TAG, "✓ BarChart ditemukan")
                    setupBarChart()
                    Log.d(TAG, "✓ BarChart berhasil di-setup")
                } else {
                    Log.e(TAG, "✗ BarChart NULL - ID mungkin salah di XML")
                    Toast.makeText(this, "Warning: Chart tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ ERROR saat setup BarChart", e)
                Toast.makeText(this, "Warning: Chart error - ${e.message}", Toast.LENGTH_LONG).show()
            }

            try {
                setupNavigation()
                Log.d(TAG, "✓ Navigation buttons setup")
            } catch (e: Exception) {
                Log.e(TAG, "✗ ERROR saat setup navigation", e)
                Toast.makeText(this, "Warning: Some buttons may not work", Toast.LENGTH_SHORT).show()
            }

            // ===== TAMBAHKAN POPUP MENU DI PROFILE =====
            try {
                val profileButton = findViewById<ImageButton>(R.id.profile)
                profileButton.setOnClickListener { view ->
                    showProfileMenu(view)
                }
                
                // Load profile image from session
                val sessionManager = com.example.ritamesa.data.pref.SessionManager(this)
                val photoUrl = sessionManager.getPhotoUrl()
                
                if (!photoUrl.isNullOrEmpty()) {
                    com.bumptech.glide.Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.profile_guru) // Fallback
                        .error(R.drawable.profile_guru)
                        .into(profileButton)
                }
                
                Log.d(TAG, "✓ Profile button setup dengan popup menu")
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error setup profile button", e)
            }

        } catch (e: Exception) {
            Log.e(TAG, "✗✗✗ FATAL ERROR di onCreate", e)
            Toast.makeText(this, "ERROR: ${e.message}", Toast.LENGTH_LONG).show()
        }

        Log.d(TAG, "=== DashboardWaka onCreate SELESAI ===")
    }

    // ===== PROFILE MENU DENGAN 2 PILIHAN =====
    private fun showProfileMenu(view: android.view.View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.profile_simple, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    showLogoutConfirmation()
                    true
                }
                R.id.menu_cancel -> {
                    Toast.makeText(this, "Menu dibatalkan", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showLogoutConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Logout Waka")
            .setMessage("Yakin ingin logout dari akun Wakil Kepala Sekolah?")
            .setPositiveButton("Ya, Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        val intent = Intent(this, LoginAwal::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Logout Waka berhasil", Toast.LENGTH_SHORT).show()
    }

    private fun updateDateTime() {
        try {
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            val formattedDate = dateFormat.format(currentDate)
            val formattedTime = timeFormat.format(currentDate)

            if (this::dateTextView.isInitialized) {
                dateTextView.text = formattedDate
            }

            if (this::timeTextView.isInitialized) {
                timeTextView.text = formattedTime
            }

            try {
                val tvRiwayatDate: TextView? = findViewById(R.id.textView20)
                tvRiwayatDate?.text = formattedDate
            } catch (e: Exception) {
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error updating date/time", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
        Log.d(TAG, "DashboardWaka destroyed, handler stopped")
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onResume() {
        super.onResume()
        updateDateTime()
        handler.post(updateTimeRunnable)
    }

    private fun setupNavigation() {
        try {
            val btnDataRekap: ImageButton? = findViewById(R.id.imageButton3)
            if (btnDataRekap != null) {
                btnDataRekap.setOnClickListener {
                    try {
                        Log.d(TAG, "Button Data Rekap diklik")
                        val intent = Intent(this, DataRekapKehadiranGuru::class.java)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error navigasi ke DataRekapKehadiranGuru", e)
                        Toast.makeText(this, "Halaman belum tersedia: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d(TAG, "✓ imageButton3 berhasil di-setup")
            } else {
                Log.w(TAG, "imageButton3 tidak ditemukan di layout")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error setup imageButton3", e)
        }

        try {
            val btnStatistik: ImageButton? = findViewById(R.id.imageButton5)
            if (btnStatistik != null) {
                btnStatistik.setOnClickListener {
                    try {
                        Log.d(TAG, "Button Statistik diklik")
                        val intent = Intent(this, StatistikWakaa::class.java)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error navigasi ke StatistikWakaa", e)
                        Toast.makeText(this, "Halaman belum tersedia: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d(TAG, "✓ imageButton5 berhasil di-setup")
            } else {
                Log.w(TAG, "imageButton5 tidak ditemukan di layout")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error setup imageButton5", e)
        }

        try {
            val btnJadwalGuru: ImageButton? = findViewById(R.id.imageButton4)
            if (btnJadwalGuru != null) {
                btnJadwalGuru.setOnClickListener {
                    try {
                        Log.d(TAG, "Button Jadwal Guru diklik - Navigasi dimulai")
                        val intent = Intent(this@DashboardWaka, JadwalPembelajaranGuru::class.java)
                        startActivity(intent)
                        Log.d(TAG, "✓ Navigasi ke JadwalPembelajaranGuru berhasil")
                    } catch (e: Exception) {
                        Log.e(TAG, "✗ Error navigasi ke JadwalPembelajaranGuru", e)
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                Log.d(TAG, "✓ imageButton4 berhasil di-setup untuk JadwalPembelajaranGuru")
            } else {
                Log.e(TAG, "✗ imageButton4 NULL - tidak ditemukan di layout XML")
                Toast.makeText(this, "Error: Button Jadwal tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error setup imageButton4", e)
            Toast.makeText(this, "Error setup button: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        try {
            val btnRiwayatKehadiran: ImageButton? = findViewById(R.id.imageButton87)
            if (btnRiwayatKehadiran != null) {
                btnRiwayatKehadiran.setOnClickListener {
                    try {
                        Log.d(TAG, "Button Riwayat Kehadiran diklik")
                        val intent = Intent(this, RiwayatKehadiranGuru1::class.java)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error navigasi ke RiwayatKehadiranGuru1", e)
                        Toast.makeText(this, "Halaman belum tersedia: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d(TAG, "✓ imageButton87 berhasil di-setup")
            } else {
                Log.w(TAG, "imageButton87 tidak ditemukan di layout")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error setup imageButton87", e)
        }

        try {
            val btnLihatDispen: ImageButton? = findViewById(R.id.btnLihatDispen)
            if (btnLihatDispen != null) {
                btnLihatDispen.setOnClickListener {
                    try {
                        Log.d(TAG, "Button Lihat Dispensasi diklik")
                        val intent = Intent(this, PersetujuanDispensasi::class.java)
                        startActivity(intent)
                        Log.d(TAG, "✓ Navigasi ke PersetujuanDispensasi berhasil")
                    } catch (e: Exception) {
                        Log.e(TAG, "✗ Error navigasi ke PersetujuanDispensasi", e)
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                Log.d(TAG, "✓ btnLihatDispen berhasil di-setup")
            } else {
                Log.w(TAG, "⚠ btnLihatDispen tidak ditemukan di layout")
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error setup btnLihatDispen", e)
            Toast.makeText(this, "Error setup button dispensasi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDashboardData() {
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)
        
        Log.d(TAG, "Loading Waka Dashboard data...")
        apiService.getWakaDashboard().enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.WakaDashboardResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.ritamesa.data.model.WakaDashboardResponse>,
                response: retrofit2.Response<com.example.ritamesa.data.model.WakaDashboardResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        Log.d(TAG, "Data received: ${data.statistik}")
                        updateChart(data.statistik)
                        // Could also update date here using data.date
                    } else {
                        Log.e(TAG, "Response body is null")
                    }
                } else {
                    Log.e(TAG, "Error: ${response.code()} ${response.message()}")
                    Toast.makeText(this@DashboardWaka, "Gagal memuat data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.WakaDashboardResponse>, t: Throwable) {
                Log.e(TAG, "Network Error", t)
                Toast.makeText(this@DashboardWaka, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupBarChart() {
        // Initial setup empty or loading
        val chart = barChart ?: return
        chart.setNoDataText("Memuat data...")
        chart.invalidate()
    }

    private fun updateChart(statistik: com.example.ritamesa.data.model.WakaStatistik) {
        val chart = barChart ?: return

        val entries = ArrayList<BarEntry>()
        // Labels: "Hadir", "Izin", "Sakit", "Tidak Hadir", "Terlambat", "Pulang"
        // Index: 0, 1, 2, 3, 4, 5
        
        entries.add(BarEntry(0f, statistik.hadir.toFloat()))
        entries.add(BarEntry(1f, statistik.izin.toFloat()))
        entries.add(BarEntry(2f, statistik.sakit.toFloat()))
        entries.add(BarEntry(3f, statistik.alpha.toFloat()))
        entries.add(BarEntry(4f, statistik.terlambat.toFloat()))
        entries.add(BarEntry(5f, statistik.pulang.toFloat()))

        val dataSet = BarDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"), // Hadir
            Color.parseColor("#FF9800"), // Izin
            Color.parseColor("#2196F3"), // Sakit
            Color.parseColor("#F44336"), // Alpha
            Color.parseColor("#9C27B0"), // Terlambat
            Color.parseColor("#00BCD4")  // Pulang
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value > 0) value.toInt().toString() else ""
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f
        chart.data = barData

        // Configure axes similar to previous setup
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = 5.5f
        xAxis.setDrawGridLines(false)

        val labels = arrayOf("Hadir", "Izin", "Sakit", "Alpha", "Late", "Pulang")
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.textSize = 11f
        xAxis.textColor = Color.BLACK
        xAxis.labelRotationAngle = -15f

        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.textSize = 10f
        leftAxis.setDrawGridLines(true)
        
        // Dynamic Maximum Y
        val maxValue = listOf(statistik.hadir, statistik.izin, statistik.sakit, statistik.alpha, statistik.terlambat, statistik.pulang).maxOrNull() ?: 10
        leftAxis.axisMaximum = (maxValue + 5).toFloat()

        chart.axisRight.isEnabled = false
        
        chart.description.isEnabled = false
        chart.legend.isEnabled = false // Labels are enough on axis
        
        chart.notifyDataSetChanged()
        chart.invalidate()
        chart.animateY(1000)
    }
}