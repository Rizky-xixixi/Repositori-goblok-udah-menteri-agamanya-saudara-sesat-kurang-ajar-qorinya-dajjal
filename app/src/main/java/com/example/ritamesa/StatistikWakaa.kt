package com.example.ritamesa

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatistikWakaa : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private var currentMode: String = "SEMUA"
    private var currentTimeRange: String = "HARIAN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.statistik_wakaa)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)
        setupBarChart()
        setupPieChart()

        setupNavigation()
        setupFilterDropdown()
        setupTimeFilterDropdown()
        setupExportButton()
    }

    private fun setupFilterDropdown() {
        val filterButton: ImageButton = findViewById(R.id.imageButton8)

        filterButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)

            popupMenu.menuInflater.inflate(R.menu.menu_data_rekap, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_semua -> {
                        showSemuaStatistics()
                        true
                    }
                    R.id.menu_guru -> {
                        showGuruStatistics()
                        true
                    }
                    R.id.menu_siswa -> {
                        showSiswaStatistics()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
    }

    private fun setupTimeFilterDropdown() {
        val timeFilterButton: ImageButton = findViewById(R.id.imageButton9)

        timeFilterButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)

            popupMenu.menuInflater.inflate(R.menu.menu_filter_time, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_harian -> {
                        currentTimeRange = "HARIAN"
                        updateTimeFilterUI()
                        updateChartsBasedOnTimeRange()
                        Toast.makeText(this, "Menampilkan data Harian", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_mingguan -> {
                        currentTimeRange = "MINGGUAN"
                        updateTimeFilterUI()
                        updateChartsBasedOnTimeRange()
                        Toast.makeText(this, "Menampilkan data Mingguan", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_bulanan -> {
                        currentTimeRange = "BULANAN"
                        updateTimeFilterUI()
                        updateChartsBasedOnTimeRange()
                        Toast.makeText(this, "Menampilkan data Bulanan", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
    }

    private fun updateTimeFilterUI() {
        val textView12: android.widget.TextView = findViewById(R.id.textView12)
        textView12.text = when (currentTimeRange) {
            "HARIAN" -> "Harian"
            "MINGGUAN" -> "Mingguan"
            "BULANAN" -> "Bulanan"
            else -> "Harian"
        }
    }

    private val apiService by lazy {
        com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)
    }

    // Storage for fetched data
    private var statsSemua = mutableMapOf<String, Int>() // "hadir" -> count
    private var statsGuru = mutableMapOf<String, Int>()
    private var statsSiswa = mutableMapOf<String, Int>()

    private fun fetchStatistics() {
        val (startDate, endDate) = getDateRange()
        
        // Fetch Student Data
        apiService.getWakaAttendanceSummary(from = startDate, to = endDate, type = "student").enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse> {
            override fun onResponse(call: retrofit2.Call<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse>, response: retrofit2.Response<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse>) {
                if (response.isSuccessful) {
                    processStats(response.body()?.statusSummary, "student")
                    checkAndUpdateCharts()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse>, t: Throwable) {
                // Log error
            }
        })

        // Fetch Teacher Data
        apiService.getWakaAttendanceSummary(from = startDate, to = endDate, type = "teacher").enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse> {
            override fun onResponse(call: retrofit2.Call<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse>, response: retrofit2.Response<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse>) {
                if (response.isSuccessful) {
                    processStats(response.body()?.statusSummary, "teacher")
                    checkAndUpdateCharts()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.WakaAttendanceSummaryResponse>, t: Throwable) {
                // Log error
            }
        })
    }

    private fun getDateRange(): Pair<String, String> {
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val end = dateFormat.format(calendar.time)
        
        when (currentTimeRange) {
            "HARIAN" -> {
                // Same day
            }
            "MINGGUAN" -> {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
            }
            "BULANAN" -> {
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        val start = dateFormat.format(calendar.time)
        return Pair(start, end)
    }

    private fun processStats(summary: List<com.example.ritamesa.data.model.StatusSummaryItem>?, type: String) {
        val map = if (type == "teacher") statsGuru else statsSiswa
        map.clear()
        summary?.forEach { 
             val key = it.status.lowercase()
             val normalizedKey = when(key) {
                 "present", "hadir" -> "hadir"
                 "sick", "sakit" -> "sakit"
                 "excused", "izin" -> "izin"
                 "absent", "alpha" -> "alpha"
                 "late", "terlambat" -> "terlambat"
                 else -> "alpha"
             }
             map[normalizedKey] = (map[normalizedKey] ?: 0) + it.total
        }
        
        // Aggregate for SEMUA (Total)
        if (type == "teacher" || type == "student") {
             // Re-calculate SEMUA whenever fetch happens. 
             // Note: ideally we wait for both, but for responsiveness we can just sum available maps
             statsSemua.clear()
             val allKeys = statsGuru.keys + statsSiswa.keys
             allKeys.forEach { key ->
                 statsSemua[key] = (statsGuru[key] ?: 0) + (statsSiswa[key] ?: 0)
             }
        }
    }

    private fun checkAndUpdateCharts() {
        // Trigger update based on current mode
        when (currentMode) {
            "SEMUA" -> showSemuaStatistics()
            "GURU" -> showGuruStatistics()
            "SISWA" -> showSiswaStatistics()
        }
    }

    private fun updateChartsBasedOnTimeRange() {
        fetchStatistics() // Re-fetch on time range change
    }

    private fun showSemuaStatistics() {
        currentMode = "SEMUA"
        findViewById<android.widget.TextView>(R.id.textView11).text = "Semua"
        updateBarChartForSemua()
        updatePieChartForSemua()
        updateStatsCardsForSemua()
    }

    private fun showGuruStatistics() {
        currentMode = "GURU"
        findViewById<android.widget.TextView>(R.id.textView11).text = "Guru"
        updateBarChartForGuru()
        updatePieChartForGuru()
        updateStatsCardsForGuru()
    }

    private fun showSiswaStatistics() {
        currentMode = "SISWA"
        findViewById<android.widget.TextView>(R.id.textView11).text = "Siswa"
        updateBarChartForSiswa()
        updatePieChartForSiswa()
        updateStatsCardsForSiswa()
    }

    private fun updateStatsCardsForSemua() {
        updateCardsFromMap(statsSemua)
    }

    private fun updateStatsCardsForGuru() {
        updateCardsFromMap(statsGuru)
    }
    
    private fun updateStatsCardsForSiswa() {
        updateCardsFromMap(statsSiswa)
    }
    
    private fun updateCardsFromMap(data: Map<String, Int>) {
        val total = data.values.sum()
        val hadir = (data["hadir"] ?: 0) + (data["terlambat"] ?: 0) // Late is considered present usually? or separate?
        // Let's keep separate as per existing UI logic maybe?
        // Actually UI has Total %, Total Students/Teachers, Late, Alpha.
        
        // textView13 -> Percentage Present?
        // textView14 -> Total Count?
        // textView15 -> Late Count
        // textView16 -> Alpha Count
        
        val presentTotal = (data["hadir"] ?: 0) + (data["terlambat"] ?: 0)
        val percent = if (total > 0) (presentTotal.toFloat() / total * 100) else 0f
        
        findViewById<android.widget.TextView>(R.id.textView13).text = "${"%.1f".format(percent)}%"
        findViewById<android.widget.TextView>(R.id.textView14).text = total.toString()
        findViewById<android.widget.TextView>(R.id.textView15).text = (data["terlambat"] ?: 0).toString()
        findViewById<android.widget.TextView>(R.id.textView16).text = (data["alpha"] ?: 0).toString()
    }

    private fun getBarChartDataForMode(): List<Float> {
        val map = when (currentMode) {
             "SEMUA" -> statsSemua
             "GURU" -> statsGuru
             "SISWA" -> statsSiswa
             else -> statsSemua
        }
        
        // Order: "Hadir", "Izin", "Sakit", "Tidak Hadir", "Terlambat", "Pulang"
        return listOf(
            (map["hadir"] ?: 0).toFloat(),
            (map["izin"] ?: 0).toFloat(),
            (map["sakit"] ?: 0).toFloat(),
            (map["alpha"] ?: 0).toFloat(),
            (map["terlambat"] ?: 0).toFloat(),
            0f // Pulang not tracked effectively yet
        )
    }

    private fun getPieChartDataForMode(): List<Float> {
        return getBarChartDataForMode()
    }

    private fun updateBarChartForSemua() {
        updateBarChartWithData(getBarChartDataForMode(), "Semua")
    }

    private fun updateBarChartForGuru() {
        updateBarChartWithData(getBarChartDataForMode(), "Guru")
    }

    private fun updateBarChartForSiswa() {
        updateBarChartWithData(getBarChartDataForMode(), "Siswa")
    }

    private fun updateBarChartWithData(data: List<Float>, mode: String) {
        val entries = ArrayList<BarEntry>()

        data.forEachIndexed { index, value ->
            entries.add(BarEntry(index.toFloat(), value))
        }

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
        dataSet.valueFormatter = object : ValueFormatter() {
             override fun getFormattedValue(value: Float): String {
                 return value.toInt().toString()
             }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f
        barChart.data = barData

        val grafikTitle: android.widget.TextView = findViewById(R.id.textViewGrafikTitle)
        val timeText = when (currentTimeRange) {
            "HARIAN" -> "Hari Ini"
            "MINGGUAN" -> "Minggu Ini"
            "BULANAN" -> "Bulan Ini"
            else -> "Hari Ini"
        }
        grafikTitle.text = "Grafik $mode $timeText"

        barChart.invalidate()
    }
    

    private fun setupPieChart() {
         // Initial setup
        pieChart.description.isEnabled = false
        // ... (rest of filtering)
        fetchStatistics() // Fetch initial data
    }
    
    private fun updatePieChartForSemua() {
        updatePieChartWithData(getPieChartDataForMode(), "Semua")
    }

    private fun updatePieChartForGuru() {
        updatePieChartWithData(getPieChartDataForMode(), "Guru")
    }

    private fun updatePieChartForSiswa() {
        updatePieChartWithData(getPieChartDataForMode(), "Siswa")
    }

    private fun updatePieChartWithData(data: List<Float>, mode: String) {
        val entries = ArrayList<PieEntry>()
        val labels = arrayOf("Hadir", "Izin", "Sakit", "Tidak Hadir", "Terlambat", "Pulang")

        data.forEachIndexed { index, value ->
            if (value > 0) {
                 entries.add(PieEntry(value, labels[index]))
            }
        }

        val dataSet = PieDataSet(entries, "Status Kehadiran $mode")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#F44336"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#00BCD4")
        )

        dataSet.valueTextSize = 13f
        dataSet.valueTextColor = Color.BLACK
        dataSet.sliceSpace = 2f

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart))
        pieData.setValueTextSize(11f)
        pieData.setValueTextColor(Color.BLACK)

        pieChart.data = pieData

        val pieTitle: android.widget.TextView = findViewById(R.id.textViewPieTitle)
        val timeText = when (currentTimeRange) {
            "HARIAN" -> "Hari Ini"
            "MINGGUAN" -> "Minggu Ini"
            "BULANAN" -> "Bulan Ini"
            else -> "Hari Ini"
        }
        pieTitle.text = "Status Kehadiran $mode $timeText"

        pieChart.invalidate()
    }

    private fun setupExportButton() {
        val exportButton: android.widget.Button = findViewById(R.id.button3)

        exportButton.setOnClickListener {
            exportDataToCSV()
        }
    }

    private fun exportDataToCSV() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Statistik_${currentMode}_${currentTimeRange}_$timeStamp.csv"

            val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val csvFile = File(storageDir, fileName)

            val writer = FileWriter(csvFile)

            writer.append("Kategori,Persentase(%)\n")

            val labels = arrayOf("Hadir", "Izin", "Sakit", "Tidak Hadir", "Terlambat", "Pulang")
            val data = getBarChartDataForMode()

            labels.forEachIndexed { index, label ->
                writer.append("$label,${data[index]}\n")
            }

            writer.append("\n")
            writer.append("Mode: $currentMode\n")
            writer.append("Periode: $currentTimeRange\n")
            writer.append("Tanggal Ekspor: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\n")

            writer.flush()
            writer.close()

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    csvFile
                )
            } else {
                Uri.fromFile(csvFile)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Data Statistik Kehadiran")
                putExtra(Intent.EXTRA_TEXT, "Berikut adalah data statistik kehadiran yang diekspor")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Ekspor Data ke..."))

            Toast.makeText(this, "Data berhasil diekspor ke $fileName", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mengekspor data: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupNavigation() {
        println("DEBUG: Setting up navigation buttons...")

        try {
            val btnHome: ImageButton = findViewById(R.id.imageButton2)
            btnHome.setOnClickListener {
                println("DEBUG: Home button clicked")
                try {
                    val intent = Intent(this, DashboardWaka::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    println("ERROR: Failed to navigate to DashboardWaka: ${e.message}")
                    Toast.makeText(this, "Gagal membuka Dashboard", Toast.LENGTH_SHORT).show()
                }
            }
            println("DEBUG: Home button setup successful")
        } catch (e: Exception) {
            println("ERROR: Home button not found or setup failed: ${e.message}")
        }

        try {
            val btnContacts: ImageButton = findViewById(R.id.imageButton3)
            btnContacts.setOnClickListener {
                println("DEBUG: Contacts button clicked")
                try {
                    val intent = Intent(this, DataRekapKehadiranGuru::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    println("ERROR: Failed to navigate to DataRekapKehadiranGuru: ${e.message}")
                    Toast.makeText(this, "Gagal membuka Data Rekap", Toast.LENGTH_SHORT).show()
                }
            }
            println("DEBUG: Contacts button setup successful")
        } catch (e: Exception) {
            println("ERROR: Contacts button not found or setup failed: ${e.message}")
        }

        try {
            val btnAssignment: ImageButton = findViewById(R.id.imageButton4)
            btnAssignment.setOnClickListener {
                println("DEBUG: Assignment button clicked, current mode: $currentMode")
                try {
                    val intent = Intent(this, JadwalPembelajaranGuru::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    println("ERROR: Failed to navigate from Assignment button: ${e.message}")
                    Toast.makeText(this, "Gagal membuka halaman rekap", Toast.LENGTH_SHORT).show()
                }
            }
            println("DEBUG: Assignment button setup successful")
        } catch (e: Exception) {
            println("ERROR: Assignment button not found or setup failed: ${e.message}")
        }

        try {
            val btnStats: ImageButton = findViewById(R.id.imageButton50)
            btnStats.setOnClickListener {
                println("DEBUG: Bar Chart button clicked (already on this page)")
                Toast.makeText(this, "Anda sudah berada di halaman Statistik", Toast.LENGTH_SHORT).show()
            }
            println("DEBUG: Bar Chart button setup successful")
        } catch (e: Exception) {
            println("ERROR: Bar Chart button not found or setup failed: ${e.message}")
        }

        try {
            val btnNotifications: ImageButton = findViewById(R.id.imageButton6)
            btnNotifications.setOnClickListener {
                println("DEBUG: Notifications button clicked")
                try {
                    val intent = Intent(this, NotifikasiSemuaWaka::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Fitur Notifikasi akan segera hadir", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    println("ERROR: Failed to navigate to Notifications: ${e.message}")
                    Toast.makeText(this, "Halaman notifikasi belum tersedia", Toast.LENGTH_SHORT).show()
                }
            }
            println("DEBUG: Notifications button setup successful")
        } catch (e: Exception) {
            println("ERROR: Notifications button not found or setup failed: ${e.message}")
        }

        println("DEBUG: All navigation buttons setup completed")
    }

    class PercentageFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if (value > 0) "${value.toInt()}%" else ""
        }
    }

    private fun setupPieChart() {
        showSemuaStatistics()

        pieChart.description.isEnabled = false
        pieChart.isRotationEnabled = true
        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(Color.WHITE)
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.setDrawCenterText(false)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(10f)

        val legend = pieChart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.textSize = 10f
        legend.textColor = Color.BLACK
        legend.form = Legend.LegendForm.CIRCLE
        legend.formSize = 9f
        legend.xEntrySpace = 5f
        legend.yEntrySpace = 3f

        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun setupBarChart() {
        showSemuaStatistics()

        val xAxis = barChart.xAxis
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

        val leftAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f
        leftAxis.granularity = 20f
        leftAxis.textSize = 12f
        leftAxis.textColor = Color.BLACK
        leftAxis.valueFormatter = PercentageFormatter()
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#E0E0E0")

        barChart.axisRight.isEnabled = false

        val legend = barChart.legend
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

        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)
        barChart.setScaleEnabled(false)
        barChart.setPinchZoom(false)
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.extraBottomOffset = 15f
        barChart.extraTopOffset = 15f

        barChart.invalidate()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, DashboardWaka::class.java)
        startActivity(intent)
        finish()
    }
}