package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import android.util.Log

class DashboardGuruActivity : AppCompatActivity() {

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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_guru)

        initViews()
        setupDateTime()
        setupKehadiran()
        setupRecyclerView()
        setupFooterNavigation()
        setupKehadiranButtons()
    }

    private fun initViews() {
        try {
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

            // TAMBAHKAN POPUP MENU DI PROFILE
            findViewById<ImageButton>(R.id.profile).setOnClickListener { view ->
                showProfileMenu(view)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error initViews: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // ===== PROFILE MENU DENGAN 2 PILIHAN =====
    private fun showProfileMenu(view: android.view.View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.profile_simple, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    showLogoutConfirmation()
                    true
                }
                R.id.menu_cancel -> {
                    Toast.makeText(this, "Menu dibatalkan", Toast.LENGTH_SHORT).show()
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
        val intent = Intent(this, LoginAwal::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Logout guru berhasil", Toast.LENGTH_SHORT).show()
    }

    // ===== FUNGSI LAINNYA TETAP SAMA =====
    private fun setupDateTime() {
        try {
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val currentDate = Date()
            val tanggalHariIni = dateFormat.format(currentDate)

            txtTanggalSekarang.text = tanggalHariIni
            txtTanggalDiJamLayout.text = tanggalHariIni
            txtJamMasuk.text = jamMasukDatabase
            txtJamPulang.text = jamPulangDatabase

            runnable = object : Runnable {
                override fun run() {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                    val currentTime = timeFormat.format(Date())
                    txtWaktuLive.text = currentTime
                    handler.postDelayed(this, 1000)
                }
            }

            handler.post(runnable)
        } catch (e: Exception) {
            Toast.makeText(this, "Error setupDateTime: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupKehadiran() {
        try {
            hadirCount = 0
            izinCount = 0
            sakitCount = 0
            alphaCount = 0

            updateKehadiranCount()
        } catch (e: Exception) {
            Toast.makeText(this, "Error setupKehadiran: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateKehadiranCount() {
        try {
            txtHadirCount.text = hadirCount.toString()
            txtIzinCount.text = izinCount.toString()
            txtSakitCount.text = sakitCount.toString()
            txtAlphaCount.text = alphaCount.toString()
        } catch (e: Exception) {
            // Log error
        }
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
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)
        
        // Show loading state if needed

        apiService.getTeacherDashboard().enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.DashboardGuruResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.ritamesa.data.model.DashboardGuruResponse>,
                response: retrofit2.Response<com.example.ritamesa.data.model.DashboardGuruResponse>
            ) {
                if (response.isSuccessful) {
                    val dashboardData = response.body()
                    if (dashboardData != null) {
                        updateUI(dashboardData)
                    }
                } else {
                    if (response.code() == 401) {
                         Toast.makeText(this@DashboardGuruActivity, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show()
                         performLogout()
                    } else {
                         Toast.makeText(this@DashboardGuruActivity, "Gagal memuat data: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.DashboardGuruResponse>, t: Throwable) {
                Toast.makeText(this@DashboardGuruActivity, "Error koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("DashboardGuru", "Error fetching dashboard", t)
            }
        })
    }

    private fun updateUI(data: com.example.ritamesa.data.model.DashboardGuruResponse) {
        // Update Attendance Stats
        hadirCount = data.attendance.present
        izinCount = data.attendance.permission
        sakitCount = data.attendance.sick
        alphaCount = data.attendance.alpha
        updateKehadiranCount()

        // Update Date/Time from Server (Optional, currently using local)
        // txtTanggalSekarang.text = data.date

        // Update Profile Image
        if (!data.teacher.photoUrl.isNullOrEmpty()) {
             try {
                 com.bumptech.glide.Glide.with(this)
                     .load(data.teacher.photoUrl)
                     .circleCrop()
                     .placeholder(R.drawable.profile_guru) // Default fallback
                     .error(R.drawable.profile_guru)
                     .into(findViewById<ImageButton>(R.id.profile))
             } catch (e: Exception) {
                 Log.e("DashboardGuru", "Error loading profile image", e)
             }
        }

        // Update Schedule List
        jadwalHariIni.clear()
        jadwalHariIni.addAll(data.schedule)
        recyclerJadwal.adapter?.notifyDataSetChanged()
    }

    // Removed generateDummyJadwal

    private fun navigateToDetailJadwalGuru(jadwal: JadwalItem) {
        try {
            val intent = Intent(this, DetailJadwalGuruActivity::class.java).apply {
                putExtra("JADWAL_DATA", JadwalData(
                    mataPelajaran = jadwal.mataPelajaran,
                    kelas = jadwal.kelas,
                    jam = jadwal.jam,
                    waktuPelajaran = jadwal.waktuPelajaran
                ))
            }
            startActivity(intent)
            Toast.makeText(this, "Membuka detail: ${jadwal.mataPelajaran}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error navigate: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupFooterNavigation() {
        try {
            val btnHome: ImageButton = findViewById(R.id.btnHome)
            val btnCalendar: ImageButton = findViewById(R.id.btnCalendar)
            val btnChart: ImageButton = findViewById(R.id.btnChart)
            val btnNotif: ImageButton = findViewById(R.id.btnNotif)

            btnHome.setOnClickListener {
                refreshDashboard()
                Toast.makeText(this, "Dashboard Guru direfresh", Toast.LENGTH_SHORT).show()
            }

            btnCalendar.setOnClickListener {
                val intent = Intent(this, RiwayatKehadiranGuruActivity::class.java)
                startActivity(intent)
            }

            btnChart.setOnClickListener {
                val intent = Intent(this, TindakLanjutGuruActivity::class.java)
                startActivity(intent)
            }

            btnNotif.setOnClickListener {
                val intent = Intent(this, NotifikasiGuruActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error footer nav: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupKehadiranButtons() {
        try {
            val btnHadir: ImageButton = findViewById(R.id.button_hadir)
            val btnIzin: ImageButton = findViewById(R.id.button_izin)
            val btnSakit: ImageButton = findViewById(R.id.button_sakit)
            val btnAlpha: ImageButton = findViewById(R.id.button_alpha)

            btnHadir.setOnClickListener {
                Toast.makeText(this, "Lihat siswa Hadir", Toast.LENGTH_SHORT).show()
            }

            btnIzin.setOnClickListener {
                Toast.makeText(this, "Lihat siswa Izin", Toast.LENGTH_SHORT).show()
            }

            btnSakit.setOnClickListener {
                Toast.makeText(this, "Lihat siswa Sakit", Toast.LENGTH_SHORT).show()
            }

            btnAlpha.setOnClickListener {
                Toast.makeText(this, "Lihat siswa Alpha", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error kehadiran buttons: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshDashboard() {
        Toast.makeText(this, "Memuat ulang data...", Toast.LENGTH_SHORT).show()
        fetchDashboardData()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }



    data class JadwalData(
        val mataPelajaran: String,
        val kelas: String,
        val jam: String,
        val waktuPelajaran: String
    ) : java.io.Serializable
}