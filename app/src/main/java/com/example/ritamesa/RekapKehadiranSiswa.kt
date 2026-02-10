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
import com.example.ritamesa.data.model.StudentItem
import com.example.ritamesa.data.model.StudentListResponse
import com.example.ritamesa.data.model.StudentAttendanceAdminResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RekapKehadiranSiswa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var rekapAdapter: RekapSiswaAdapter
    private lateinit var editTextSearch: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var apiService: ApiService
    private var siswaList = mutableListOf<StudentItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rekap_kehadiran_siswa)

        apiService = ApiClient.getClient(this).create(ApiService::class.java)

        initView()
        setupRecyclerView()
        setupActions()
        setupBottomNavigation()
        setupSearch()
        
        fetchStudents()
    }

    private fun fetchStudents(query: String? = null) {
        val pd = android.app.ProgressDialog(this)
        if (query == null) {
            pd.setMessage("Memuat data siswa...")
            pd.show()
        }

        apiService.getStudents(search = query, page = 1).enqueue(object : Callback<StudentListResponse> {
            override fun onResponse(call: Call<StudentListResponse>, response: Response<StudentListResponse>) {
                if (query == null) pd.dismiss()
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    siswaList.clear()
                    siswaList.addAll(data)
                    rekapAdapter.updateData(siswaList)
                } else {
                    Toast.makeText(this@RekapKehadiranSiswa, "Gagal memuat siswa", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<StudentListResponse>, t: Throwable) {
                if (query == null) pd.dismiss()
                Toast.makeText(this@RekapKehadiranSiswa, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun fetchStudentHistory(studentId: Int, callback: (List<com.example.ritamesa.data.model.StudentAttendanceItem>) -> Unit) {
         val pd = android.app.ProgressDialog(this)
         pd.setMessage("Memuat riwayat kehadiran...")
         pd.show()
         
         apiService.getStudentAttendanceAdmin(studentId = studentId).enqueue(object : Callback<List<StudentAttendanceAdminResponse>> {
             override fun onResponse(call: Call<List<StudentAttendanceAdminResponse>>, response: Response<List<StudentAttendanceAdminResponse>>) {
                 pd.dismiss()
                 if (response.isSuccessful) {
                     val list = response.body()
                     if (!list.isNullOrEmpty()) {
                         callback(list[0].items)
                     } else {
                         callback(emptyList())
                         Toast.makeText(this@RekapKehadiranSiswa, "Belum ada data kehadiran", Toast.LENGTH_SHORT).show()
                     }
                 } else {
                     Toast.makeText(this@RekapKehadiranSiswa, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
                 }
             }
             override fun onFailure(call: Call<List<StudentAttendanceAdminResponse>>, t: Throwable) {
                 pd.dismiss()
                 Toast.makeText(this@RekapKehadiranSiswa, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
             }
         })
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvKehadiran)
        editTextSearch = findViewById(R.id.editTextText5)
        btnBack = findViewById(R.id.btnBack)
        btnMenu = findViewById(R.id.buttonmenu)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        rekapAdapter = RekapSiswaAdapter(
            siswaList,
            onLihatClickListener = { siswa ->
                showPopupDetailSiswa(siswa)
            }
        )
        recyclerView.adapter = rekapAdapter
    }

    private fun showPopupDetailSiswa(siswa: StudentItem) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_siswa_detail, null)

        // Set data siswa
        popupView.findViewById<TextView>(R.id.tvPopupNama).text = siswa.name
        popupView.findViewById<TextView>(R.id.tvPopupNisn).text = siswa.nisn ?: "-"
        popupView.findViewById<TextView>(R.id.tvPopupKelas).text = siswa.className

        val container = popupView.findViewById<LinearLayout>(R.id.containerKehadiran)
        
        // Fetch and show history
        fetchStudentHistory(siswa.id) { history ->
            setupDataKehadiranSiswa(container, history)
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

    private fun setupDataKehadiranSiswa(container: LinearLayout, history: List<com.example.ritamesa.data.model.StudentAttendanceItem>) {
        container.removeAllViews()

        history.forEach { kehadiran ->
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_kehadiran_popup, container, false)

            itemView.findViewById<TextView>(R.id.tvTanggal).text = kehadiran.date
            itemView.findViewById<TextView>(R.id.tvMapelKelas).text = kehadiran.schedule?.subjectInfo?.name ?: "-"
            itemView.findViewById<TextView>(R.id.tvJam).text = "${kehadiran.schedule?.startTime ?: "-"} - ${kehadiran.schedule?.endTime ?: "-"}"
            itemView.findViewById<TextView>(R.id.tvStatus).text = kehadiran.status
            itemView.findViewById<TextView>(R.id.tvKeterangan).text = "-" // Reason not in basic item, check model

            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            when (kehadiran.status?.lowercase()) {
                "present" -> {
                    tvStatus.text = "Hadir"
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                }
                "sick" -> {
                     tvStatus.text = "Sakit"
                     tvStatus.setTextColor(Color.parseColor("#FF9800"))
                }
                "excused" -> {
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
                // filterData(s.toString().trim()) // Using API search instead
            }

            override fun afterTextChanged(s: Editable?) {
                 fetchStudents(s.toString())
            }
        })

        // BUTTON SEARCH CLEAR
        findViewById<ImageButton>(R.id.imageButton17).setOnClickListener {
            editTextSearch.text.clear()
            editTextSearch.requestFocus()
            fetchStudents(null)
            Toast.makeText(this, "Menampilkan semua data siswa", Toast.LENGTH_SHORT).show()
        }
    }

    // private fun filterData(query: String) { ... } // Removed local filter

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_rekap_switch, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_guru -> {
                    // Pindah ke halaman rekap guru
                    val intent = Intent(this, RekapKehadiranGuru::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.menu_siswa -> {
                    // Sudah di halaman siswa
                    Toast.makeText(this, "Sudah di halaman Siswa", Toast.LENGTH_SHORT).show()
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

        // Contacts (Active) - ke Data Rekap (halaman ini)
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            Toast.makeText(this, "Sudah di halaman Data Rekap", Toast.LENGTH_SHORT).show()
        }

        // Bar Chart - ke Statistik
        findViewById<ImageButton>(R.id.imageButton5).setOnClickListener {
            try {
                val intent = Intent(this, StatistikKehadiran::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        // Notifications - ke Notifikasi
        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            try {
                val intent = Intent(this, NotifikasiSemua::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Adapter untuk RecyclerView siswa
    class RekapSiswaAdapter(
        private var dataList: List<StudentItem>,
        private val onLihatClickListener: (StudentItem) -> Unit
    ) : RecyclerView.Adapter<RekapSiswaAdapter.SiswaViewHolder>() {

        inner class SiswaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
            val tvNama: TextView = itemView.findViewById(R.id.tvNama)
            val tvNisn: TextView = itemView.findViewById(R.id.tvTelepon)
            val tvKelasJurusan: TextView = itemView.findViewById(R.id.tvMataPelajaran)
            val btnLihat: ImageButton = itemView.findViewById(R.id.btnLihat)

            init {
                btnLihat.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onLihatClickListener(dataList[position])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lihat_rekap_guru, parent, false)
            return SiswaViewHolder(view)
        }

        override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
            val siswa = dataList[position]
            holder.tvNomor.text = (position + 1).toString()
            holder.tvNama.text = siswa.name
            holder.tvNisn.text = siswa.nisn ?: "-"
            holder.tvKelasJurusan.text = siswa.className
        }

        override fun getItemCount(): Int = dataList.size

        fun updateData(newData: List<StudentItem>) {
            dataList = newData
            notifyDataSetChanged()
        }
    }
}
