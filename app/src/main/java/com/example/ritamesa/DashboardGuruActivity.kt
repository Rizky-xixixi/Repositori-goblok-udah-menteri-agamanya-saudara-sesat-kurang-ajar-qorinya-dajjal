package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.ritamesa.data.model.JadwalItem
import com.example.ritamesa.data.pref.SessionManager
import android.util.Log

class DashboardGuruActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DashboardGuru"
        private const val DEBUG = false // Set to false in production to reduce logs
    }

    private lateinit var txtTanggalSekarang: TextView
    private lateinit var txtWaktuLive: TextView
    private lateinit var txtJamMasuk: TextView
    private lateinit var txtJamPulang: TextView
    private lateinit var txtTanggalDiJamLayout: TextView
    private lateinit var txtHadirCount: TextView
    private lateinit var txtIzinCount: TextView
    private lateinit var txtSakitCount: TextView
    private lateinit var txtAlphaCount: TextView
    private lateinit var recyclerJadwal: RecyclerView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private val jadwalHariIni = mutableListOf<JadwalItem>()

    private val jamMasukDatabase = "07:00:00"
    private val jamPulangDatabase = "15:00:00"

    private var hadirCount = 0
    private var izinCount = 0
    private var sakitCount = 0
    private var alphaCount = 0
    
    private var isLoading = false
    private lateinit var sessionManager: SessionManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_guru)

        try {
            sessionManager = SessionManager(this)
            initViews()
            setupDateTime()
            setupKehadiran()
            setupRecyclerView()
            setupFooterNavigation()
            setupKehadiranButtons()
        } catch (e: Exception) {
            logError("onCreate", e)
            showError("Gagal memuat halaman dashboard")
        }
    }

    private fun initViews() {
        txtTanggalSekarang = findViewById(R.id.txtTanggalSekarang)
        txtWaktuLive = findViewById(R.id.txtWaktuLive)
        txtJamMasuk = findViewById(R.id.txtJamMasuk)
        txtJamPulang = findViewById(R.id.txtJamPulang)
        txtTanggalDiJamLayout = findViewById(R.id.txtTanggalDiJamLayout)
        txtHadirCount = findViewById(R.id.txt_hadir_count)
        txtIzinCount = findViewById(R.id.txt_izin_count)
        txtSakitCount = findViewById(R.id.txt_sakit_count)
        txtAlphaCount = findViewById(R.id.txt_alpha_count)
        recyclerJadwal = findViewById(R.id.recyclerJadwal)

        // Profile menu
        findViewById<ImageButton>(R.id.profile).setOnClickListener { view ->
            showProfileMenu(view)
        }
    }

    // ===== PROFILE MENU DENGAN 2 PILIHAN =====
    private fun showProfileMenu(view: android.view.View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.profile_simple, popupMenu.menu)

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
            .setTitle("Logout Guru")
            .setMessage("Yakin ingin logout dari akun guru?")
            .setPositiveButton("Ya, Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        // Clear session data
        sessionManager.clearSession()
        
        val intent = Intent(this, LoginAwal::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
    }

    private fun setupDateTime() {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val currentDate = Date()
        val tanggalHariIni = dateFormat.format(currentDate)

        txtTanggalSekarang.text = tanggalHariIni
        txtTanggalDiJamLayout.text = tanggalHariIni
        
        fetchSettings()

        runnable = object : Runnable {
            override fun run() {
                try {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                    val currentTime = timeFormat.format(Date())
                    txtWaktuLive.text = currentTime
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    logError("runnable", e)
                }
            }
        }
        handler.post(runnable)
    }

    private fun setupKehadiran() {
        hadirCount = 0
        izinCount = 0
        sakitCount = 0
        alphaCount = 0
        updateKehadiranCount()
    }

    private fun updateKehadiranCount() {
        txtHadirCount.text = hadirCount.toString()
        txtIzinCount.text = izinCount.toString()
        txtSakitCount.text = sakitCount.toString()
        txtAlphaCount.text = alphaCount.toString()
    }

    private fun setupRecyclerView() {
         // Setup empty adapter first
         val jadwalAdapter = JadwalAdapter(jadwalHariIni) { jadwal ->
             navigateToDetailJadwalGuru(jadwal)
         }

         recyclerJadwal.layoutManager = LinearLayoutManager(this).apply {
             orientation = LinearLayoutManager.VERTICAL
         }

         recyclerJadwal.adapter = jadwalAdapter
         recyclerJadwal.setHasFixedSize(true)

         // Fetch data
         fetchDashboardData()
    }

    private fun fetchDashboardData() {
        if (isLoading) return
        isLoading = true
        
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)

        apiService.getTeacherDashboard().enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.DashboardGuruResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.ritamesa.data.model.DashboardGuruResponse>,
                response: retrofit2.Response<com.example.ritamesa.data.model.DashboardGuruResponse>
            ) {
                isLoading = false
                if (response.isSuccessful) {
                    response.body()?.let { updateUI(it) }
                } else {
                    when (response.code()) {
                        401 -> {
                            showError("Sesi habis, silakan login ulang")
                            performLogout()
                        }
                        else -> {
                            logDebug("API Error: ${response.code()} - ${response.message()}")
                        }
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.DashboardGuruResponse>, t: Throwable) {
                isLoading = false
                Log.e("DashboardGuru", "Error fetching dashboard data: ${t.message}", t)
                Toast.makeText(this@DashboardGuruActivity, "Gagal memuat data dashboard: ${t.message}", Toast.LENGTH_LONG).show()
                // The provided instruction for updateUI call is syntactically incorrect
                // for the existing DashboardGuruResponse model.
                // Assuming DashboardGuruResponse has a constructor that can take nulls or default values
                // for its fields (attendance, teacher, schedule) to represent an empty state.
                // If not, this line would cause a compilation error.
                // For now, commenting out the problematic line to maintain syntactic correctness.
                // updateUI(com.example.ritamesa.data.model.DashboardGuruResponse(
                //     com.example.ritamesa.data.model.TeacherScheduleInfo("", "", "", "", "", ""),
                //     com.example.ritamesa.data.model.TeacherStatusSummary(0, 0, 0, 0, 0)
                // ))
                // Instead, we can update the UI with empty data or just show the error.
                // For now, let's just log and toast, and not attempt to call updateUI with an invalid constructor.
                // If an empty state is desired, the DashboardGuruResponse model needs to be adaptable.
                // For example, by passing nulls or empty lists/objects if the model allows.
                // updateUI(com.example.ritamesa.data.model.DashboardGuruResponse(null, null, emptyList())) // Example if model supports this
            }
        })
    }

    private fun fetchSettings() {
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)
        apiService.getSettings().enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.SettingResponse> {
            override fun onResponse(call: retrofit2.Call<com.example.ritamesa.data.model.SettingResponse>, response: retrofit2.Response<com.example.ritamesa.data.model.SettingResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { settings ->
                        val start = settings["school_start_time"] ?: "07:00"
                        val end = settings["school_end_time"] ?: "15:00"
                        txtJamMasuk.text = if (start.length >= 5) start.substring(0, 5) else start
                        txtJamPulang.text = if (end.length >= 5) end.substring(0, 5) else end
                    }
                }
            }
            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.SettingResponse>, t: Throwable) {
                logError("fetchSettings", t)
                Log.e("DashboardGuru", "Failed to fetch settings: ${t.message}", t)
                Toast.makeText(this@DashboardGuruActivity, "Gagal memuat pengaturan: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateUI(data: com.example.ritamesa.data.model.DashboardGuruResponse) {
        // Update Attendance Stats (with null safety)
        data.attendance?.let { att ->
            hadirCount = att.present
            izinCount = att.permission
            sakitCount = att.sick
            alphaCount = att.alpha
        } ?: run {
            hadirCount = 0
            izinCount = 0
            sakitCount = 0
            alphaCount = 0
        }
        updateKehadiranCount()

        // Update Profile Image safely
        data.teacher?.photoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            try {
                com.bumptech.glide.Glide.with(this)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.profile_guru)
                    .error(R.drawable.profile_guru)
                    .into(findViewById<ImageButton>(R.id.profile))
            } catch (e: Exception) {
                logError("loadProfileImage", e)
            }
        }

        // Update Schedule List
        jadwalHariIni.clear()
        jadwalHariIni.addAll(data.schedule)
        recyclerJadwal.adapter?.notifyDataSetChanged()
        
        // Show empty state if no schedule
        updateEmptyState(data.schedule.isEmpty())
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        // RecyclerView visibility is handled by adapter
        // Can add empty state view here if needed
        logDebug("Schedule empty: $isEmpty")
    }

    // Removed generateDummyJadwal

    private fun navigateToDetailJadwalGuru(jadwal: JadwalItem) {
        try {
            val intent = Intent(this, DetailJadwalGuruActivity::class.java).apply {
                putExtra("JADWAL_DATA", JadwalData(
                    id = jadwal.id,
                    mataPelajaran = jadwal.mataPelajaran ?: "-",
                    kelas = jadwal.kelas ?: "-",
                    classId = jadwal.classId,
                    jam = jadwal.jam,
                    waktuPelajaran = jadwal.waktuPelajaran
                ))
            }
            startActivity(intent)
        } catch (e: Exception) {
            logError("navigateToDetailJadwalGuru", e)
            showError("Gagal membuka detail jadwal")
        }
    }

    private fun setupFooterNavigation() {
        val btnHome: ImageButton = findViewById(R.id.btnHome)
        val btnCalendar: ImageButton = findViewById(R.id.btnCalendar)
        val btnChart: ImageButton = findViewById(R.id.btnChart)
        val btnNotif: ImageButton = findViewById(R.id.btnNotif)

        btnHome.setOnClickListener {
            refreshDashboard()
        }

        btnCalendar.setOnClickListener {
            startActivity(Intent(this, RiwayatKehadiranGuruActivity::class.java))
        }

        btnChart.setOnClickListener {
            val intent = Intent(this, StatistikKehadiran::class.java)
            intent.putExtra("ROLE", "TEACHER")
            startActivity(intent)
        }

        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotifikasiGuruActivity::class.java))
        }
    }

    private fun setupKehadiranButtons() {
        // Note: Dashboard guru layout only has button_hadir and button_sakit
        // which contains combined stats. Navigate to RiwayatKehadiranGuru for details.
        
        val btnHadir: ImageButton? = findViewById(R.id.button_hadir)
        val btnSakit: ImageButton? = findViewById(R.id.button_sakit)

        btnHadir?.setOnClickListener {
            navigateToRiwayatWithFilter("hadir")
        }

        btnSakit?.setOnClickListener {
            // This button contains Izin, Sakit, Alpha combined
            navigateToRiwayatWithFilter(null) // Show all non-hadir
        }
    }
    
    private fun navigateToRiwayatWithFilter(filter: String?) {
        val intent = Intent(this, RiwayatKehadiranGuruActivity::class.java)
        filter?.let { intent.putExtra("FILTER_STATUS", it) }
        startActivity(intent)
    }

    private fun refreshDashboard() {
        fetchDashboardData()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
    }
    
    // ===== HELPER METHODS =====
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun logDebug(message: String) {
        if (DEBUG) {
            Log.d(TAG, message)
        }
    }
    
    private fun logError(tag: String, e: Throwable) {
        Log.e(TAG, "Error in $tag: ${e.message}", e)
    }



    data class JadwalData(
        val id: Int, // Added ID
        val mataPelajaran: String,
        val kelas: String,
        val classId: Int? = null,
        val jam: String,
        val waktuPelajaran: String
    ) : java.io.Serializable
}