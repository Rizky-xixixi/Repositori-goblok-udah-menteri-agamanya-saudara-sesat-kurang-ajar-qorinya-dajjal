package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import com.example.ritamesa.data.model.JadwalItem
import java.util.*

class DashboardSiswaActivity : AppCompatActivity() {

    private lateinit var txtTanggalSekarang: TextView
    private lateinit var txtWaktuLive: TextView
    private lateinit var txtJamMasuk: TextView
    private lateinit var txtJamPulang: TextView
    private lateinit var recyclerJadwal: RecyclerView
    private lateinit var btnHome: ImageButton
    private lateinit var btnAssignment: ImageButton
    private lateinit var profileSiswa: ImageView
    private lateinit var profileOverlay: ImageView
    private lateinit var txtTanggalDiJamLayout: TextView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private val jadwalHariIni = mutableListOf<JadwalSiswaItem>()
    private var isPengurus = false

    private val jamMasukDatabase = "07:00:00"
    private val jamPulangDatabase = "15:00:00"

    companion object {
        private const val TAG = "DashboardSiswa"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "=== DASHBOARD SISWA ACTIVITY START ===")

        try {
            isPengurus = intent.getBooleanExtra("IS_PENGURUS", false)
            Log.d(TAG, "isPengurus = $isPengurus")

            setContentView(R.layout.dashboard_siswa)
            Log.d(TAG, "Layout loaded successfully")

            initViews()
            setupDateTime()
            setupProfileImage()
            setupRecyclerView()
            setupButtonListeners()
            updateRoleUI()

            Toast.makeText(this, "Dashboard dimuat sukses", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "ERROR in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        try {
            txtTanggalSekarang = findViewById(R.id.txtTanggalSekarang)
            txtWaktuLive = findViewById(R.id.txtWaktuLive)
            txtJamMasuk = findViewById(R.id.txtJamMasuk)
            txtJamPulang = findViewById(R.id.txtJamPulang)
            recyclerJadwal = findViewById(R.id.recyclerJadwal)
            btnHome = findViewById(R.id.btnHome)
            btnAssignment = findViewById(R.id.btnAssignment)
            profileSiswa = findViewById(R.id.profile_siswa)
            profileOverlay = findViewById(R.id.profile)
            txtTanggalDiJamLayout = findViewById(R.id.txtTanggalDiJamLayout)

            // TAMBAHKAN POPUP MENU DI PROFILE
            findViewById<ImageButton>(R.id.profile).setOnClickListener { view ->
                showProfileMenu(view)
            }

            Log.d(TAG, "=== VIEW INITIALIZATION ===")
            Log.d(TAG, "txtTanggalSekarang: ${txtTanggalSekarang != null}")

        } catch (e: Exception) {
            Log.e(TAG, "Error in initViews: ${e.message}", e)
            Toast.makeText(this, "Error finding views: ${e.message}", Toast.LENGTH_LONG).show()
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
                    // Do nothing or dismiss
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showLogoutConfirmation() {
        val role = if (isPengurus) "Pengurus Kelas" else "Siswa"
        android.app.AlertDialog.Builder(this)
            .setTitle("Logout $role")
            .setMessage("Yakin ingin logout dari akun $role?")
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
        val role = if (isPengurus) "pengurus" else "siswa"
        Toast.makeText(this, "Logout $role berhasil", Toast.LENGTH_SHORT).show()
    }

    private fun setupDateTime() {
        try {
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val currentDate = Date()
            val tanggalHariIni = dateFormat.format(currentDate)

            val tanggalFormatBesar = tanggalHariIni.toUpperCase(Locale.forLanguageTag("id-ID"))

            txtTanggalSekarang.text = tanggalFormatBesar
            txtTanggalDiJamLayout.text = tanggalFormatBesar
            
            fetchSettings()

            Log.d(TAG, "Date setup: $tanggalFormatBesar")

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
            Log.e(TAG, "Error in setupDateTime: ${e.message}")
        }
    }

    private fun setupProfileImage() {
        try {
            if (isPengurus) {
                profileSiswa.setImageResource(R.drawable.profile_pengurus)
                profileOverlay.setImageResource(R.drawable.profile_p)
                Log.d(TAG, "Profile set to PENGURUS")
            } else {
                profileSiswa.setImageResource(R.drawable.profile_siswa)
                profileOverlay.setImageResource(R.drawable.profile_p)
                Log.d(TAG, "Profile set to SISWA BIASA")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupProfileImage: ${e.message}", e)
        }
    }

    private fun setupRecyclerView() {
        // Setup empty adapter first
        val adapter = JadwalSiswaAdapter(jadwalHariIni)
        recyclerJadwal.layoutManager = LinearLayoutManager(this)
        recyclerJadwal.setHasFixedSize(true)
        recyclerJadwal.adapter = adapter

        // Fetch data
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)

        apiService.getStudentDashboard().enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.DashboardSiswaResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.ritamesa.data.model.DashboardSiswaResponse>,
                response: retrofit2.Response<com.example.ritamesa.data.model.DashboardSiswaResponse>
            ) {
                if (response.isSuccessful) {
                    val dashboardData = response.body()
                    if (dashboardData != null) {
                        updateUI(dashboardData)
                    }
                } else {
                     if (response.code() == 401) {
                         Toast.makeText(this@DashboardSiswaActivity, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show()
                         performLogout()
                     } else {
                         Toast.makeText(this@DashboardSiswaActivity, "Gagal memuat data: ${response.message()}", Toast.LENGTH_SHORT).show()
                     }
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.DashboardSiswaResponse>, t: Throwable) {
                Log.e(TAG, "Error fetching dashboard data: ${t.message}", t)
                Toast.makeText(this@DashboardSiswaActivity, "Gagal memuat data dashboard: ${t.message}", Toast.LENGTH_LONG).show()
                // Optionally, update UI with empty data or a message
                updateUI(com.example.ritamesa.data.model.DashboardSiswaResponse(
                    schedule = emptyList(),
                    student = null // Assuming student can be null
                ))
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
                Log.e(TAG, "Failed to fetch settings: ${t.message}", t)
                Toast.makeText(this@DashboardSiswaActivity, "Gagal memuat pengaturan waktu sekolah: ${t.message}", Toast.LENGTH_LONG).show()
                // Fallback to defaults already in XML or previously set
            }
        })
    }

    private fun updateUI(data: com.example.ritamesa.data.model.DashboardSiswaResponse) {
        // Update Schedule List
        jadwalHariIni.clear()
        
        // Map API response to local model
        data.schedule.forEach { apiItem ->
            val statusLabel = apiItem.statusLabel ?: "Belum Absen"
            val displayStatus = if (apiItem.status == "present") "Hadir" else statusLabel
            
            jadwalHariIni.add(
                JadwalSiswaItem(
                    id = apiItem.id,
                    sesi = apiItem.jam, 
                    mataPelajaran = apiItem.mataPelajaran,
                    status = apiItem.status ?: "none",
                    jam = "${apiItem.startTime} - ${apiItem.endTime}",
                    keterangan = displayStatus
                )
            )
        }
        
        recyclerJadwal.adapter?.notifyDataSetChanged()
        
        // Update Profile Image (with null safety)
        val photoUrl = data.student?.photoUrl
        if (!photoUrl.isNullOrEmpty()) {
            try {
                // Determine default placeholder based on role
                val defaultPlaceholder = if (isPengurus) R.drawable.profile_pengurus else R.drawable.profile_siswa
                val smallPlaceholder = R.drawable.profile_p
                
                // Load into small profile icon (overlay/toolbar)
                com.bumptech.glide.Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(smallPlaceholder)
                    .error(smallPlaceholder)
                    .into(profileOverlay)
                    
                // Load into big profile image
                com.bumptech.glide.Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(defaultPlaceholder)
                    .error(defaultPlaceholder)
                    .into(profileSiswa)
                    
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile image", e)
            }
        }
        
        // Update Date if needed
        // txtTanggalSekarang.text = data.date
    }

    // Removed generateDummyJadwal

    private fun setupButtonListeners() {
        try {
            Log.d(TAG, "=== SETUP BUTTON LISTENERS ===")

            if (btnHome != null) {
                Log.d(TAG, "btnHome FOUND, setting onClick...")
                btnHome.setOnClickListener {
                    Toast.makeText(this, "Anda sudah di Dashboard", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Tombol Home diklik")
                }
                Log.d(TAG, "btnHome onClick listener SET")
            } else {
                Log.e(TAG, "btnHome is NULL!")
            }

            if (btnAssignment != null) {
                Log.d(TAG, "btnAssignment FOUND, setting onClick...")
                btnAssignment.setOnClickListener {
                    Log.d(TAG, ">>> TOMBOL ASSIGNMENT DIKLIK! <<<")

                    try {
                        if (isPengurus) {
                            Toast.makeText(this, "Membuka Riwayat Kehadiran Kelas (Pengurus)...", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, RiwayatKehadiranKelasPengurusActivity::class.java)
                            intent.putExtra("IS_PENGURUS", true)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            Log.d(TAG, "Navigasi ke RiwayatKehadiranKelasPengurusActivity BERHASIL")
                        } else {
                            Toast.makeText(this, "Membuka Riwayat Kehadiran Kelas (Siswa)...", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, RiwayatKehadiranKelasSiswaActivity::class.java)
                            intent.putExtra("IS_PENGURUS", false)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            Log.d(TAG, "Navigasi ke RiwayatKehadiranKelasSiswaActivity BERHASIL")
                        }
                    } catch (e: ClassNotFoundException) {
                        Log.e(TAG, "CLASS NOT FOUND ERROR!", e)
                        Toast.makeText(this,
                            "Error: Activity tidak ditemukan!",
                            Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "GENERAL ERROR: ${e.message}", e)
                        Toast.makeText(this,
                            "Gagal membuka halaman: ${e.localizedMessage}",
                            Toast.LENGTH_LONG).show()
                    }
                }
                Log.d(TAG, "btnAssignment onClick listener SET")
            } else {
                Log.e(TAG, "btnAssignment is NULL!")
                Toast.makeText(this, "ERROR: Tombol Riwayat tidak ditemukan", Toast.LENGTH_LONG).show()
            }

            Log.d(TAG, "=== BUTTON LISTENERS SETUP COMPLETE ===")

        } catch (e: Exception) {
            Log.e(TAG, "Error in setupButtonListeners: ${e.message}", e)
            Toast.makeText(this, "Error setting up buttons: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateRoleUI() {
        val role = if (isPengurus) "Pengurus Kelas" else "Siswa"
        Toast.makeText(this, "Selamat datang, $role!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            handler.removeCallbacks(runnable)
            Log.d(TAG, "=== DASHBOARD ACTIVITY DESTROYED ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing handler callbacks: ${e.message}")
        }
    }

    private inner class JadwalSiswaAdapter(
        private val jadwalList: List<JadwalSiswaItem>
    ) : RecyclerView.Adapter<JadwalSiswaAdapter.JadwalViewHolder>() {

        inner class JadwalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtMataPelajaran: TextView = itemView.findViewById(R.id.MataPelajaran)
            val txtMapelDetail: TextView = itemView.findViewById(R.id.Mata_pelajaran)
            val txtKeterangan: TextView = itemView.findViewById(R.id.Text_keterangan_hadir)
            val txtJam: TextView = itemView.findViewById(R.id.tvJam_1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalViewHolder {
            try {
                Log.d(TAG, "Adapter: onCreateViewHolder")
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_dashboard_siswa, parent, false)
                return JadwalViewHolder(view)
            } catch (e: Exception) {
                Log.e(TAG, "Adapter ERROR in onCreateViewHolder: ${e.message}", e)
                throw e
            }
        }

        override fun onBindViewHolder(holder: JadwalViewHolder, position: Int) {
            try {
                if (position >= jadwalList.size) {
                    Log.e(TAG, "Adapter: Invalid position $position")
                    return
                }

                val jadwal = jadwalList[position]
                holder.txtMataPelajaran.text = jadwal.sesi
                holder.txtMapelDetail.text = jadwal.mataPelajaran
                holder.txtKeterangan.text = jadwal.keterangan
                holder.txtJam.text = jadwal.jam

                Log.d(TAG, "Adapter: Bound position $position - ${jadwal.sesi}")

            } catch (e: Exception) {
                Log.e(TAG, "Adapter ERROR in onBindViewHolder: ${e.message}", e)
            }
        }

        override fun getItemCount(): Int {
            val count = jadwalList.size
            Log.d(TAG, "Adapter: getItemCount = $count")
            return count
        }
    }

    data class JadwalSiswaItem(
        val id: Int,
        val sesi: String,
        val mataPelajaran: String,
        val status: String,
        val jam: String,
        val keterangan: String
    )


}