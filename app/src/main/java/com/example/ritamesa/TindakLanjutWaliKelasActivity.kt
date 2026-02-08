package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.StudentFollowUpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TindakLanjutWaliKelasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearchKelas: EditText
    private lateinit var adapter: SiswaTindakLanjutAdapter

    private lateinit var btnHome: ImageButton
    private lateinit var btnCalendar: ImageButton
    private lateinit var btnChart: ImageButton
    private lateinit var btnNotif: ImageButton

    private val allSiswaData = mutableListOf<Map<String, Any>>()
    private val filteredSiswaData = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.tindak_lanjut_guru) // Using same layout

            initViews()
            setupFooterNavigation()
            setupRecyclerView()
            setupSearchFilter()

            loadDataFromApi()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvSiswaAbsensi)
        etSearchKelas = findViewById(R.id.etSearchKelas)

        btnHome = findViewById(R.id.btnHome)
        btnCalendar = findViewById(R.id.btnCalendar)
        btnChart = findViewById(R.id.btnChart)
        btnNotif = findViewById(R.id.btnNotif)
    }

    private fun setupFooterNavigation() {
        btnHome.setOnClickListener {
            startActivity(Intent(this, DashboardWaliKelasActivity::class.java))
        }

        btnCalendar.setOnClickListener {
            startActivity(Intent(this, RiwayatKehadiranKelasActivity::class.java))
        }

        btnChart.setOnClickListener {
             refreshData()
        }

        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotifikasiWaliKelasActivity::class.java))
        }
    }

    private fun refreshData() {
        loadDataFromApi()
        Toast.makeText(this, "Data Tindak Lanjut direfresh", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        adapter = SiswaTindakLanjutAdapter(filteredSiswaData)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchFilter() {
        etSearchKelas.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterData(s.toString())
            }
        })
    }

    private fun filterData(query: String) {
        filteredSiswaData.clear()

        if (query.isEmpty()) {
            filteredSiswaData.addAll(allSiswaData)
        } else {
            val lowerQuery = query.lowercase()
            filteredSiswaData.addAll(allSiswaData.filter {
                val nama = it["nama"] as String
                val kelasJurusan = it["kelasJurusan"] as String
                nama.lowercase().contains(lowerQuery) || kelasJurusan.lowercase().contains(lowerQuery)
            })
        }

        adapter.notifyDataSetChanged()
    }

    private fun loadDataFromApi() {
        // Use getStudentsFollowUp (same specialized endpoint for all teachers)
        // If Wali Kelas needs restricted view, backend should handle it or we assume they want to see all students they teach?
        // Typically Wali Kelas Dashboard links to "Tindak Lanjut" which for them implies their class.
        // But getStudentsFollowUp returns students from *taught* classes. 
        // If they teach their class, it works.
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.getStudentsFollowUp().enqueue(object : Callback<StudentFollowUpResponse> {
            override fun onResponse(
                call: Call<StudentFollowUpResponse>,
                response: Response<StudentFollowUpResponse>
            ) {
                if (response.isSuccessful) {
                    val apiData = response.body()?.data ?: emptyList()
                    processApiData(apiData)
                } else {
                    Toast.makeText(this@TindakLanjutWaliKelasActivity, "Gagal memuat: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StudentFollowUpResponse>, t: Throwable) {
                Toast.makeText(this@TindakLanjutWaliKelasActivity, "Error koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun processApiData(items: List<com.example.ritamesa.data.model.StudentFollowUpItem>) {
        allSiswaData.clear()
        
        items.forEach { item ->
            // Only add if NOT "Aman"
            if (item.badge.type != "success") {
                 val badgeDrawable = when(item.badge.type) {
                     "danger" -> R.drawable.box_danger
                     "warning" -> R.drawable.box_warning
                     else -> R.drawable.box_success
                 }

                 allSiswaData.add(mapOf(
                     "id" to item.id,
                     "nama" to item.name,
                     "kelasJurusan" to item.className,
                     "alphaCount" to item.attendanceSummary.absent,
                     "izinCount" to item.attendanceSummary.excused, 
                     "sakitCount" to item.attendanceSummary.sick,
                     
                     "badgeDrawable" to badgeDrawable,
                     "badgeText" to item.badge.label,
                     "showBadge" to true,
                     
                     "severityScore" to item.severityScore
                 ))
            }
        }

        filterData(etSearchKelas.text.toString())
        Toast.makeText(this, "Ditemukan ${filteredSiswaData.size} siswa perlu ditindak lanjuti", Toast.LENGTH_SHORT).show()
    }
}