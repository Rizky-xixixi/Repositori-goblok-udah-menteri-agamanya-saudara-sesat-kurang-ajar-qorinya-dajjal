package com.example.ritamesa

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.MajorResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TotalJurusan : AppCompatActivity() {

    // ===== DATA LIST =====
    private val listJurusanRaw = ArrayList<Jurusan>() // Data asli dari API
    private val listJurusanDisplay = ArrayList<Jurusan>() // Data yang ditampilkan (bisa difilter)

    // ===== COMPONENTS =====
    private lateinit var recyclerView: RecyclerView
    private lateinit var jurusanAdapter: JurusanAdapter
    private lateinit var editTextSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.total_jurusan)

        initView()
        setupRecyclerView()
        setupActions()
        
        // Fetch data dari API
        fetchJurusanData()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvJurusan)
        editTextSearch = findViewById(R.id.editTextText7)
        editTextSearch.hint = "Cari jurusan"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        jurusanAdapter = JurusanAdapter(
            listJurusanDisplay,
            onEditClickListener = { jurusan ->
                // Edit not implemented for API yet, show toast
                Toast.makeText(this, "Edit fitur belum tersedia untuk API", Toast.LENGTH_SHORT).show()
            },
            onDeleteClickListener = { jurusan ->
                // Delete not implemented for API yet, show toast
                Toast.makeText(this, "Hapus fitur belum tersedia untuk API", Toast.LENGTH_SHORT).show()
                // Untuk simulasi hapus lokal:
                // showDeleteConfirmation(jurusan, listJurusanDisplay.indexOf(jurusan))
            }
        )
        recyclerView.adapter = jurusanAdapter
    }

    private fun fetchJurusanData() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        // Show loading (optional, toast for now)
        Toast.makeText(this, "Memuat data jurusan...", Toast.LENGTH_SHORT).show()

        apiService.getMajors().enqueue(object : Callback<MajorResponse> {
            override fun onResponse(call: Call<MajorResponse>, response: Response<MajorResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val majors = response.body()!!.data
                    
                    listJurusanRaw.clear()
                    listJurusanRaw.addAll(majors)
                    
                    listJurusanDisplay.clear()
                    listJurusanDisplay.addAll(majors)
                    
                    jurusanAdapter.updateData(listJurusanDisplay)
                    Toast.makeText(this@TotalJurusan, "Data berhasil dimuat: ${majors.size} jurusan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@TotalJurusan, "Gagal memuat data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MajorResponse>, t: Throwable) {
                Toast.makeText(this@TotalJurusan, "Error koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("TotalJurusan", "Error fetching majors", t)
            }
        })
    }

    private fun setupActions() {
        // BUTTON BACK
        findViewById<View>(R.id.imageView36).setOnClickListener {
            finish()
        }

        // BUTTON TAMBAH
        val btnTambah = findViewById<LinearLayout>(R.id.imageButton23)
        btnTambah.setOnClickListener {
            Toast.makeText(this, "Fitur Tambah belum tersedia untuk API", Toast.LENGTH_SHORT).show()
            // showAddDialog() 
        }

        // BUTTON SEARCH
        findViewById<View>(R.id.imageButton17).setOnClickListener {
            searchJurusan()
        }

        // ENTER KEY LISTENER UNTUK SEARCH
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                searchJurusan()
                true
            } else {
                false
            }
        }
    }

    private fun searchJurusan() {
        val query = editTextSearch.text.toString().trim()
        val filteredList = if (query.isEmpty()) {
            listJurusanRaw
        } else {
            listJurusanRaw.filter {
                it.KonsentrasiKeahlian.contains(query, true) ||
                        it.Kodejurusan.contains(query, true)
            }
        }

        if (filteredList.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(this, "Tidak ditemukan jurusan dengan kata kunci '$query'", Toast.LENGTH_SHORT).show()
        }

        listJurusanDisplay.clear()
        listJurusanDisplay.addAll(filteredList)
        jurusanAdapter.updateData(listJurusanDisplay)
    }

    // Dialog methods commented out or kept as placeholder for future implementation
    
    private fun showDeleteConfirmation(jurusan: Jurusan, position: Int) {
         // Local delete logic
    }
}