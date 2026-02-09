package com.example.ritamesa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.SchoolAttendanceResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranGuru1 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RiwayatKehadiranGuruAdapter1
    private lateinit var backButton: ImageButton
    private lateinit var filterButton: ImageButton
    private lateinit var calendarButton: ImageButton
    private lateinit var pageControlButton: ImageButton
    private lateinit var statusTextView: TextView
    private lateinit var dateTextView: TextView

    private var currentStatusFilter = "Semua"
    private var currentDateFilter = Calendar.getInstance()

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
        setupBackButton()
        setupFilterButton()
        setupCategoryButton()
        setupCalendarButton()
        setupPageControlButton()
        
        loadDataFromApi()
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
        dateTextView.text = dateFormat.format(currentDateFilter.time)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RiwayatKehadiranGuruAdapter1(emptyList())
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
        val year = currentDateFilter.get(Calendar.YEAR)
        val month = currentDateFilter.get(Calendar.MONTH)
        val day = currentDateFilter.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                currentDateFilter.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))
                val formattedDate = dateFormat.format(currentDateFilter.time)
                dateTextView.text = formattedDate
                loadDataFromApi()
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
                R.id.menu_hadir -> { statusTextView.text = "Hadir"; currentStatusFilter = "present" }
                R.id.menu_izin -> { statusTextView.text = "Izin"; currentStatusFilter = "excused" }
                R.id.menu_sakit -> { statusTextView.text = "Sakit"; currentStatusFilter = "sick" }
                R.id.menu_alpha -> { statusTextView.text = "Alpha"; currentStatusFilter = "absent" }
                R.id.menu_terlambat -> { statusTextView.text = "Terlambat"; currentStatusFilter = "late" }
                R.id.menu_semua -> { statusTextView.text = "Semua"; currentStatusFilter = "Semua" }
                else -> false
            }
            loadDataFromApi()
            true
        }
        popupMenu.show()
    }

    private fun loadDataFromApi() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(currentDateFilter.time)
        val apiStatus = if (currentStatusFilter == "Semua") null else currentStatusFilter

        apiService.getSchoolAttendanceHistory(date = dateStr, status = apiStatus, role = "Guru")
            .enqueue(object : Callback<SchoolAttendanceResponse> {
                override fun onResponse(call: Call<SchoolAttendanceResponse>, response: Response<SchoolAttendanceResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val items = response.body()!!.data.map { apiItem ->
                            RiwayatKehadiranGuruWaka(
                                id = apiItem.id,
                                nama = apiItem.teacher?.user?.name ?: "Guru",
                                role = "Guru", // Bisa diisi mapel jika ada di model
                                tanggal = apiItem.date,
                                waktu = apiItem.checkedInTime?.substringAfter(" ")?.substringBeforeLast(":") ?: "-",
                                status = when(apiItem.status) {
                                    "present" -> "Hadir"
                                    "late" -> "Terlambat"
                                    "sick" -> "Sakit"
                                    "excused" -> "Izin"
                                    "absent" -> "Alpha"
                                    else -> apiItem.status
                                },
                                keterangan = if(apiItem.status == "late") "Terlambat" else "Hadir"
                            )
                        }
                        adapter.updateData(items)
                    } else {
                        Toast.makeText(this@RiwayatKehadiranGuru1, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SchoolAttendanceResponse>, t: Throwable) {
                    Toast.makeText(this@RiwayatKehadiranGuru1, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupPageControlButton() {
        pageControlButton.setOnClickListener { showExportImportMenu() }
    }

    private fun showExportImportMenu() {
        val popupMenu = PopupMenu(this, pageControlButton)
        popupMenu.menuInflater.inflate(R.menu.menu_ekspor_impor, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_ekspor -> { Toast.makeText(this, "Ekspor data...", Toast.LENGTH_SHORT).show(); true }
                R.id.menu_impor -> { Toast.makeText(this, "Impor data...", Toast.LENGTH_SHORT).show(); true }
                else -> false
            }
        }
        popupMenu.show()
    }

    // Adaptor inner class updated with updateData
    inner class RiwayatKehadiranGuruAdapter1(
        private var list: List<RiwayatKehadiranGuruWaka>
    ) : RecyclerView.Adapter<RiwayatKehadiranGuruAdapter1.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textNama: TextView = view.findViewById(R.id.textView63)
            val textRole: TextView = view.findViewById(R.id.textView64)
            val textWaktu: TextView = view.findViewById(R.id.textView66)
            val textStatus: TextView = view.findViewById(R.id.textView65)
        }

        fun updateData(newList: List<RiwayatKehadiranGuruWaka>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_guru_waka, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.textNama.text = item.nama
            holder.textRole.text = item.role
            holder.textWaktu.text = item.waktu
            holder.textStatus.text = item.status
            
            // Set color based on status if needed (optional since layout might handle it)
        }

        override fun getItemCount() = list.size
    }
}