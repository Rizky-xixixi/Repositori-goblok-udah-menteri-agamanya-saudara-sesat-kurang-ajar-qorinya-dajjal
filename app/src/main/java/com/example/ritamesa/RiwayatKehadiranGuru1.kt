package com.example.ritamesa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranGuru1 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RiwayatKehadiranGuruAdapter
    private lateinit var backButton: ImageButton
    private lateinit var filterButton: ImageButton
    private lateinit var calendarButton: ImageButton
    private lateinit var pageControlButton: ImageButton
    private lateinit var statusTextView: TextView
    private lateinit var dateTextView: TextView

    private val allData = listOf(
        RiwayatKehadiranGuruWaka(
            id = 1,
            nama = "Budi Santoso",
            role = "Guru Matematika",
            tanggal = "Senin, 7 Januari 2026",
            waktu = "07.00",
            status = "Hadir",
            keterangan = "Tepat waktu"
        ),
        RiwayatKehadiranGuruWaka(
            id = 2,
            nama = "Siti Aminah",
            role = "Guru Bahasa Indonesia",
            tanggal = "Selasa, 8 Januari 2026",
            waktu = "07.15",
            status = "Terlambat",
            keterangan = "Macet di jalan"
        ),
        RiwayatKehadiranGuruWaka(
            id = 3,
            nama = "Ahmad Rizki",
            role = "Guru IPA",
            tanggal = "Rabu, 9 Januari 2026",
            waktu = "07.00",
            status = "Hadir",
            keterangan = "Tepat waktu"
        ),
        RiwayatKehadiranGuruWaka(
            id = 4,
            nama = "Dewi Lestari",
            role = "Guru Bahasa Inggris",
            tanggal = "Kamis, 10 Januari 2026",
            waktu = "-",
            status = "Izin",
            keterangan = "Keperluan keluarga"
        ),
        RiwayatKehadiranGuruWaka(
            id = 5,
            nama = "Rudi Hartono",
            role = "Guru Olahraga",
            tanggal = "Jumat, 11 Januari 2026",
            waktu = "-",
            status = "Sakit",
            keterangan = "Demam tinggi"
        ),
        RiwayatKehadiranGuruWaka(
            id = 6,
            nama = "Maya Indah",
            role = "Guru Seni",
            tanggal = "Sabtu, 12 Januari 2026",
            waktu = "-",
            status = "Alpha",
            keterangan = "Tidak ada keterangan"
        )
    )

    private var currentStatusFilter = "Semua"
    private var currentDateFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.riwayat_kehadiran_guru1)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        setupRecyclerView()
        loadData(currentStatusFilter, currentDateFilter)
        setupBackButton()
        setupFilterButton()
        setupCategoryButton()
        setupCalendarButton()
        setupPageControlButton()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewRiwayat)
        backButton = findViewById(R.id.imageButton48)
        filterButton = findViewById(R.id.imageButton200)
        calendarButton = findViewById(R.id.imageButton51)
        pageControlButton = findViewById(R.id.imageButton52)
        statusTextView = findViewById(R.id.textView53)
        dateTextView = findViewById(R.id.textView56)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))
        val today = dateFormat.format(Calendar.getInstance().time)
        dateTextView.text = today
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RiwayatKehadiranGuruAdapter(emptyList())
        recyclerView.adapter = adapter
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            val intent = Intent(this, DashboardWaka::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupFilterButton() {
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
        popupMenu.menuInflater.inflate(R.menu.menu_data_rekap, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_guru -> true
                R.id.menu_siswa -> {
                    val intent = Intent(this, RiwayatKehadiranSiswa1::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showStatusDropdownMenu() {
        val popupMenu = PopupMenu(this, filterButton)
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
    }

    private fun loadData(status: String, selectedDate: String?) {
        var filteredData = allData

        if (status != "Semua") {
            filteredData = filteredData.filter { it.status == status }
        }

        if (!selectedDate.isNullOrEmpty()) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val selectedCalendar = Calendar.getInstance()

            try {
                val parsedDate = dateFormat.parse(selectedDate)
                selectedCalendar.time = parsedDate
            } catch (e: ParseException) {
                selectedCalendar.time = Date()
            }

            filteredData = filteredData.filter { data ->
                val dataDateString = convertToDateOnly(data.tanggal)
                val dataDate = try {
                    dateFormat.parse(dataDateString)
                } catch (e: ParseException) {
                    null
                }

                dataDate != null && dateFormat.format(dataDate) == selectedDate
            }
        }

        adapter = RiwayatKehadiranGuruAdapter(filteredData)
        recyclerView.adapter = adapter

        recyclerView.layoutManager?.scrollToPosition(0)
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
                val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

                val date = inputFormat.parse(dateString)
                outputFormat.format(date)
            } catch (e2: ParseException) {
                dateString
            }
        }
    }

    private fun setupPageControlButton() {
        pageControlButton.setOnClickListener { view ->
            showExportImportMenu()
        }
    }

    private fun showExportImportMenu() {
        val popupMenu = PopupMenu(this, pageControlButton)
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
    }

    private fun handleEkspor() {
        android.widget.Toast.makeText(this, "Ekspor data riwayat kehadiran guru...", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun handleImpor() {
        android.widget.Toast.makeText(this, "Impor data riwayat kehadiran guru...", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun resetDateFilter() {
        currentDateFilter = null
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))
        val today = dateFormat.format(Calendar.getInstance().time)
        dateTextView.text = today
        loadData(currentStatusFilter, currentDateFilter)
    }
}