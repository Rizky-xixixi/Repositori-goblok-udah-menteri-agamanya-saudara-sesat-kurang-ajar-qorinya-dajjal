package com.example.ritamesa

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent

class DataRekapKehadiranGuru : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextSearch: EditText
    private lateinit var adapter: GuruAdapterWaka
    private var allGuruList = mutableListOf<TeacherItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_rekapkehadiran_guru)

        recyclerView = findViewById(R.id.rvSiswa)
        editTextSearch = findViewById(R.id.editTextText)

        setupAllNavigation()
        setupRecyclerView()
        setupSearch()
        loadDataFromApi()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GuruAdapterWaka(emptyList()) { guru ->
            showPopupDetailGuru(guru)
        }
        recyclerView.adapter = adapter
    }

    private fun loadDataFromApi() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.getTeachers().enqueue(object : Callback<TeacherListResponse> {
            override fun onResponse(call: Call<TeacherListResponse>, response: Response<TeacherListResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    allGuruList = response.body()!!.data.toMutableList()
                    adapter.updateData(allGuruList)
                } else {
                    Toast.makeText(this@DataRekapKehadiranGuru, "Gagal mengambil data guru", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TeacherListResponse>, t: Throwable) {
                Toast.makeText(this@DataRekapKehadiranGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showPopupDetailGuru(guru: TeacherItem) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_guru_detail, null)

        popupView.findViewById<TextView>(R.id.tvPopupNama).text = guru.name
        popupView.findViewById<TextView>(R.id.tvPopupNip).text = guru.nip ?: "-"
        popupView.findViewById<TextView>(R.id.tvPopupMapel).text = guru.mapel

        val container = popupView.findViewById<LinearLayout>(R.id.containerKehadiran)
        val progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        container.addView(progressBar)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 20f
        popupWindow.isOutsideTouchable = true

        popupView.findViewById<Button>(R.id.btnTutupPopup).setOnClickListener {
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(window.decorView.rootView, android.view.Gravity.CENTER, 0, 0)

        // Fetch History
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.getTeacherAttendanceAdmin(guru.id).enqueue(object : Callback<List<TeachingAttendanceItem>> {
            override fun onResponse(call: Call<List<TeachingAttendanceItem>>, response: Response<List<TeachingAttendanceItem>>) {
                container.removeView(progressBar)
                if (response.isSuccessful && response.body() != null) {
                    val history = response.body()!!
                    if (history.isEmpty()) {
                        val tvEmpty = TextView(this@DataRekapKehadiranGuru).apply {
                            text = "Tidak ada riwayat kehadiran"
                            gravity = android.view.Gravity.CENTER
                            setPadding(0, 20, 0, 20)
                            setTextColor(Color.GRAY)
                        }
                        container.addView(tvEmpty)
                    } else {
                        history.forEach { item ->
                            val itemView = LayoutInflater.from(this@DataRekapKehadiranGuru)
                                .inflate(R.layout.item_kehadiran_popup, container, false)

                            itemView.findViewById<TextView>(R.id.tvTanggal).text = item.date
                            itemView.findViewById<TextView>(R.id.tvMapelKelas).text = "${item.schedule?.subjectInfo?.name ?: "Pelajaran"} / ${item.schedule?.classInfo?.name ?: "-"}"
                            itemView.findViewById<TextView>(R.id.tvJam).text = "-" 
                            itemView.findViewById<TextView>(R.id.tvStatus).text = item.status
                            itemView.findViewById<TextView>(R.id.tvKeterangan).text = "-"

                            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
                            when (item.status.lowercase()) {
                                "hadir" -> { tvStatus.text = "Hadir"; tvStatus.setTextColor(Color.parseColor("#4CAF50")) }
                                "late" -> { tvStatus.text = "Terlambat"; tvStatus.setTextColor(Color.parseColor("#FF9800")) }
                                "sick" -> { tvStatus.text = "Sakit"; tvStatus.setTextColor(Color.parseColor("#FF9800")) }
                                "excused" -> { tvStatus.text = "Izin"; tvStatus.setTextColor(Color.parseColor("#2196F3")) }
                                "absent" -> { tvStatus.text = "Alpha"; tvStatus.setTextColor(Color.parseColor("#F44336")) }
                            }
                            container.addView(itemView)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<List<TeachingAttendanceItem>>, t: Throwable) {
                container.removeView(progressBar)
                Toast.makeText(this@DataRekapKehadiranGuru, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearch() {
        editTextSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString().trim())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<ImageButton>(R.id.imageButton12).setOnClickListener {
            editTextSearch.text.clear()
            loadDataFromApi()
        }
    }

    private fun filterData(query: String) {
        val filteredList = if (query.isEmpty()) {
            allGuruList
        } else {
            val lowercaseQuery = query.lowercase()
            allGuruList.filter { guru ->
                guru.name.lowercase().contains(lowercaseQuery) ||
                        (guru.nip ?: "").lowercase().contains(lowercaseQuery) ||
                        (guru.mapel ?: "").lowercase().contains(lowercaseQuery)
            }
        }
        adapter.updateData(filteredList)
    }

    private fun setupAllNavigation() {
        setupMoreVertButton()
        setupHomeButton()
        setupDataRekapButton()
        setupJadwalButton()
        setupStatistikButton()
        setupNotifikasiButton()
        setupBackButton()
    }

    private fun setupBackButton() {
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { navigateToDashboardWaka() }
    }

    private fun setupMoreVertButton() {
        findViewById<ImageButton>(R.id.imageButton20).setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.menu_data_rekap, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_guru -> {
                        Toast.makeText(this, "Sudah di halaman Guru", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_siswa -> {
                        val intent = Intent(this, DataRekapkehadiranSiswa::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun setupHomeButton() {
        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener { navigateToDashboardWaka() }
    }

    private fun setupDataRekapButton() {
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            Toast.makeText(this, "Sudah di halaman Data Rekap", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupJadwalButton() {
        findViewById<ImageButton>(R.id.imageButton4).setOnClickListener {
            try {
                val intent = Intent(this, JadwalPembelajaranGuru::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupStatistikButton() {
        findViewById<ImageButton>(R.id.imageButton5).setOnClickListener {
            try {
                val intent = Intent(this, StatistikWakaa::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNotifikasiButton() {
        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            try {
                val intent = Intent(this, NotifikasiSemuaWaka::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToDashboardWaka() {
        val intent = Intent(this, DashboardWaka::class.java)
        startActivity(intent)
        finish()
    }

    inner class GuruAdapterWaka(
        private var list: List<TeacherItem>,
        private val onClick: (TeacherItem) -> Unit
    ) : RecyclerView.Adapter<GuruAdapterWaka.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNama: TextView = view.findViewById(R.id.tvNamaGuru) // Assuming ID from layout
            val tvMapel: TextView = view.findViewById(R.id.tvMapel)
        }
        fun updateData(newList: List<TeacherItem>) {
            list = newList
            notifyDataSetChanged()
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rekap_guru, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvNama.text = item.name
            holder.tvMapel.text = item.mapel
            holder.itemView.setOnClickListener { onClick(item) }
        }
        override fun getItemCount() = list.size
    }
}