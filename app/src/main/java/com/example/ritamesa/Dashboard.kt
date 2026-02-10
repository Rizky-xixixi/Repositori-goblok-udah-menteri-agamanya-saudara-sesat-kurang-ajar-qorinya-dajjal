package com.example.ritamesa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class Dashboard : AppCompatActivity() {

    private lateinit var tvTanggal: TextView
    private lateinit var tvJam: TextView
    private lateinit var tvTotalSiswa: TextView
    private lateinit var tvTotalGuru: TextView
    private lateinit var tvTotalJurusan: TextView
    private lateinit var tvTotalKelas: TextView
    private lateinit var tvHadir: TextView
    private lateinit var tvTerlambat: TextView
    private lateinit var tvIzin: TextView
    private lateinit var tvSakit: TextView
    private lateinit var tvAlpha: TextView
    private lateinit var tvPulang: TextView
    private lateinit var barChart: BarChart
    private lateinit var tvDateCard: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = true
    private var lastUpdateTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // ===== INISIALISASI VIEW =====
        tvTanggal = findViewById(R.id.textView9)
        tvJam = findViewById(R.id.textView21)
        tvTotalSiswa = findViewById(R.id.textView12)
        tvTotalGuru = findViewById(R.id.textView16)
        tvTotalJurusan = findViewById(R.id.textView17)
        tvTotalKelas = findViewById(R.id.textView18)
        tvDateCard = findViewById(R.id.textView20)

        tvHadir = findViewById(R.id.tvTepatWaktuDashboard)
        tvTerlambat = findViewById(R.id.tvTerlambatDashboard)
        tvIzin = findViewById(R.id.tvIzinDashboard)
        tvSakit = findViewById(R.id.tvSakitDashboard)
        tvAlpha = findViewById(R.id.tvAlphaDashboard)
        tvPulang = findViewById(R.id.tvPulang)

        barChart = findViewById(R.id.barChartBulanan)

        updateTanggalJam()
        updateDataStatistik()

        // ===== PROFILE POPUP MENU 2 ITEM =====
        findViewById<ImageButton>(R.id.profile).setOnClickListener { view ->
            showProfileMenu(view)
        }

        // ===== NAVIGASI LAINNYA =====
        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            // HOME - sudah di dashboard
        }

        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            startActivity(Intent(this, RekapKehadiranSiswa::class.java))
        }

        findViewById<ImageButton>(R.id.imageButton4).setOnClickListener {
            startActivity(Intent(this, StatistikKehadiran::class.java))
        }

        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            startActivity(Intent(this, NotifikasiSemua::class.java))
        }

        findViewById<ImageButton>(R.id.imageButton87).setOnClickListener {
            startActivity(Intent(this, RiwayatKehadiranSiswa1::class.java))
        }

        findViewById<ImageButton>(R.id.imageView16).setOnClickListener {
            startActivity(Intent(this, TotalGuru::class.java))
        }

        findViewById<ImageButton>(R.id.imageView17).setOnClickListener {
            startActivity(Intent(this, TotalJurusan::class.java))
        }

        findViewById<ImageButton>(R.id.imageView18).setOnClickListener {
            startActivity(Intent(this, TotalKelas::class.java))
        }

        findViewById<ImageButton>(R.id.imageView6).setOnClickListener {
            startActivity(Intent(this, TotalSiswa::class.java))
        }
    }

    // ===== PROFILE MENU DENGAN 2 PILIHAN =====
    private fun showProfileMenu(view: android.view.View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.profile_simple, popupMenu.menu)

        // Custom background untuk menu
        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popupMenu)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_logout -> {
                    showLogoutConfirmation()
                    true
                }
                R.id.menu_cancel -> {
                    // Do nothing, dismiss menu
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showLogoutConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi Logout")
            .setMessage("Yakin ingin logout dari akun Admin?")
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
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
    }

    // ===== FUNGSI LAINNYA (TETAP SAMA) =====
    private fun updateDataStatistik() {
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)
        
        apiService.getAdminDashboard().enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.AdminDashboardResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.ritamesa.data.model.AdminDashboardResponse>,
                response: retrofit2.Response<com.example.ritamesa.data.model.AdminDashboardResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        tvTotalSiswa.text = data.totalSiswa.toString()
                        tvTotalGuru.text = data.totalGuru.toString()
                        tvTotalJurusan.text = data.totalJurusan.toString()
                        tvTotalKelas.text = data.totalKelas.toString()

                        // Handle nullable attendance stats
                        val attendance = data.attendance
                        tvHadir.text = (attendance?.hadir ?: 0).toString()
                        tvIzin.text = (attendance?.izin ?: 0).toString()
                        tvSakit.text = (attendance?.sakit ?: 0).toString()
                        tvAlpha.text = (attendance?.alpha ?: 0).toString()
                        tvTerlambat.text = (attendance?.terlambat ?: 0).toString()
                        tvPulang.text = (attendance?.pulang ?: 0).toString()

                        setupChartHariIni()
                    }
                } else {
                    Toast.makeText(this@Dashboard, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.AdminDashboardResponse>, t: Throwable) {
                Toast.makeText(this@Dashboard, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupChartHariIni() {
        val hadir = tvHadir.text.toString().toIntOrNull() ?: 0
        val izin = tvIzin.text.toString().toIntOrNull() ?: 0
        val pulang = tvPulang.text.toString().toIntOrNull() ?: 0
        val tidakHadir = tvAlpha.text.toString().toIntOrNull() ?: 0
        val sakit = tvSakit.text.toString().toIntOrNull() ?: 0

        val labels = arrayOf("Hadir", "Izin", "Pulang", "Tidak Hadir", "Sakit")
        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(0f, hadir.toFloat()))
        entries.add(BarEntry(1f, izin.toFloat()))
        entries.add(BarEntry(2f, pulang.toFloat()))
        entries.add(BarEntry(3f, tidakHadir.toFloat()))
        entries.add(BarEntry(4f, sakit.toFloat()))

        val warnaStatus = listOf(
            Color.parseColor("#4CAF50"),
            Color.parseColor("#A1B324"),
            Color.parseColor("#1976D2"),
            Color.parseColor("#B1170F"),
            Color.parseColor("#741781")
        )

        val dataSet = BarDataSet(entries, "Kehadiran Hari Ini").apply {
            colors = warnaStatus
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.5f
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })
        }

        barChart.apply {
            data = barData
            description.isEnabled = false

            xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return labels.getOrNull(value.toInt()) ?: ""
                    }
                }
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelCount = labels.size
                textSize = 10f
                axisMinimum = -0.5f
                axisMaximum = labels.size - 0.5f
            }

            axisLeft.apply {
                axisMinimum = 0f
                granularity = 10f
                setDrawGridLines(true)
                textSize = 10f
                val maxValue = maxOf(hadir, izin, pulang, tidakHadir, sakit)
                axisMaximum = (maxValue + 10).toFloat()
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 10f
            legend.formSize = 10f

            setTouchEnabled(true)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setDragEnabled(false)
            setScaleEnabled(false)
            setDoubleTapToZoomEnabled(false)

            animateY(1000)
            invalidate()
        }
    }

    private fun updateTanggalJam() {
        isRunning = true
        handler.post(object : Runnable {
            override fun run() {
                if (!isRunning) return

                val tanggalFormat = SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale("id", "ID")
                )
                val jamFormat = SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.getDefault()
                )

                val now = Date()
                val tanggal = tanggalFormat.format(now)
                val jam = jamFormat.format(now)

                tvTanggal.text = tanggal
                tvJam.text = jam
                tvDateCard.text = tanggal

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= 30000) {
                    updateDataStatistik()
                    lastUpdateTime = currentTime
                }

                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        updateDataStatistik()
    }
}