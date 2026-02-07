package com.example.ritamesa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranSiswa1 : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var calendarButton: ImageButton
    private lateinit var pageControlButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AttendanceAdapter

    private val allData = listOf(
        Attendance(
            date = "Senin, 7 Januari 2026",
            name = "Andi Wijaya",
            classGrade = "12 RPL 2",
            status = "Tepat Waktu",
            keterangan = "Hadir"
        ),
        Attendance(
            date = "Senin, 7 Januari 2026",
            name = "Budi Santoso",
            classGrade = "12 RPL 2",
            status = "Terlambat",
            keterangan = "Macet di jalan"
        ),
        Attendance(
            date = "Senin, 7 Januari 2026",
            name = "Cici Amelia",
            classGrade = "11 TKJ 1",
            status = "Izin",
            keterangan = "Dispensasi Lomba"
        ),
        Attendance(
            date = "Senin, 7 Januari 2026",
            name = "Dedi Pratama",
            classGrade = "10 MM 3",
            status = "Sakit",
            keterangan = "Demam"
        ),
        Attendance(
            date = "Senin, 7 Januari 2026",
            name = "Eka Putri",
            classGrade = "12 AKL 1",
            status = "Alpha",
            keterangan = "-"
        ),
        Attendance(
            date = "Selasa, 8 Januari 2026",
            name = "Fajar Nugroho",
            classGrade = "12 RPL 2",
            status = "Tepat Waktu",
            keterangan = "Hadir"
        ),
        Attendance(
            date = "Selasa, 8 Januari 2026",
            name = "Gita Wulandari",
            classGrade = "11 TKJ 1",
            status = "Terlambat",
            keterangan = "Bangun kesiangan"
        ),
        Attendance(
            date = "Rabu, 9 Januari 2026",
            name = "Hadi Susanto",
            classGrade = "10 MM 3",
            status = "Izin",
            keterangan = "Keperluan keluarga"
        ),
        Attendance(
            date = "Kamis, 10 Januari 2026",
            name = "Indra Maulana",
            classGrade = "12 AKL 1",
            status = "Sakit",
            keterangan = "Flu"
        ),
        Attendance(
            date = "Jumat, 11 Januari 2026",
            name = "Joko Widodo",
            classGrade = "12 RPL 2",
            status = "Alpha",
            keterangan = "-"
        )
    )

    private var currentStatusFilter = "Semua"
    private var currentDateFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            setContentView(R.layout.riwayat_kehadiran_siswa1)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            setupViews()
            setupRecyclerView()
            setupBackButton()
            setupFilterButton()
            setupCategoryButton()
            setupCalendarButton()
            setupPageControlButton()
            loadData(currentStatusFilter, currentDateFilter)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupViews() {
        statusTextView = findViewById(R.id.textView53)
        dateTextView = findViewById(R.id.textView56)
        calendarButton = findViewById(R.id.imageButton51)
        pageControlButton = findViewById(R.id.imageButton52)
        recyclerView = findViewById(R.id.recyclerView)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))
        val today = dateFormat.format(Calendar.getInstance().time)
        dateTextView.text = today
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AttendanceAdapter(emptyList())
        recyclerView.adapter = adapter
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.imageButton48)
        backButton.setOnClickListener {
            val intent = Intent(this, DashboardWaka::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupFilterButton() {
        val filterButton = findViewById<ImageButton>(R.id.imageButton200)
        filterButton.setOnClickListener {
            showStatusDropdownMenu()
        }
    }

    private fun setupCategoryButton() {
        val categoryButton = findViewById<ImageButton>(R.id.imageButton2)
        categoryButton.setOnClickListener {
            showCategoryDropdownMenu()
        }
    }

    private fun setupCalendarButton() {
        calendarButton.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))
                val formattedDate = dateFormat.format(selectedCalendar.time)

                dateTextView.text = formattedDate

                currentDateFilter = formattedDate
                loadData(currentStatusFilter, currentDateFilter)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun showCategoryDropdownMenu() {
        val categoryButton = findViewById<ImageButton>(R.id.imageButton2)
        val popupMenu = PopupMenu(this, categoryButton)

        try {
            popupMenu.menuInflater.inflate(R.menu.menu_data_rekap, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_guru -> {
                        val intent = Intent(this, RiwayatKehadiranGuru1::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }
                    R.id.menu_siswa -> true
                    else -> false
                }
            }

            popupMenu.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Menu tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStatusDropdownMenu() {
        val filterButton = findViewById<ImageButton>(R.id.imageButton200)
        val popupMenu = PopupMenu(this, filterButton)

        try {
            popupMenu.menuInflater.inflate(R.menu.dropdown_status_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_hadir -> {
                        statusTextView.text = "Hadir"
                        currentStatusFilter = "Hadir"
                        loadData(currentStatusFilter, currentDateFilter)
                        true
                    }
                    R.id.menu_izin -> {
                        statusTextView.text = "Izin"
                        currentStatusFilter = "Izin"
                        loadData(currentStatusFilter, currentDateFilter)
                        true
                    }
                    R.id.menu_sakit -> {
                        statusTextView.text = "Sakit"
                        currentStatusFilter = "Sakit"
                        loadData(currentStatusFilter, currentDateFilter)
                        true
                    }
                    R.id.menu_alpha -> {
                        statusTextView.text = "Alpha"
                        currentStatusFilter = "Alpha"
                        loadData(currentStatusFilter, currentDateFilter)
                        true
                    }
                    R.id.menu_terlambat -> {
                        statusTextView.text = "Terlambat"
                        currentStatusFilter = "Terlambat"
                        loadData(currentStatusFilter, currentDateFilter)
                        true
                    }
                    R.id.menu_semua -> {
                        statusTextView.text = "Semua"
                        currentStatusFilter = "Semua"
                        loadData(currentStatusFilter, currentDateFilter)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Menu status tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadData(status: String, selectedDate: String?) {
        try {
            var filteredList = allData

            filteredList = when (status) {
                "Semua" -> filteredList
                "Hadir" -> filteredList.filter { it.status == "Tepat Waktu" }
                "Terlambat" -> filteredList.filter { it.status == "Terlambat" }
                "Izin" -> filteredList.filter { it.status == "Izin" }
                "Sakit" -> filteredList.filter { it.status == "Sakit" }
                "Alpha" -> filteredList.filter { it.status == "Alpha" }
                else -> filteredList
            }

            if (!selectedDate.isNullOrEmpty()) {
                filteredList = filteredList.filter { data ->
                    try {
                        val dataDate = convertToDateOnly(data.date)
                        val selectedDateFormatted = convertToDateOnly(selectedDate)
                        dataDate == selectedDateFormatted
                    } catch (e: Exception) {
                        false
                    }
                }
            }

            adapter = AttendanceAdapter(filteredList)
            recyclerView.adapter = adapter

            updateStatistics(filteredList)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertToDateOnly(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: ParseException) {
            try {
                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                dateString
            } catch (e2: ParseException) {
                dateString
            }
        }
    }

    private fun updateStatistics(data: List<Attendance>) {
        try {
            val totalSiswa = allData.size
            val hadirCount = data.count { it.status == "Tepat Waktu" }
            val terlambatCount = data.count { it.status == "Terlambat" }
            val izinCount = data.count { it.status == "Izin" }
            val sakitCount = data.count { it.status == "Sakit" }
            val alphaCount = data.count { it.status == "Alpha" }

            findViewById<TextView>(R.id.textView62).text = totalSiswa.toString()
            findViewById<TextView>(R.id.textView68).text = hadirCount.toString()
            findViewById<TextView>(R.id.textView69).text = terlambatCount.toString()
            findViewById<TextView>(R.id.textView72).text = izinCount.toString()
            findViewById<TextView>(R.id.textView71).text = sakitCount.toString()
            findViewById<TextView>(R.id.textView70).text = alphaCount.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupPageControlButton() {
        pageControlButton.setOnClickListener { view ->
            showExportImportMenu()
        }
    }

    private fun showExportImportMenu() {
        val popupMenu = PopupMenu(this, pageControlButton)

        try {
            popupMenu.menuInflater.inflate(R.menu.menu_ekspor_impor, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_ekspor -> {
                        handleEkspor()
                        true
                    }
                    R.id.menu_impor -> {
                        handleImpor()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Menu ekspor/impor tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleEkspor() {
        Toast.makeText(this, "Ekspor data riwayat kehadiran siswa...", Toast.LENGTH_SHORT).show()
    }

    private fun handleImpor() {
        Toast.makeText(this, "Impor data riwayat kehadiran siswa...", Toast.LENGTH_SHORT).show()
    }

    private fun resetDateFilter() {
        currentDateFilter = null
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))
        val today = dateFormat.format(Calendar.getInstance().time)
        dateTextView.text = today
        loadData(currentStatusFilter, currentDateFilter)
    }
}