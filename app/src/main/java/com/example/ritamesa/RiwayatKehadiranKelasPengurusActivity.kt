package com.example.ritamesa

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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

class RiwayatKehadiranKelasPengurusActivity : AppCompatActivity() {

    private lateinit var recyclerRiwayat: RecyclerView
    private lateinit var txtHadirCount: TextView
    private lateinit var txtIzinCount: TextView
    private lateinit var txtSakitCount: TextView
    private lateinit var txtAlphaCount: TextView
    private lateinit var txtFilterTanggal: TextView
    private lateinit var btnCalendar: ImageButton
    private lateinit var txtJumlah: TextView

    private val riwayatList = mutableListOf<RiwayatPengurusItem>()
    
    // Map subject/schedule ID to list of students attendance
    private val subjectDetailsMap = mutableMapOf<String, List<RiwayatAbsenItem>>()

    private var totalHadir = 0
    private var totalIzin = 0
    private var totalSakit = 0
    private var totalAlpha = 0

    private var isPengurus = true
    private var currentDate = Date()
    private lateinit var adapter: RiwayatPengurusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            isPengurus = intent.getBooleanExtra("IS_PENGURUS", true)
            setContentView(R.layout.riwayat_kehadiran_kelas_pengurus)

            initViews()
            setupRecyclerView()
            setupButtonListeners()
            setupBackPressedHandler()
            
            loadDataFromApi()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        txtHadirCount = findViewById(R.id.txt_hadir_count)
        txtIzinCount = findViewById(R.id.txt_izin_count)
        txtSakitCount = findViewById(R.id.txt_sakit_count)
        txtAlphaCount = findViewById(R.id.txt_alpha_count)
        txtFilterTanggal = findViewById(R.id.text_filter_tanggal)
        btnCalendar = findViewById(R.id.icon_calendar)
        btnCalendar.visibility = View.GONE // No custom date filter for now, defaults to today
        txtJumlah = findViewById(R.id.text_jumlah_siswa)
        recyclerRiwayat = findViewById(R.id.recycler_riwayat)

