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

class DataRekapkehadiranSiswa : AppCompatActivity() {

    private lateinit var rvSiswa: RecyclerView
    private lateinit var editTextSearch: EditText
    private lateinit var siswaAdapter: SiswaAdapterWaka
    private var allSiswaList = mutableListOf<StudentItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_rekapkehadiran_siswa)

        rvSiswa = findViewById(R.id.rvKehadiran)
        editTextSearch = findViewById(R.id.editTextText5)

        setupNavigation()
        setupRecyclerView()
        setupSearch()
        loadDataFromApi()
    }

    private fun setupRecyclerView() {
        rvSiswa.layoutManager = LinearLayoutManager(this)
        siswaAdapter = SiswaAdapterWaka(emptyList()) { siswa ->
            showPopupDetailSiswa(siswa)
        }
        rvSiswa.adapter = siswaAdapter
    }

    private fun loadDataFromApi() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.getStudents().enqueue(object : Callback<StudentListResponse> {
            override fun onResponse(call: Call<StudentListResponse>, response: Response<StudentListResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    allSiswaList = response.body()!!.data.toMutableList()
                    siswaAdapter.updateData(allSiswaList)
                } else {
                    Toast.makeText(this@DataRekapkehadiranSiswa, "Gagal mengambil data siswa", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<StudentListResponse>, t: Throwable) {
                Toast.makeText(this@DataRekapkehadiranSiswa, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showPopupDetailSiswa(siswa: StudentItem) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_siswa_detail, null)

        popupView.findViewById<TextView>(R.id.tvPopupNama).text = siswa.name
        popupView.findViewById<TextView>(R.id.tvPopupNisn).text = siswa.nisn ?: "-"
        popupView.findViewById<TextView>(R.id.tvPopupKelas).text = siswa.className

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
        apiService.getStudentAttendanceAdmin(siswa.id).enqueue(object : Callback<List<StudentAttendanceAdminResponse>> {
            override fun onResponse(call: Call<List<StudentAttendanceAdminResponse>>, response: Response<List<StudentAttendanceAdminResponse>>) {
                container.removeView(progressBar)
                if (response.isSuccessful && response.body() != null) {
                    val history = response.body()!!
                    if (history.isEmpty()) {
                        val tvEmpty = TextView(this@DataRekapkehadiranSiswa).apply {
                            text = "Tidak ada riwayat kehadiran"
                            gravity = android.view.Gravity.CENTER
                            setPadding(0, 20, 0, 20)
                            setTextColor(Color.GRAY)
                        }
                        container.addView(tvEmpty)
                    } else {
                        history.forEach { item ->
                            val itemView = LayoutInflater.from(this@DataRekapkehadiranSiswa)
                                .inflate(R.layout.item_kehadiran_popup, container, false)

                            itemView.findViewById<TextView>(R.id.tvTanggal).text = item.date
                            itemView.findViewById<TextView>(R.id.tvMapelKelas).text = item.status.replaceFirstChar { it.uppercase() }
                            itemView.findViewById<TextView>(R.id.tvJam).text = "-" 
                            itemView.findViewById<TextView>(R.id.tvStatus).text = item.status
                            itemView.findViewById<TextView>(R.id.tvKeterangan).text = item.note ?: "-"

                            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
                            when (item.status.lowercase()) {
                                "present" -> { tvStatus.text = "Hadir"; tvStatus.setTextColor(Color.parseColor("#4CAF50")) }
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
            override fun onFailure(call: Call<List<StudentAttendanceAdminResponse>>, t: Throwable) {
                container.removeView(progressBar)
                Toast.makeText(this@DataRekapkehadiranSiswa, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
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

        findViewById<ImageButton>(R.id.imageButton17).setOnClickListener {
            editTextSearch.text.clear()
            loadDataFromApi()
        }
    }

    private fun filterData(query: String) {
        val filteredList = if (query.isEmpty()) {
            allSiswaList
        } else {
            val lowercaseQuery = query.lowercase()
            allSiswaList.filter { siswa ->
                siswa.name.lowercase().contains(lowercaseQuery) ||
                        (siswa.nisn ?: "").lowercase().contains(lowercaseQuery) ||
                        siswa.className.lowercase().contains(lowercaseQuery)
            }
        }
        siswaAdapter.updateData(filteredList)
    }

    private fun setupNavigation() {
        setupMoreVertButton()
        setupHomeButton()
        setupDataRekapButton()
        setupJadwalButton()
        setupStatistikButton()
        setupNotifikasiButton()
    }

    private fun setupMoreVertButton() {
        findViewById<ImageButton>(R.id.imageButton5).setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.menu_data_rekap, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_guru -> {
                        val intent = Intent(this, DataRekapKehadiranGuru::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.menu_siswa -> {
                        Toast.makeText(this, "Sudah di halaman Siswa", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun setupHomeButton() {
        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            val intent = Intent(this, DashboardWaka::class.java)
            startActivity(intent)
            finish()
        }
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
        findViewById<ImageButton>(R.id.imageButton55).setOnClickListener {
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

    inner class SiswaAdapterWaka(
        private var list: List<StudentItem>,
        private val onClick: (StudentItem) -> Unit
    ) : RecyclerView.Adapter<SiswaAdapterWaka.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNama: TextView = view.findViewById(R.id.textView58)
            val tvKelas: TextView = view.findViewById(R.id.textView59)
        }
        fun updateData(newList: List<StudentItem>) {
            list = newList
            notifyDataSetChanged()
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rekap_siswa, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvNama.text = item.name
            holder.tvKelas.text = item.className
            holder.itemView.setOnClickListener { onClick(item) }
        }
        override fun getItemCount() = list.size
    }
}