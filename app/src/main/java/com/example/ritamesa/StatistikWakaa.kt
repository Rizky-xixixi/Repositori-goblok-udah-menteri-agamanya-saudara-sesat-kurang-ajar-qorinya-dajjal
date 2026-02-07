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

    private fun updateChartsBasedOnTimeRange() {
        when (currentMode) {
            "SEMUA" -> showSemuaStatistics()
            "GURU" -> showGuruStatistics()
            "SISWA" -> showSiswaStatistics()
        }
    }

    private fun showSemuaStatistics() {
        currentMode = "SEMUA"

        val textView11: android.widget.TextView = findViewById(R.id.textView11)
        textView11.text = "Semua"

        updateBarChartForSemua()
        updatePieChartForSemua()
        updateStatsCardsForSemua()

        Toast.makeText(this, "Menampilkan statistik Semua", Toast.LENGTH_SHORT).show()
    }

    private fun showGuruStatistics() {
        currentMode = "GURU"

        val textView11: android.widget.TextView = findViewById(R.id.textView11)
        textView11.text = "Guru"

        updateBarChartForGuru()
        updatePieChartForGuru()
        updateStatsCardsForGuru()

        Toast.makeText(this, "Menampilkan statistik Guru", Toast.LENGTH_SHORT).show()
    }

    private fun showSiswaStatistics() {
        currentMode = "SISWA"

        val textView11: android.widget.TextView = findViewById(R.id.textView11)
        textView11.text = "Siswa"

        updateBarChartForSiswa()
        updatePieChartForSiswa()
        updateStatsCardsForSiswa()

        Toast.makeText(this, "Menampilkan statistik Siswa", Toast.LENGTH_SHORT).show()
    }

    private fun updateStatsCardsForSemua() {
        val textView13: android.widget.TextView = findViewById(R.id.textView13)
        val textView14: android.widget.TextView = findViewById(R.id.textView14)
        val textView15: android.widget.TextView = findViewById(R.id.textView15)
        val textView16: android.widget.TextView = findViewById(R.id.textView16)

        when (currentTimeRange) {
            "HARIAN" -> {
                textView13.text = "85%"
                textView14.text = "120"
                textView15.text = "25"
                textView16.text = "8"
            }
            "MINGGUAN" -> {
                textView13.text = "82%"
                textView14.text = "580"
                textView15.text = "95"
                textView16.text = "35"
            }
            "BULANAN" -> {
                textView13.text = "88%"
                textView14.text = "2200"
                textView15.text = "210"
                textView16.text = "80"
            }
        }
    }

    private fun updateStatsCardsForGuru() {
        val textView13: android.widget.TextView = findViewById(R.id.textView13)
        val textView14: android.widget.TextView = findViewById(R.id.textView14)
        val textView15: android.widget.TextView = findViewById(R.id.textView15)
        val textView16: android.widget.TextView = findViewById(R.id.textView16)

        when (currentTimeRange) {
            "HARIAN" -> {
                textView13.text = "92%"
                textView14.text = "45"
                textView15.text = "3"
                textView16.text = "1"
            }
            "MINGGUAN" -> {
                textView13.text = "90%"
                textView14.text = "225"
                textView15.text = "18"
                textView16.text = "7"
            }
            "BULANAN" -> {
                textView13.text = "94%"
                textView14.text = "900"
                textView15.text = "45"
                textView16.text = "15"
            }
        }
    }

    private fun updateStatsCardsForSiswa() {
        val textView13: android.widget.TextView = findViewById(R.id.textView13)
        val textView14: android.widget.TextView = findViewById(R.id.textView14)
        val textView15: android.widget.TextView = findViewById(R.id.textView15)
        val textView16: android.widget.TextView = findViewById(R.id.textView16)

        when (currentTimeRange) {
            "HARIAN" -> {
                textView13.text = "78%"
                textView14.text = "75"
                textView15.text = "22"
                textView16.text = "7"
            }
            "MINGGUAN" -> {
                textView13.text = "80%"
                textView14.text = "355"
                textView15.text = "77"
                textView16.text = "28"
            }
            "BULANAN" -> {
                textView13.text = "85%"
                textView14.text = "1300"
                textView15.text = "165"
                textView16.text = "65"
            }
        }
    }

    private fun getBarChartDataForMode(): List<Float> {
        return when (currentMode) {
            "SEMUA" -> {
                when (currentTimeRange) {
                    "HARIAN" -> listOf(85f, 8f, 5f, 2f, 3f, 92f)
                    "MINGGUAN" -> listOf(82f, 10f, 6f, 2f, 4f, 88f)
                    "BULANAN" -> listOf(88f, 7f, 3f, 2f, 2f, 94f)
                    else -> listOf(85f, 8f, 5f, 2f, 3f, 92f)
                }
            }
            "GURU" -> {
                when (currentTimeRange) {
                    "HARIAN" -> listOf(92f, 5f, 2f, 1f, 2f, 95f)
                    "MINGGUAN" -> listOf(90f, 6f, 3f, 1f, 3f, 92f)
                    "BULANAN" -> listOf(94f, 4f, 1f, 1f, 2f, 96f)
                    else -> listOf(92f, 5f, 2f, 1f, 2f, 95f)
                }
            }
            "SISWA" -> {
                when (currentTimeRange) {
                    "HARIAN" -> listOf(78f, 10f, 7f, 5f, 8f, 82f)
                    "MINGGUAN" -> listOf(80f, 11f, 6f, 3f, 7f, 85f)
                    "BULANAN" -> listOf(85f, 9f, 4f, 2f, 6f, 90f)
                    else -> listOf(78f, 10f, 7f, 5f, 8f, 82f)
                }
            }
            else -> listOf(85f, 8f, 5f, 2f, 3f, 92f)
        }
    }

    private fun getPieChartDataForMode(): List<Float> {
        return when (currentMode) {
            "SEMUA" -> {
                when (currentTimeRange) {
                    "HARIAN" -> listOf(85f, 8f, 5f, 2f, 3f, 92f)
                    "MINGGUAN" -> listOf(82f, 10f, 6f, 2f, 4f, 88f)
                    "BULANAN" -> listOf(88f, 7f, 3f, 2f, 2f, 94f)
                    else -> listOf(85f, 8f, 5f, 2f, 3f, 92f)
                }
            }
            "GURU" -> {
                when (currentTimeRange) {
                    "HARIAN" -> listOf(92f, 5f, 2f, 1f, 2f, 95f)
                    "MINGGUAN" -> listOf(90f, 6f, 3f, 1f, 3f, 92f)
                    "BULANAN" -> listOf(94f, 4f, 1f, 1f, 2f, 96f)
                    else -> listOf(92f, 5f, 2f, 1f, 2f, 95f)
                }
            }
            "SISWA" -> {
                when (currentTimeRange) {
                    "HARIAN" -> listOf(78f, 10f, 7f, 5f, 8f, 82f)
                    "MINGGUAN" -> listOf(80f, 11f, 6f, 3f, 7f, 85f)
                    "BULANAN" -> listOf(85f, 9f, 4f, 2f, 6f, 90f)
                    else -> listOf(78f, 10f, 7f, 5f, 8f, 82f)
                }
            }
            else -> listOf(85f, 8f, 5f, 2f, 3f, 92f)
        }
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
        dataSet.valueFormatter = PercentageFormatter()

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
            entries.add(PieEntry(value, labels[index]))
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