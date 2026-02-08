package com.example.ritamesa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.HomeroomAttendanceItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranKelasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtJumlahSiswa: TextView
    private lateinit var txtHadirCount: TextView
    private lateinit var txtSakitCount: TextView
    private lateinit var txtIzinCount: TextView
    private lateinit var txtAlphaCount: TextView
    private lateinit var txtFilterTanggal: TextView

    private lateinit var btnHadir: ImageButton
    private lateinit var btnSakit: ImageButton
    private lateinit var btnIzin: ImageButton
    private lateinit var btnAlpha: ImageButton
    private lateinit var iconCalendar: ImageView

    private lateinit var btnHome: ImageButton
    private lateinit var btnChart: ImageButton
    private lateinit var btnNotif: ImageButton

    // Data lists
    private val allData = Collections.synchronizedList(mutableListOf<Map<String, Any>>())
    private val filteredData = Collections.synchronizedList(mutableListOf<Map<String, Any>>())
    private lateinit var adapter: SimpleSiswaAdapter
    private var filterActive: String? = null

    private var totalStudents = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isLoading = false
    private var currentDate: Calendar = Calendar.getInstance()

    private val textColorActive = android.graphics.Color.WHITE
    private val textColorNormal = android.graphics.Color.parseColor("#4B5563")
    private val textColorDefault = android.graphics.Color.BLACK

    companion object {
        private const val TAG = "RiwayatKelasActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in ${thread.name}: ${throwable.message}")
            runOnUiThread {
                Toast.makeText(this, "Error aplikasi, restart...", Toast.LENGTH_SHORT).show()
            }
            handler.postDelayed({
                finish()
                startActivity(intent)
            }, 1500)
        }

        try {
            setContentView(R.layout.riwayat_kehadiran_kelas)

            if (!initializeViews()) {
                Toast.makeText(this, "Gagal memuat tampilan", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            setupRecyclerView()
            setupFooterNavigation()
            setupFilterButtons()
            setupCalendarButton()

            updateTanggalDisplay()
            resetTextColors()

            loadDataFromApi()

        } catch (e: Exception) {
            Log.e(TAG, "FATAL ERROR in onCreate: ${e.message}", e)
            showErrorAndExit("Gagal memuat halaman kelas")
        }
    }

    private fun initializeViews(): Boolean {
        return try {
            recyclerView = findViewById(R.id.recycler_riwayat)!!
            txtJumlahSiswa = findViewById(R.id.text_jumlah_siswa)!!
            txtHadirCount = findViewById(R.id.txt_hadir_count)!!
            txtSakitCount = findViewById(R.id.txt_sakit_count)!!
            txtIzinCount = findViewById(R.id.txt_izin_count)!!
            txtAlphaCount = findViewById(R.id.txt_alpha_count)!!
            txtFilterTanggal = findViewById(R.id.text_filter_tanggal)!!

            btnHadir = findViewById(R.id.button_hadir)!!
            btnSakit = findViewById(R.id.button_sakit)!!
            btnIzin = findViewById(R.id.button_izin)!!
            btnAlpha = findViewById(R.id.button_alpha)!!

            iconCalendar = findViewById(R.id.icon_calendar)!!

            btnHome = findViewById(R.id.btnHome) ?: ImageButton(this)
            btnChart = findViewById(R.id.btnChart) ?: ImageButton(this)
            btnNotif = findViewById(R.id.btnNotif) ?: ImageButton(this)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializeViews: ${e.message}", e)
            false
        }
    }

    private fun setupCalendarButton() {
        iconCalendar.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            currentDate.set(selectedYear, selectedMonth, selectedDay)
            updateTanggalDisplay()
            loadDataFromApi()
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun updateTanggalDisplay() {
        try {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val formatted = sdf.format(currentDate.time)
            val finalDate = if (formatted.isNotEmpty()) {
                formatted[0].uppercaseChar() + formatted.substring(1)
            } else {
                formatted
            }
            txtFilterTanggal.text = finalDate
        } catch (e: Exception) {
            Log.e(TAG, "Error updateTanggalDisplay: ${e.message}")
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            txtFilterTanggal.text = sdf.format(Date())
        }
    }

    private fun setupRecyclerView() {
        adapter = SimpleSiswaAdapter(this, filteredData)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupFooterNavigation() {
        btnHome.setOnClickListener { safeNavigateTo(DashboardWaliKelasActivity::class.java, "Dashboard Wali Kelas") }
        
        val btnAssignment = findViewById<ImageButton>(R.id.btnAssigment)
        btnAssignment?.setOnClickListener {
            if (!isLoading) {
                filterActive = null
                resetFilter()
                updateTombolAktif()
                resetTextColors()
                Toast.makeText(this, "Filter direset", Toast.LENGTH_SHORT).show()
            }
        }

        btnChart.setOnClickListener { safeNavigateTo(TindakLanjutWaliKelasActivity::class.java, "Tindak Lanjut") }
        btnNotif.setOnClickListener { safeNavigateTo(NotifikasiWaliKelasActivity::class.java, "Notifikasi") }
    }

    private fun safeNavigateTo(activityClass: Class<*>, screenName: String) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    private fun loadDataFromApi() {
        if (isLoading) return
        isLoading = true
        Toast.makeText(this, "Memuat data...", Toast.LENGTH_SHORT).show()

        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateStr = dateFormat.format(currentDate.time)

        apiService.getHomeroomAttendance(from = dateStr, to = dateStr).enqueue(object : Callback<List<HomeroomAttendanceItem>> {
            override fun onResponse(call: Call<List<HomeroomAttendanceItem>>, response: Response<List<HomeroomAttendanceItem>>) {
                isLoading = false
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    processApiData(data)
                } else {
                    Toast.makeText(this@RiwayatKehadiranKelasActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<HomeroomAttendanceItem>>, t: Throwable) {
                isLoading = false
                Toast.makeText(this@RiwayatKehadiranKelasActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun processApiData(items: List<HomeroomAttendanceItem>) {
        allData.clear()
        
        items.forEach { item ->
            val statusType = when(item.status) {
                "present" -> "hadir"
                "sick" -> "sakit"
                "excused" -> "izin"
                "absent" -> "alpha"
                else -> "hadir" // Default or 'late' -> hadir
            }
            
            val keterangan = when (statusType) {
                "hadir" -> "Siswa Hadir di Kelas"
                "sakit" -> "Siswa Tidak Hadir (Sakit)"
                "izin" -> "Siswa Izin Tidak Hadir"
                "alpha" -> "Siswa Tidak Hadir Tanpa Keterangan"
                else -> "Status Tidak Diketahui"
            }

            val mapel = item.schedule?.subjectInfo?.name ?: "-"
            val guru = item.schedule?.teacher?.user?.name ?: "-"

            allData.add(mapOf(
                "id" to item.id,
                "nama" to (item.student?.user?.name ?: "Siswa ${item.id}"),
                "jurusan" to "Kelas", // Backend doesn't send class name in this endpoint efficiently, generic is fine or fix backend
                "mapelGuru" to "$mapel, [$guru]",
                "keterangan" to keterangan,
                "statusType" to statusType
            ))
        }

        totalStudents = allData.size // Approximate, effectively shows attendance records count
        
        resetFilter()
        updateAngkaTombol()
        updateTotalSiswa()
        
        Toast.makeText(this, "Data dimuat: ${allData.size}", Toast.LENGTH_SHORT).show()
    }

    private fun setupFilterButtons() {
        btnHadir.setOnClickListener { toggleFilter("hadir") }
        btnSakit.setOnClickListener { toggleFilter("sakit") }
        btnIzin.setOnClickListener { toggleFilter("izin") }
        btnAlpha.setOnClickListener { toggleFilter("alpha") }
    }

    private fun toggleFilter(status: String) {
        if (filterActive == status) {
            filterActive = null
            resetFilter()
            resetTextColors()
        } else {
            filterActive = status
            applyFilter(status)
            updateTextColors(status)
        }
        updateTombolAktif()
    }

    private fun applyFilter(status: String) {
        filteredData.clear()
        filteredData.addAll(allData.filter { it["statusType"] == status })
        adapter.notifyDataSetChanged()
    }

    private fun resetFilter() {
        filteredData.clear()
        filteredData.addAll(allData)
        adapter.notifyDataSetChanged()
    }

    private fun updateTombolAktif() {
         // Reset
        val defaultDrawable = android.R.drawable.ic_menu_save // Just fallback, usage of R.drawable.btn_guru_... needed
        
        try {
             btnHadir.setImageResource(R.drawable.btn_guru_hadir)
             btnSakit.setImageResource(R.drawable.btn_guru_sakit)
             btnIzin.setImageResource(R.drawable.btn_guru_izin)
             btnAlpha.setImageResource(R.drawable.btn_guru_alpha)
        } catch(e: Exception) { }

        // Active
        try {
            when (filterActive) {
                "hadir" -> btnHadir.setImageResource(R.drawable.btn_guru_hadir_active)
                "sakit" -> btnSakit.setImageResource(R.drawable.btn_guru_sakit_active)
                "izin" -> btnIzin.setImageResource(R.drawable.btn_guru_izin_active)
                "alpha" -> btnAlpha.setImageResource(R.drawable.btn_guru_alpha_active)
            }
        } catch(e: Exception) {}
    }

    private fun updateTextColors(activeStatus: String) {
        resetTextColors()
        when (activeStatus) {
            "hadir" -> txtHadirCount.setTextColor(textColorActive)
            "sakit" -> txtSakitCount.setTextColor(textColorActive)
            "izin" -> txtIzinCount.setTextColor(textColorActive)
            "alpha" -> txtAlphaCount.setTextColor(textColorActive)
        }
    }

    private fun resetTextColors() {
        txtHadirCount.setTextColor(textColorNormal)
        txtSakitCount.setTextColor(textColorNormal)
        txtIzinCount.setTextColor(textColorNormal)
        txtAlphaCount.setTextColor(textColorNormal)
    }

    private fun updateAngkaTombol() {
        var hadir = 0
        var sakit = 0
        var izin = 0
        var alpha = 0

        for (data in allData) {
            when (data["statusType"]) {
                "hadir" -> hadir++
                "sakit" -> sakit++
                "izin" -> izin++
                "alpha" -> alpha++
            }
        }

        txtHadirCount.text = hadir.toString()
        txtSakitCount.text = sakit.toString()
        txtIzinCount.text = izin.toString()
        txtAlphaCount.text = alpha.toString()
    }

    private fun updateTotalSiswa() {
        txtJumlahSiswa.text = "Total Kehadiran : $totalStudents"
    }

    private fun showErrorAndExit(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        handler.postDelayed({ finish() }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}