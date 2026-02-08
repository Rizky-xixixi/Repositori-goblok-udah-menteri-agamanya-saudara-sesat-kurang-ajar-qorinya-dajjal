package com.example.ritamesa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import kotlin.random.Random

class StatistikKehadiran : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart

    private var selectedRole = "Semua"
    private var selectedTime = "Bulanan"

    // Data untuk 6 kategori
    private var hadir = 0f
    private var terlambat = 0f
    private var izin = 0f
    private var sakit = 0f
    private var pulang = 0f
    private var alpha = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.statistik_kehadiran)

        // ===== CHART =====
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)

        // ===== FILTER =====
        val txtRole = findViewById<TextView>(R.id.textView2)
        val btnRole = findViewById<ImageButton>(R.id.imageButton1)

        val txtTime = findViewById<TextView>(R.id.textView3)
        val btnTime = findViewById<ImageButton>(R.id.imageButton2)

        // ===== STATISTIK CARD =====
        val tvHadirPercent = findViewById<TextView>(R.id.textView20)
        val tvTerlambatPercent = findViewById<TextView>(R.id.textView78)
        val tvIzinPercent = findViewById<TextView>(R.id.textView90)
        val tvAlphaPercent = findViewById<TextView>(R.id.textView50)

        // ===== BOTTOM NAV =====
        val btnHome = findViewById<ImageButton>(R.id.imageButton27)
        val btnPeople = findViewById<ImageButton>(R.id.imageButton3)
        val btnStatistik = findViewById<ImageButton>(R.id.imageButton4)
        val btnNotif = findViewById<ImageButton>(R.id.imageButton6)

        // ===== NAVIGATION ACTION =====
        // ===== NAVIGATION ACTION =====
        val userRole = intent.getStringExtra("ROLE")

        btnHome.setOnClickListener {
            if (userRole == "TEACHER") {
                val intent = Intent(this, DashboardGuruActivity::class.java)
                startActivity(intent)
            } else {
                startActivity(Intent(this, Dashboard::class.java))
            }
            finish()
        }

        btnPeople.setOnClickListener {
            startActivity(Intent(this, RekapKehadiranSiswa::class.java))
            finish()
        }

        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotifikasiSemua::class.java))
            finish()
        }

        // ===== DROPDOWN ROLE =====
        btnRole.setOnClickListener {
            val popup = PopupMenu(this, btnRole)
            popup.menu.add("Semua")
            popup.menu.add("Guru")
            popup.menu.add("Siswa")

            popup.setOnMenuItemClickListener {
                selectedRole = it.title.toString()
                txtRole.text = selectedRole
                updateAllChart()
                updateStatistikCards(tvHadirPercent, tvTerlambatPercent, tvIzinPercent, tvAlphaPercent)
                true
            }
            popup.show()
        }

        // ===== DROPDOWN TIME =====
        btnTime.setOnClickListener {
            val popup = PopupMenu(this, btnTime)
            popup.menu.add("Harian")
            popup.menu.add("Mingguan")
            popup.menu.add("Bulanan")
            popup.menu.add("Tahunan")

            popup.setOnMenuItemClickListener {
                selectedTime = it.title.toString()
                txtTime.text = selectedTime
                updateAllChart()
                updateStatistikCards(tvHadirPercent, tvTerlambatPercent, tvIzinPercent, tvAlphaPercent)
                true
            }
            popup.show()
        }

        setupBarChart()
        setupPieChart()
        updateAllChart()
        updateStatistikCards(tvHadirPercent, tvTerlambatPercent, tvIzinPercent, tvAlphaPercent)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // =================================================
    // BAR CHART (6 KATEGORI)
    // =================================================
    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
        barChart.legend.textSize = 12f
        barChart.legend.formSize = 12f
        barChart.axisRight.isEnabled = false
        barChart.setDrawGridBackground(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Hadir", "Terlambat", "Izin", "Sakit", "Pulang", "Alpha"))
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 6
        xAxis.textSize = 11f

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.granularity = 10f

        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e ?: return
                val labels = listOf("Hadir", "Terlambat", "Izin", "Sakit", "Pulang", "Alpha")
                val label = labels.getOrNull(e.x.toInt()) ?: "Data"
                Toast.makeText(
                    this@StatistikKehadiran,
                    "$label: ${e.y.toInt()}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected() {}
        })
    }

    // =================================================
    // PIE CHART (6 KATEGORI)
    // =================================================
    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 45f
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(11f)
        pieChart.legend.isEnabled = true
        pieChart.legend.textSize = 12f
        pieChart.legend.formSize = 12f
        pieChart.setDrawEntryLabels(false) // Nonaktifkan label di dalam pie
    }

    // =================================================
    // UPDATE ALL CHARTS
    // =================================================
    private fun updateAllChart() {
        // generateDummyData() // Removed
        loadDataFromApi()
    }

    private fun loadDataFromApi() {
        val sessionManager = com.example.ritamesa.data.pref.SessionManager(this)
        val role = sessionManager.getUserRole() // Fixed method name
        
        // Use current month/year or selected
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.get(java.util.Calendar.MONTH) + 1 // 1-12
        val year = calendar.get(java.util.Calendar.YEAR)
        
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)

        Toast.makeText(this, "Memuat data statistik...", Toast.LENGTH_SHORT).show()

        if (role == "teacher") { // Assuming "teacher" is the role string
             apiService.getMonthlyStatistics(month, year).enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.TeacherStatisticsResponse> {
                override fun onResponse(call: retrofit2.Call<com.example.ritamesa.data.model.TeacherStatisticsResponse>, response: retrofit2.Response<com.example.ritamesa.data.model.TeacherStatisticsResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { processTeacherApiData(it) }
                    } else {
                         Toast.makeText(this@StatistikKehadiran, "Gagal memuat statistik", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.TeacherStatisticsResponse>, t: Throwable) {
                    Toast.makeText(this@StatistikKehadiran, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Waka / Admin - Use Waka Summary
            // Construct from/to dates for the month
            val start = "$year-${String.format("%02d", month)}-01"
            val end = "$year-${String.format("%02d", month)}-${calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)}"
            
            apiService.getWakaAttendanceSummary(from = start, to = end).enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse> {
                override fun onResponse(call: retrofit2.Call<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse>, response: retrofit2.Response<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { processWakaApiData(it) }
                    } else {
                         Toast.makeText(this@StatistikKehadiran, "Gagal memuat statistik waka", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse>, t: Throwable) {
                    Toast.makeText(this@StatistikKehadiran, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun processTeacherApiData(data: com.example.ritamesa.data.model.TeacherStatisticsResponse) {
        hadir = data.summary.hadir.toFloat()
        sakit = data.summary.sakit.toFloat()
        izin = data.summary.izin.toFloat()
        alfa = data.summary.alfa.toFloat()
        terlambat = data.summary.terlambat.toFloat()
        pulang = 0f
        
        updateBarChart()
        updatePieChart()
        updateCards()
    }

    private fun processWakaApiData(data: com.example.ritamesa.data.model.WakaAttendanceSummaryResponse) {
        // Reset
        hadir = 0f; sakit = 0f; izin = 0f; alfa = 0f; terlambat = 0f; pulang = 0f
        
        data.statusSummary.forEach { item ->
            when(item.status.lowercase()) {
                "present", "hadir" -> hadir += item.total
                "sick", "sakit" -> sakit += item.total
                "excused", "izin" -> izin += item.total
                "absent", "alpha" -> alfa += item.total
                "late", "terlambat" -> terlambat += item.total
                "pulang", "izin_pulang" -> pulang += item.total
                else -> {}
            }
        }
        
        updateBarChart()
        updatePieChart()
        updateCards()
    }
    
    private fun updateCards() {
        val tvHadirPercent = findViewById<TextView>(R.id.textView20)
        val tvTerlambatPercent = findViewById<TextView>(R.id.textView78)
        val tvIzinPercent = findViewById<TextView>(R.id.textView90)
        val tvAlphaPercent = findViewById<TextView>(R.id.textView50)
        updateStatistikCards(tvHadirPercent, tvTerlambatPercent, tvIzinPercent, tvAlphaPercent)
    }

    // Removed generateDummyData()

    private fun updateBarChart() {
        // ... (Keep existing setup but use updated variables)
        val entries = listOf(
            BarEntry(0f, hadir),
            BarEntry(1f, terlambat),
            BarEntry(2f, izin),
            BarEntry(3f, sakit),
            BarEntry(4f, pulang),
            BarEntry(5f, alpha)
        )

        val dataSet = BarDataSet(entries, "Statistik Kehadiran")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),  // Hijau
            Color.parseColor("#FF9800"),  // Oranye
            Color.parseColor("#2196F3"),  // Biru
            Color.parseColor("#9C27B0"),  // Ungu
            Color.parseColor("#00BCD4"),  // Cyan
            Color.parseColor("#F44336")   // Merah
        )

        dataSet.valueTextSize = 11f
        dataSet.valueTextColor = Color.BLACK
        dataSet.setDrawValues(true)

        val data = BarData(dataSet)
        data.barWidth = 0.5f

        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        })

        barChart.data = data
        
        val maxValue = listOf(hadir, terlambat, izin, sakit, pulang, alpha).maxOrNull() ?: 100f
        barChart.axisLeft.axisMaximum = maxValue + (maxValue * 0.1f) // Add 10% buffering

        barChart.animateY(800)
        barChart.invalidate()
    }
    
    // ... (Keep existing updatePieChart and others)


    private fun updatePieChart() {
        val entries = listOf(
            PieEntry(hadir, "Hadir"),
            PieEntry(terlambat, "Terlambat"),
            PieEntry(izin, "Izin"),
            PieEntry(sakit, "Sakit"),
            PieEntry(pulang, "Pulang"),
            PieEntry(alpha, "Alpha")
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),  // Hijau - Hadir
            Color.parseColor("#FF9800"),  // Oranye - Terlambat
            Color.parseColor("#2196F3"),  // Biru - Izin
            Color.parseColor("#9C27B0"),  // Ungu - Sakit
            Color.parseColor("#00BCD4"),  // Cyan - Pulang
            Color.parseColor("#F44336")   // Merah - Alpha
        )

        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)
        data.setDrawValues(true)

        // Format nilai persentase
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val total = entries.sumOf { it.value.toDouble() }.toFloat()
                val percentage = (value / total) * 100
                return "${"%.1f".format(percentage)}%"
            }
        })

        pieChart.data = data

        // Set center text
        val total = entries.sumOf { it.value.toDouble() }.toInt()
        pieChart.centerText = "Total\n$total"
        pieChart.setCenterTextSize(14f)
        pieChart.setCenterTextColor(Color.BLACK)

        pieChart.animateY(800)
        pieChart.invalidate()
    }

    // =================================================
    // UPDATE STATISTIK CARDS
    // =================================================
    private fun updateStatistikCards(
        tvHadir: TextView,
        tvTerlambat: TextView,
        tvIzin: TextView,
        tvAlpha: TextView
    ) {
        val total = hadir + terlambat + izin + sakit + pulang + alpha

        val hadirPercent = if (total > 0) (hadir / total * 100) else 0f
        val terlambatPercent = if (total > 0) (terlambat / total * 100) else 0f
        val izinPercent = if (total > 0) (izin / total * 100) else 0f
        val alphaPercent = if (total > 0) (alpha / total * 100) else 0f

        tvHadir.text = "${"%.1f".format(hadirPercent)}%"
        tvTerlambat.text = "${"%.1f".format(terlambatPercent)}%"
        tvIzin.text = "${"%.1f".format(izinPercent)}%"
        tvAlpha.text = "${"%.1f".format(alphaPercent)}%"
    }
}