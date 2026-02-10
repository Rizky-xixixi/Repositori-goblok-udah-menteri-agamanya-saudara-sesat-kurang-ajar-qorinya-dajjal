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
import com.example.ritamesa.data.model.TeachingAttendanceItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranGuruActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
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

    // Data for adapter
    private val allData = Collections.synchronizedList(mutableListOf<Map<String, Any>>())
    private val filteredData = Collections.synchronizedList(mutableListOf<Map<String, Any>>())
    private lateinit var adapter: SimpleGuruAdapter
    private var filterActive: String? = null
    private var dateFilterActive = true // Default aktif filter tanggal saat membuka halaman

    private val handler = Handler(Looper.getMainLooper())
    private var isLoading = false
    private var selectedDate = Calendar.getInstance()

    private val textColorActive = android.graphics.Color.WHITE
    private val textColorNormal = android.graphics.Color.parseColor("#4B5563")
    private val textColorDefault = android.graphics.Color.BLACK

    companion object {
        private const val TAG = "RiwayatGuruActivity"
        private const val DATE_FORMAT = "dd-MM-yyyy"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.riwayat_kehadiran_guru_fix)

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

            handler.postDelayed({
                loadDataFromApi()
            }, 300)

        } catch (e: Exception) {
            Log.e(TAG, "FATAL ERROR in onCreate: ${e.message}", e)
            Toast.makeText(this, "Gagal memuat halaman: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initializeViews(): Boolean {
        return try {
            recyclerView = findViewById(R.id.recycler_riwayat)
            txtHadirCount = findViewById(R.id.txt_hadir_count)
            txtSakitCount = findViewById(R.id.txt_sakit_count)
            txtIzinCount = findViewById(R.id.txt_izin_count)
            txtAlphaCount = findViewById(R.id.txt_alpha_count)
            txtFilterTanggal = findViewById(R.id.text_filter_tanggal)

            btnHadir = findViewById(R.id.button_hadir)
            btnSakit = findViewById(R.id.button_sakit)
            btnIzin = findViewById(R.id.button_izin)
            btnAlpha = findViewById(R.id.button_alpha)

            iconCalendar = findViewById(R.id.icon_calendar)

            btnHome = findViewById(R.id.btnHome)
            btnChart = findViewById(R.id.btnChart)
            btnNotif = findViewById(R.id.btnNotif)

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeViews: ${e.message}", e)
            false
        }
    }

    private fun setupCalendarButton() {
        iconCalendar.setOnClickListener { showDatePicker() }
        txtFilterTanggal.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                dateFilterActive = true
                updateTanggalDisplay()
                applyDateFilter()

                // Reset status filter but keep date filter
                filterActive = null
                updateTombolAktif()
                resetTextColors()
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateTanggalDisplay() {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val formatted = sdf.format(selectedDate.time)
        val finalDate = if (formatted.isNotEmpty()) formatted[0].uppercaseChar() + formatted.substring(1) else formatted
        txtFilterTanggal.text = finalDate
    }

    private fun applyDateFilter() {
        if (isLoading) return

        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val selectedDateStr = dateFormat.format(selectedDate.time)

        val tempFilteredData = allData.filter {
            val tanggal = it["tanggal"] as? String ?: ""
            // Check if date string contains the selected date (dd-MM-yyyy)
            tanggal.contains(selectedDateStr)
        }

        filteredData.clear()
        filteredData.addAll(tempFilteredData)

        if (filterActive != null) {
            applyFilter(filterActive!!)
        } else {
            adapter.notifyDataSetChanged()
        }

        updateAngkaTombol()

        Log.d(TAG, "Date filter applied: ${filteredData.size} items for $selectedDateStr")
    }

    private fun setupRecyclerView() {
        adapter = SimpleGuruAdapter(this, filteredData)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupFooterNavigation() {
        btnHome.setOnClickListener {
            startActivity(Intent(this, DashboardGuruActivity::class.java))
            finish()
        }

        val btnAssignment = findViewById<ImageButton>(R.id.btnAssigment)
        btnAssignment?.setOnClickListener {
            // Reset filters tapi tetap aktifkan filter tanggal
            filterActive = null
            dateFilterActive = true
            selectedDate = Calendar.getInstance()
            updateTanggalDisplay()
            applyDateFilter()
            updateTombolAktif()
            resetTextColors()
        }

        btnChart.setOnClickListener {
            startActivity(Intent(this, TindakLanjutGuruActivity::class.java))
        }

        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotifikasiGuruActivity::class.java))
        }
    }

    private fun setupFilterButtons() {
        btnHadir.setOnClickListener { toggleFilter("hadir") }
        btnSakit.setOnClickListener { toggleFilter("sakit") }
        btnIzin.setOnClickListener { toggleFilter("izin") }
        btnAlpha.setOnClickListener { toggleFilter("alpha") }
    }

    private fun loadDataFromApi() {
        if (isLoading) return
        isLoading = true

        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.getTeachingHistory().enqueue(object : Callback<List<TeachingAttendanceItem>> {
            override fun onResponse(
                call: Call<List<TeachingAttendanceItem>>,
                response: Response<List<TeachingAttendanceItem>>
            ) {
                isLoading = false
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    processApiData(items)
                } else {
                    Log.d(TAG, "API error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<TeachingAttendanceItem>>, t: Throwable) {
                isLoading = false
                Log.e(TAG, "Network error: ${t.message}", t)
            }
        })
    }

    private fun processApiData(items: List<TeachingAttendanceItem>) {
        allData.clear()

        val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        items.forEach { item ->
            val dateObj = try { inputFormat.parse(item.date) } catch(e: Exception) { Date() }
            val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(dateObj)
            val fullDateStr = "$formattedDate ${item.schedule?.startTime ?: "00:00"}"

            val (statusType, statusText) = when (item.status) {
                "present" -> "hadir" to "Hadir"
                "late" -> "hadir" to "Terlambat"
                "sick" -> "sakit" to "Sakit"
                "excused" -> "izin" to "Izin"
                "absent" -> "alpha" to "Alpha"
                else -> "alpha" to "Tanpa Keterangan"
            }

            allData.add(mapOf(
                "id" to item.id,
                "mapel" to (item.schedule?.subjectInfo?.name ?: "-"),
                "kelas" to (item.schedule?.classInfo?.name ?: "-"),
                "status" to statusText,
                "tanggal" to fullDateStr,
                "statusType" to statusType
            ))
        }

        // Sort by date desc
        allData.sortByDescending { it["tanggal"] as String }

        // Otomatis filter untuk tanggal saat ini (hari ini)
        applyDateFilter()

        Log.d(TAG, "Loaded ${allData.size} teaching records")
    }

    private fun toggleFilter(status: String) {
        if (filterActive == status) {
            filterActive = null
            resetFilter() // Resets to date filter if active
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

        if (dateFilterActive) {
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val selectedDateStr = dateFormat.format(selectedDate.time)

            filteredData.addAll(allData.filter {
                val sType = it["statusType"] as String
                val tanggal = it["tanggal"] as String
                sType == status && tanggal.contains(selectedDateStr)
            })
        } else {
            filteredData.addAll(allData.filter { it["statusType"] == status })
        }
        adapter.notifyDataSetChanged()
        updateAngkaTombolForFilter(status)
    }

    private fun resetFilter() {
        filteredData.clear()
        if (dateFilterActive) {
            // Gunakan applyDateFilter() untuk konsistensi
            applyDateFilter()
        } else {
            filteredData.addAll(allData)
            adapter.notifyDataSetChanged()
            updateAngkaTombol()
        }
    }

    private fun updateAngkaTombol() {
        var h = 0; var s = 0; var i = 0; var a = 0
        filteredData.forEach {
            when(it["statusType"]) {
                "hadir" -> h++
                "sakit" -> s++
                "izin" -> i++
                "alpha" -> a++
            }
        }
        txtHadirCount.text = h.toString()
        txtSakitCount.text = s.toString()
        txtIzinCount.text = i.toString()
        txtAlphaCount.text = a.toString()
    }

    private fun updateAngkaTombolForFilter(activeStatus: String) {
        // Calculate totals for the CURRENT SCOPE (Date) regardless of status filter
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val selectedDateStr = dateFormat.format(selectedDate.time)

        var h = 0; var s = 0; var i = 0; var a = 0
        allData.forEach {
            val tanggal = it["tanggal"] as? String ?: ""
            if (tanggal.contains(selectedDateStr)) {
                when(it["statusType"]) {
                    "hadir" -> h++
                    "sakit" -> s++
                    "izin" -> i++
                    "alpha" -> a++
                }
            }
        }

        txtHadirCount.text = h.toString()
        txtSakitCount.text = s.toString()
        txtIzinCount.text = i.toString()
        txtAlphaCount.text = a.toString()

        Log.d(TAG, "Filter $activeStatus: ${filteredData.size} items")
    }

    private fun updateTombolAktif() {
        btnHadir.setImageResource(if(filterActive == "hadir") R.drawable.btn_guru_hadir_active else R.drawable.btn_guru_hadir)
        btnSakit.setImageResource(if(filterActive == "sakit") R.drawable.btn_guru_sakit_active else R.drawable.btn_guru_sakit)
        btnIzin.setImageResource(if(filterActive == "izin") R.drawable.btn_guru_izin_active else R.drawable.btn_guru_izin)
        btnAlpha.setImageResource(if(filterActive == "alpha") R.drawable.btn_guru_alpha_active else R.drawable.btn_guru_alpha)
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}