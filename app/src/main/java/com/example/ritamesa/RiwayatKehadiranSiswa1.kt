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

class RiwayatKehadiranSiswa1 : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var calendarButton: ImageButton
    private lateinit var pageControlButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AttendanceAdapter1

    private var currentStatusFilter = "Semua"
    private var currentDateFilter = Calendar.getInstance()

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
            
            loadDataFromApi()
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
        dateTextView.text = dateFormat.format(currentDateFilter.time)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AttendanceAdapter1(emptyList())
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
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Menu status tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDataFromApi() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(currentDateFilter.time)
        val apiStatus = if (currentStatusFilter == "Semua") null else currentStatusFilter

        apiService.getSchoolAttendanceHistory(date = dateStr, status = apiStatus, role = "Siswa")
            .enqueue(object : Callback<SchoolAttendanceResponse> {
                override fun onResponse(call: Call<SchoolAttendanceResponse>, response: Response<SchoolAttendanceResponse>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()?.data ?: emptyList()
                        val items = responseData.map { apiItem ->
                            Attendance1(
                                date = apiItem.date,
                                name = apiItem.student?.user?.name ?: "Siswa",
                                classGrade = apiItem.schedule?.classInfo?.name ?: "-",
                                status = when(apiItem.status) {
                                    "present" -> "Tepat Waktu"
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
                        updateStatistics(items)
                    } else {
                        Toast.makeText(this@RiwayatKehadiranSiswa1, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SchoolAttendanceResponse>, t: Throwable) {
                    Toast.makeText(this@RiwayatKehadiranSiswa1, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateStatistics(data: List<Attendance1>) {
        try {
            val totalSiswa = data.size
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
        pageControlButton.setOnClickListener { showExportImportMenu() }
    }

    private fun showExportImportMenu() {
        val popupMenu = PopupMenu(this, pageControlButton)

        try {
            popupMenu.menuInflater.inflate(R.menu.menu_ekspor_impor, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_ekspor -> { handleEkspor(); true }
                    R.id.menu_impor -> { handleImpor(); true }
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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(currentDateFilter.time)
        
        // Use the absolute URL via getClient instance or hardcode the export path
        val baseUrl = com.example.ritamesa.data.api.ApiClient.BASE_URL
        val exportUrl = "${baseUrl}attendance/export-pdf?from=$dateStr&to=$dateStr"
        
        Toast.makeText(this, "Mengekspor data ke PDF...", Toast.LENGTH_SHORT).show()
        
        val request = android.app.DownloadManager.Request(android.net.Uri.parse(exportUrl))
        request.setTitle("Laporan Kehadiran $dateStr")
        request.setDescription("Sedang mengunduh laporan...")
        request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, "Laporan_Kehadiran_$dateStr.pdf")
        
        // Add auth token if available in SharedPreferences
        val prefs = getSharedPreferences("ritamesa_prefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        if (token != null) {
            request.addRequestHeader("Authorization", "Bearer $token")
        }
        
        val downloadManager = getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        downloadManager.enqueue(request)
    }

    private fun handleImpor() {
        Toast.makeText(this, "Fitur Impor (Excel) dilakukan melalui Dashboard Web Admin", Toast.LENGTH_LONG).show()
    }

    inner class AttendanceAdapter1(private var list: List<Attendance1>) : RecyclerView.Adapter<AttendanceAdapter1.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textNama: TextView = view.findViewById(R.id.textView34)
            val textKelas: TextView = view.findViewById(R.id.textView197)
            val textStatus: TextView = view.findViewById(R.id.textView202)
            val textKeterangan: TextView = view.findViewById(R.id.textView201)
        }
        fun updateData(newList: List<Attendance1>) {
            list = newList
            notifyDataSetChanged()
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_siswa, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.textNama.text = item.name
            holder.textKelas.text = item.classGrade
            holder.textStatus.text = item.status
            holder.textKeterangan.text = item.keterangan
        }
        override fun getItemCount() = list.size
    }
}

data class Attendance1(
    val date: String,
    val name: String,
    val classGrade: String,
    val status: String,
    val keterangan: String
)