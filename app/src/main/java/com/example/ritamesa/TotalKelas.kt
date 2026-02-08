package com.example.ritamesa

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.ClassItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TotalKelas : AppCompatActivity() {

    // ===== DATA LIST =====
    private val listKelasRaw = ArrayList<Kelas>()
    private val listKelasDisplay = ArrayList<Kelas>()

    // ===== DATA DROPDOWN =====
    private val listJurusan = listOf("RPL", "TKJ", "MM", "TKR", "TSM", "TITL", "AK", "AP", "PH")
    private val listTingkatan = listOf("X", "XI", "XII")
    private val listRombel = listOf("1", "2", "3", "4", "5", "6")

    // ===== COMPONENTS =====
    private lateinit var recyclerView: RecyclerView
    private lateinit var kelasAdapter: KelasAdapter
    private lateinit var editTextSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.total_kelas)

        initView()
        setupRecyclerView()
        setupActions()
        
        // Fetch data from API
        fetchKelasData()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvKelas)
        editTextSearch = findViewById(R.id.editTextText7)
        editTextSearch.hint = "Cari nama kelas"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        kelasAdapter = KelasAdapter(
            listKelasDisplay,
            onEditClickListener = { kelas ->
                Toast.makeText(this, "Edit fitur belum tersedia untuk API", Toast.LENGTH_SHORT).show()
                // showEditDialog(kelas, listKelasDisplay.indexOf(kelas))
            },
            onDeleteClickListener = { kelas ->
                Toast.makeText(this, "Hapus fitur belum tersedia untuk API", Toast.LENGTH_SHORT).show()
                // showDeleteConfirmation(kelas, listKelasDisplay.indexOf(kelas))
            }
        )
        recyclerView.adapter = kelasAdapter
    }
    
    private fun fetchKelasData() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        Toast.makeText(this, "Memuat data kelas...", Toast.LENGTH_SHORT).show()
        
        apiService.getClasses().enqueue(object : Callback<List<ClassItem>> {
            override fun onResponse(call: Call<List<ClassItem>>, response: Response<List<ClassItem>>) {
                if (response.isSuccessful && response.body() != null) {
                    val classItems = response.body()!!
                    
                    val mappedClasses = classItems.map { item ->
                        Kelas(
                            id = item.id,
                            namaJurusan = item.major?.name ?: "-",
                            namaKelas = item.name,
                            waliKelas = item.homeroomTeacher?.user?.name ?: "-"
                        )
                    }
                    
                    listKelasRaw.clear()
                    listKelasRaw.addAll(mappedClasses)
                    
                    listKelasDisplay.clear()
                    listKelasDisplay.addAll(mappedClasses)
                    
                    kelasAdapter.updateData(listKelasDisplay)
                    Toast.makeText(this@TotalKelas, "Data berhasil dimuat: ${mappedClasses.size} kelas", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@TotalKelas, "Gagal memuat data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<List<ClassItem>>, t: Throwable) {
                Toast.makeText(this@TotalKelas, "Error koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("TotalKelas", "Error fetching classes", t)
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
            searchKelas()
        }

        // ENTER KEY LISTENER UNTUK SEARCH
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                searchKelas()
                true
            } else {
                false
            }
        }
    }

    private fun searchKelas() {
        val query = editTextSearch.text.toString().trim()
        val filteredList = if (query.isEmpty()) {
            listKelasRaw
        } else {
            listKelasRaw.filter {
                it.namaJurusan.contains(query, true) ||
                        it.namaKelas.contains(query, true) ||
                        it.waliKelas.contains(query, true)
            }
        }

        if (filteredList.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(this, "Tidak ditemukan kelas dengan kata kunci '$query'", Toast.LENGTH_SHORT).show()
        }

        listKelasDisplay.clear()
        listKelasDisplay.addAll(filteredList)
        kelasAdapter.updateData(listKelasDisplay)
    }

    // Dialog methods commented out for now
    
    private fun showAddDialog() {
         // Local add logic
    }

    private fun showJurusanDropdown(dialog: Dialog, etJurusan: EditText) {
         // Local dropdown logic
    }

    private fun showKelasDropdown(dialog: Dialog, etKelas: EditText) {
         // Local dropdown logic
    }

    private fun showEditDialog(kelas: Kelas, position: Int) {
         // Local edit logic
    }

    private fun showDeleteConfirmation(kelas: Kelas, position: Int) {
         // Local delete logic
    }
}
