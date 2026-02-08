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

    class PercentageFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if (value > 0) "${value.toInt()}%" else ""
        }
    }

    private fun setupBarChart() {
        val chart = barChart ?: return

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 90f))
        entries.add(BarEntry(1f, 60f))
        entries.add(BarEntry(2f, 65f))
        entries.add(BarEntry(3f, 45f))
        entries.add(BarEntry(4f, 55f))
        entries.add(BarEntry(5f, 85f))

        val dataSet = BarDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#F44336"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#00BCD4")
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = PercentageFormatter()

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f
        chart.data = barData

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = 5.5f
        xAxis.setDrawGridLines(false)

        val labels = arrayOf("Hadir", "Izin", "Sakit", "Tidak Hadir", "Terlambat", "Pulang")
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.textSize = 11f
        xAxis.textColor = Color.BLACK
        xAxis.labelRotationAngle = -15f

        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f
        leftAxis.granularity = 20f
        leftAxis.textSize = 12f
        leftAxis.textColor = Color.BLACK
        leftAxis.valueFormatter = PercentageFormatter()
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#E0E0E0")

        chart.axisRight.isEnabled = false

        val legend = chart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textSize = 10f
        legend.textColor = Color.BLACK
        legend.form = Legend.LegendForm.SQUARE
        legend.formSize = 9f
        legend.xEntrySpace = 10f
        legend.yEntrySpace = 2f
        legend.formToTextSpace = 3f

        val legendEntries = arrayOf(
            com.github.mikephil.charting.components.LegendEntry("Hadir", Legend.LegendForm.SQUARE, 9f, 2f, null, Color.parseColor("#4CAF50")),
            com.github.mikephil.charting.components.LegendEntry("Izin", Legend.LegendForm.SQUARE, 9f, 2f, null, Color.parseColor("#FF9800")),
            com.github.mikephil.charting.components.LegendEntry("Sakit", Legend.LegendForm.SQUARE, 9f, 2f, null, Color.parseColor("#2196F3")),
            com.github.mikephil.charting.components.LegendEntry("", Legend.LegendForm.NONE, 0f, 0f, null, Color.TRANSPARENT),
            com.github.mikephil.charting.components.LegendEntry("Tidak Hadir", Legend.LegendForm.SQUARE, 9f, 2f, null, Color.parseColor("#F44336")),
            com.github.mikephil.charting.components.LegendEntry("Terlambat", Legend.LegendForm.SQUARE, 9f, 2f, null, Color.parseColor("#9C27B0")),
            com.github.mikephil.charting.components.LegendEntry("Pulang", Legend.LegendForm.SQUARE, 9f, 2f, null, Color.parseColor("#00BCD4"))
        )
        legend.setCustom(legendEntries)

        chart.description.isEnabled = false
        chart.setFitBars(true)
        chart.animateY(1000)
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.extraBottomOffset = 15f
        chart.extraTopOffset = 15f

        chart.invalidate()
    }
}