        updateTanggalDisplay()
    }

    private fun updateTanggalDisplay() {
        try {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val formatted = sdf.format(currentDate)
            val finalDate = if (formatted.isNotEmpty()) {
                formatted[0].uppercaseChar() + formatted.substring(1)
            } else {
                formatted
            }
            txtFilterTanggal.text = finalDate
        } catch (e: Exception) {
            txtFilterTanggal.text = "-"
        }
    }

    private fun loadDataFromApi() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateStr = dateFormat.format(currentDate)

        apiService.getClassAttendance(fromDate = dateStr, toDate = dateStr).enqueue(object : Callback<List<HomeroomAttendanceItem>> {
            override fun onResponse(call: Call<List<HomeroomAttendanceItem>>, response: Response<List<HomeroomAttendanceItem>>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    processApiData(data)
                } else {
                    Toast.makeText(this@RiwayatKehadiranKelasPengurusActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<HomeroomAttendanceItem>>, t: Throwable) {
                Toast.makeText(this@RiwayatKehadiranKelasPengurusActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun processApiData(items: List<HomeroomAttendanceItem>) {
        riwayatList.clear()
        subjectDetailsMap.clear()
        
        totalHadir = 0
        totalIzin = 0
        totalSakit = 0
        totalAlpha = 0

        // Group by Schedule ID (or Subject Name + Start Time if ID missing)
        // Ideally Schedule ID is unique for a specific class session
        val grouped = items.groupBy { it.schedule?.id ?: -1 }

        grouped.forEach { (scheduleId, attendanceItems) ->
            if (attendanceItems.isEmpty()) return@forEach
            
            val firstItem = attendanceItems[0]
            val subjectName = firstItem.schedule?.subjectInfo?.name ?: "Mata Pelajaran"
            val teacherName = firstItem.schedule?.teacher?.user?.name ?: "Guru"
            val timeSlot = "${firstItem.schedule?.startTime ?: ""} - ${firstItem.schedule?.endTime ?: ""}"
            
            var hadir = 0
            var izin = 0
            var sakit = 0
            var alpha = 0

            val detailsList = mutableListOf<RiwayatAbsenItem>()
            var counter = 1

            attendanceItems.forEach { item ->
                val statusType = when(item.status) {
                    "present" -> "hadir"
                    "sick" -> "sakit"
                    "excused" -> "izin"
                    "absent" -> "alpha"
                    else -> "hadir"
                }

                when (statusType) {
                    "hadir" -> hadir++
                    "sakit" -> sakit++
                    "izin" -> izin++
                    "alpha" -> alpha++
                }

                detailsList.add(RiwayatAbsenItem(
                    id = counter++,
                    namaSiswa = item.student?.user?.name ?: "Siswa",
                    jurusan = "-", // Not essential inside popup
                    tanggal = item.createdAt, // Or format strictly
                    waktu = item.schedule?.startTime ?: "-",
                    status = statusType
                ))
            }
            
            totalHadir += hadir
            totalIzin += izin
            totalSakit += sakit
            totalAlpha += alpha

            // Summary string
            val parts = mutableListOf<String>()
            if (hadir > 0) parts.add("$hadir siswa hadir")
            if (izin > 0) parts.add("$izin izin")
            if (sakit > 0) parts.add("$sakit sakit")
            if (alpha > 0) parts.add("$alpha alpha")
            val summary = parts.joinToString(", ")

            // Use subjectName as key for popup if unique enough for today, otherwise Schedule ID map
            // We use subjectName for display, but let's use a unique key for the map
            val key = "$subjectName ($timeSlot)" // Combined key
            
            riwayatList.add(RiwayatPengurusItem(
                id = scheduleId.toString(),
                mataPelajaran = subjectName, 
                keterangan = summary, // e.g. "30 siswa hadir, 2 sakit"
                status = "hadir", // Just visual badge
                uniqueKey = key 
            ))
            
            subjectDetailsMap[key] = detailsList
        }

        txtHadirCount.text = totalHadir.toString()
        txtIzinCount.text = totalIzin.toString()
        txtSakitCount.text = totalSakit.toString()
        txtAlphaCount.text = totalAlpha.toString()
        txtJumlah.text = "Total Mata Pelajaran: ${riwayatList.size}"

        adapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        adapter = RiwayatPengurusAdapter(riwayatList, object : OnItemClickListener {
            override fun onItemClick(uniqueKey: String, subjectName: String) {
                showDetailPopup(uniqueKey, subjectName)
            }
        })
        recyclerRiwayat.layoutManager = LinearLayoutManager(this)
        recyclerRiwayat.adapter = adapter
    }

    private fun showDetailPopup(uniqueKey: String, subjectName: String) {
        val dialog = Dialog(this)
        val dialogView = layoutInflater.inflate(R.layout.pop_up_riwayat_kehadiran_siswa, null)

        val tvJudul = dialogView.findViewById<TextView>(R.id.tv_judul_popup)
        tvJudul.text = "Absensi $subjectName"

        val rvSiswa = dialogView.findViewById<RecyclerView>(R.id.rv_riwayat_siswa)
        rvSiswa.layoutManager = LinearLayoutManager(this)

        val details = subjectDetailsMap[uniqueKey] ?: emptyList()
        val popupAdapter = RiwayatAbsenAdapter(details)
        rvSiswa.adapter = popupAdapter

        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )
    }

    private fun setupButtonListeners() {
        findViewById<View>(R.id.btnHome).setOnClickListener { navigateToDashboard() }
        findViewById<View>(R.id.btnAssignment).setOnClickListener { 
             Toast.makeText(this, "Sudah di halaman ini", Toast.LENGTH_SHORT).show() 
        }
        findViewById<View>(R.id.text_navigasi).setOnClickListener { navigateToJadwalHarian() }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToDashboard()
            }
        })
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardSiswaActivity::class.java).apply {
            putExtra("IS_PENGURUS", isPengurus)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToJadwalHarian() {
        val intent = Intent(this, JadwalHarianSiswaActivity::class.java).apply {
            putExtra("IS_PENGURUS", true)
        }
        startActivity(intent)
    }

    // Data Classes
    data class RiwayatPengurusItem(
        val id: String,
        val mataPelajaran: String,
        val keterangan: String,
        val status: String,
        val uniqueKey: String 
    )

    interface OnItemClickListener {
        fun onItemClick(uniqueKey: String, subjectName: String)
    }

    private inner class RiwayatPengurusAdapter(
        private val list: List<RiwayatPengurusItem>,
        private val listener: OnItemClickListener
    ) : RecyclerView.Adapter<RiwayatPengurusAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtSession: TextView = itemView.findViewById(R.id.Session)
            val txtMataPelajaran: TextView = itemView.findViewById(R.id.MataPelajaran)
            val txtKeterangan: TextView = itemView.findViewById(R.id.TextKeteranganAbsen)
            val btnTampilkan: ImageButton = itemView.findViewById(R.id.BadgeKehadiran)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_kehadiran_kelas, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.txtSession.text = "Sesi ${position + 1}" // Or use ID
            holder.txtMataPelajaran.text = item.mataPelajaran
            holder.txtKeterangan.text = item.keterangan
            
            holder.btnTampilkan.setOnClickListener {
                listener.onItemClick(item.uniqueKey, item.mataPelajaran)
            }
        }

        override fun getItemCount() = list.size
    }
}