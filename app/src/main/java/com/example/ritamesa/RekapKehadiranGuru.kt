package com.example.ritamesa

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.TeacherItem
import com.example.ritamesa.data.model.TeacherListResponse
import com.example.ritamesa.data.model.TeachingAttendanceItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RekapKehadiranGuru : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GuruAdapter
    private lateinit var editTextSearch: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var apiService: ApiService
    private var guruList = mutableListOf<TeacherItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rekap_kehadiran_guru)

        apiService = ApiClient.getClient(this).create(ApiService::class.java)

        initView()
        setupRecyclerView()
        setupActions()
        setupBottomNavigation()
        setupSearch()
        
        fetchTeachers()
    }

    private fun fetchTeachers() {
        val pd = android.app.ProgressDialog(this)
        pd.setMessage("Memuat data guru...")
        pd.show()

        // Page 1 for now, or implement pagination
        apiService.getTeachers().enqueue(object : Callback<TeacherListResponse> {
            override fun onResponse(call: Call<TeacherListResponse>, response: Response<TeacherListResponse>) {
                pd.dismiss()
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    guruList.clear()
                    guruList.addAll(data)
                    adapter.updateData(guruList)
                } else {
                    Toast.makeText(this@RekapKehadiranGuru, "Gagal memuat guru", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TeacherListResponse>, t: Throwable) {
                pd.dismiss()
                Toast.makeText(this@RekapKehadiranGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun fetchTeacherHistory(teacherId: Int, callback: (List<TeachingAttendanceItem>) -> Unit) {
         val pd = android.app.ProgressDialog(this)
         pd.setMessage("Memuat riwayat kehadiran...")
         pd.show()
         
         apiService.getTeacherAttendanceAdmin(id = teacherId).enqueue(object : Callback<List<TeachingAttendanceItem>> {
             override fun onResponse(call: Call<List<TeachingAttendanceItem>>, response: Response<List<TeachingAttendanceItem>>) {
                 pd.dismiss()
                 if (response.isSuccessful) {
                     val list = response.body() ?: emptyList()
                     if (list.isEmpty()) {
                         Toast.makeText(this@RekapKehadiranGuru, "Belum ada data kehadiran", Toast.LENGTH_SHORT).show()
                     }
                     callback(list)
                 } else {
                     Toast.makeText(this@RekapKehadiranGuru, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
                 }
             }
             override fun onFailure(call: Call<List<TeachingAttendanceItem>>, t: Throwable) {
                 pd.dismiss()
                 Toast.makeText(this@RekapKehadiranGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
             }
         })
    }

    private fun initView() {
        recyclerView = findViewById(R.id.recyclerViewGuru)
        editTextSearch = findViewById(R.id.editTextText5)
        btnBack = findViewById(R.id.btnBack)
        btnMenu = findViewById(R.id.buttonmenu)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = GuruAdapter(guruList) { guru ->
            showPopupDetailGuru(guru)
        }
        recyclerView.adapter = adapter
    }

    private fun showPopupDetailGuru(guru: TeacherItem) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_guru_detail, null)

        // Set data guru
        popupView.findViewById<TextView>(R.id.tvPopupNama).text = guru.name
        popupView.findViewById<TextView>(R.id.tvPopupNip).text = guru.nip ?: "-"
        popupView.findViewById<TextView>(R.id.tvPopupMapel).text = guru.mapel

        val container = popupView.findViewById<LinearLayout>(R.id.containerKehadiran)
        
        fetchTeacherHistory(guru.id) { history ->
            setupDataKehadiranGuru(container, history)
        }

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set background transparan untuk efek blur
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 20f
        popupWindow.isOutsideTouchable = true

        // Set opacity untuk background popup
        val popupContainer = popupView.findViewById<View>(R.id.popupContainer)
        popupContainer.alpha = 0.95f  // Sedikit transparan untuk efek blur

        popupView.findViewById<Button>(R.id.btnTutupPopup).setOnClickListener {
            popupWindow.dismiss()
        }

        // Tambahkan background semi-transparan untuk area di luar popup
        val backgroundView = View(this)
        backgroundView.setBackgroundColor(Color.parseColor("#80000000")) // Hitam semi-transparan
        val rootView = window.decorView.rootView as ViewGroup
        rootView.addView(backgroundView)

        popupWindow.showAtLocation(window.decorView.rootView, android.view.Gravity.CENTER, 0, 0)

        // Hapus background view saat popup ditutup
        popupWindow.setOnDismissListener {
            rootView.removeView(backgroundView)
        }
    }

    private fun setupDataKehadiranGuru(container: LinearLayout, history: List<TeachingAttendanceItem>) {
        container.removeAllViews()

        history.forEach { kehadiran ->
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_kehadiran_popup, container, false)

            itemView.findViewById<TextView>(R.id.tvTanggal).text = kehadiran.date
            // Check if schedule is null
            val mapel = kehadiran.schedule?.subjectInfo?.name ?: "-"
            val kelas = kehadiran.schedule?.classInfo?.name ?: "-"
            itemView.findViewById<TextView>(R.id.tvMapelKelas).text = "$mapel / $kelas"
            itemView.findViewById<TextView>(R.id.tvJam).text = "${kehadiran.schedule?.startTime ?: "-"} - ${kehadiran.schedule?.endTime ?: "-"}"
            itemView.findViewById<TextView>(R.id.tvStatus).text = kehadiran.status
            // itemView.findViewById<TextView>(R.id.tvKeterangan).text = kehadiran.keterangan // No reason in basic model, maybe in future

            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            when (kehadiran.status.lowercase()) {
                "present" -> {
                    tvStatus.text = "Hadir"
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                }
                "sakit" -> {
                     tvStatus.text = "Sakit"
                     tvStatus.setTextColor(Color.parseColor("#FF9800"))
                }
                "izin" -> {
                     tvStatus.text = "Izin"
                     tvStatus.setTextColor(Color.parseColor("#2196F3"))
                }
                "absent" -> {
                     tvStatus.text = "Alpha"
                     tvStatus.setTextColor(Color.parseColor("#F44336"))
                }
                 "late" -> {
                     tvStatus.text = "Terlambat"
                     tvStatus.setTextColor(Color.parseColor("#FF9800"))
                }
            }

            container.addView(itemView)
        }
    }

    private fun setupActions() {
        // BUTTON BACK
        btnBack.setOnClickListener {
            finish()
        }

        // BUTTON MENU (More Vert)
        btnMenu.setOnClickListener {
            showPopupMenu(it)
        }
    }

    private fun setupSearch() {
        // SEARCH TEXT LISTENER
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString().trim())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // BUTTON SEARCH CLEAR
        findViewById<ImageButton>(R.id.imageButton17).setOnClickListener {
            editTextSearch.text.clear()
            editTextSearch.requestFocus()
            adapter.filter("")
            Toast.makeText(this, "Menampilkan semua data guru", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSearch(query: String) {
        adapter.filter(query)

        if (query.isNotEmpty() && adapter.itemCount == 0) {
            Toast.makeText(this, "Tidak ditemukan guru dengan kata kunci '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_rekap_switch, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_guru -> {
                    // Sudah di halaman guru
                    Toast.makeText(this, "Sudah di halaman Guru", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_siswa -> {
                    // Pindah ke halaman rekap siswa
                    val intent = Intent(this, RekapKehadiranSiswa::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun setupBottomNavigation() {
        // Home - ke Dashboard
        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }

        // Contacts - ke Rekap Kehadiran (Siswa)
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            val intent = Intent(this, RekapKehadiranSiswa::class.java)
            startActivity(intent)
            finish()
        }

        // Bar Chart - ke Statistik
        findViewById<ImageButton>(R.id.imageButton5).setOnClickListener {
            val intent = Intent(this, StatistikKehadiran::class.java)
            startActivity(intent)
            finish()
        }

        // Notifications - ke Notifikasi
        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            val intent = Intent(this, NotifikasiSemua::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Adapter untuk RecyclerView dengan filter
    class GuruAdapter(
        private var guruList: List<TeacherItem>,
        private val onLihatClickListener: (TeacherItem) -> Unit
    ) : RecyclerView.Adapter<GuruAdapter.GuruViewHolder>() {

        private var filteredList: List<TeacherItem> = guruList

        inner class GuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
            val tvNama: TextView = itemView.findViewById(R.id.tvNama)
            val tvTelepon: TextView = itemView.findViewById(R.id.tvTelepon)
            val tvMataPelajaran: TextView = itemView.findViewById(R.id.tvMataPelajaran)
            val btnLihat: ImageButton = itemView.findViewById(R.id.btnLihat)

            init {
                btnLihat.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onLihatClickListener(filteredList[position])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lihat_rekap_guru, parent, false)
            return GuruViewHolder(view)
        }

        override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
            val guru = filteredList[position]
            holder.tvNomor.text = (position + 1).toString()
            holder.tvNama.text = guru.name
            holder.tvTelepon.text = guru.nip ?: "-"
            holder.tvMataPelajaran.text = guru.mapel
        }

        override fun getItemCount(): Int = filteredList.size

        // Fungsi untuk filter data
        fun filter(query: String) {
            filteredList = if (query.isEmpty()) {
                guruList
            } else {
                val lowercaseQuery = query.lowercase()
                guruList.filter {
                    it.name.lowercase().contains(lowercaseQuery) ||
                            (it.nip ?: "").lowercase().contains(lowercaseQuery) ||
                            it.mapel.lowercase().contains(lowercaseQuery)
                }
            }
            notifyDataSetChanged()
        }
        
        fun updateData(newData: List<TeacherItem>) {
            guruList = newData
            filteredList = newData
            notifyDataSetChanged()
        }
    }
}
