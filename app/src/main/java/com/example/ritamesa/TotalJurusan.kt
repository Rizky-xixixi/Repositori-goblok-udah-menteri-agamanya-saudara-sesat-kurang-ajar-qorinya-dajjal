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
import com.example.ritamesa.data.model.MajorListResponse
import com.example.ritamesa.data.model.MajorItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TotalJurusan : AppCompatActivity() {

    // ===== DATA LIST =====
    private val listJurusanRaw = ArrayList<MajorItem>() // Data asli dari API
    private val listJurusanDisplay = ArrayList<MajorItem>() // Data yang ditampilkan (bisa difilter)

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
                showEditDialog(jurusan)
            },
            onDeleteClickListener = { jurusan ->
                showDeleteConfirmation(jurusan)
            }
        )
        recyclerView.adapter = jurusanAdapter
    }

    private fun fetchJurusanData() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        apiService.getMajors().enqueue(object : Callback<MajorListResponse> {
            override fun onResponse(call: Call<MajorListResponse>, response: Response<MajorListResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val majors = response.body()!!.data
                    
                    listJurusanRaw.clear()
                    listJurusanRaw.addAll(majors)
                    
                    listJurusanDisplay.clear()
                    listJurusanDisplay.addAll(majors)
                    
                    jurusanAdapter.updateData(listJurusanDisplay)
                } else {
                    Toast.makeText(this@TotalJurusan, "Gagal memuat data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MajorListResponse>, t: Throwable) {
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
            showAddMajorDialog()
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

    private fun showAddMajorDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_tambah_jurusan)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etNama = dialog.findViewById<EditText>(R.id.et_nama_jurusan)
        val etKode = dialog.findViewById<EditText>(R.id.et_kode_jurusan)
        val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)
        val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)

        btnBatal.setOnClickListener { dialog.dismiss() }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val kode = etKode.text.toString().trim()

            if (nama.isEmpty() || kode.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createMajor(nama, kode, dialog)
        }

        dialog.show()
    }

    private fun createMajor(nama: String, kode: String, dialog: Dialog) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val request = com.example.ritamesa.data.model.MajorCreate(kode, nama)

        apiService.createMajor(request).enqueue(object : Callback<com.example.ritamesa.data.model.GeneralResponse> {
            override fun onResponse(call: Call<com.example.ritamesa.data.model.GeneralResponse>, response: Response<com.example.ritamesa.data.model.GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TotalJurusan, "Jurusan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    fetchJurusanData()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@TotalJurusan, "Gagal menambah jurusan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.example.ritamesa.data.model.GeneralResponse>, t: Throwable) {
                Toast.makeText(this@TotalJurusan, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditDialog(jurusan: MajorItem) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_edit_jurusan)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etNama = dialog.findViewById<EditText>(R.id.et_nama_jurusan)
        val etKode = dialog.findViewById<EditText>(R.id.et_kode_jurusan)
        val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)
        val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)

        etNama.setText(jurusan.name)
        etKode.setText(jurusan.code)

        btnBatal.setOnClickListener { dialog.dismiss() }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val kode = etKode.text.toString().trim()

            if (nama.isEmpty() || kode.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateMajor(jurusan.id, nama, kode, dialog)
        }

        dialog.show()
    }

    private fun updateMajor(id: Int, nama: String, kode: String, dialog: Dialog) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val request = com.example.ritamesa.data.model.CreateMajorRequest(nama, kode)

        apiService.updateMajor(id, request).enqueue(object : Callback<com.example.ritamesa.data.model.GeneralResponse> {
            override fun onResponse(call: Call<com.example.ritamesa.data.model.GeneralResponse>, response: Response<com.example.ritamesa.data.model.GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TotalJurusan, "Jurusan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    fetchJurusanData()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@TotalJurusan, "Gagal memperbarui jurusan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.example.ritamesa.data.model.GeneralResponse>, t: Throwable) {
                Toast.makeText(this@TotalJurusan, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(jurusan: MajorItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Jurusan")
            .setMessage("Apakah Anda yakin ingin menghapus jurusan ${jurusan.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteMajor(jurusan.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteMajor(id: Int) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.deleteMajor(id).enqueue(object : Callback<com.example.ritamesa.data.model.GeneralResponse> {
            override fun onResponse(call: Call<com.example.ritamesa.data.model.GeneralResponse>, response: Response<com.example.ritamesa.data.model.GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TotalJurusan, "Jurusan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    fetchJurusanData()
                } else {
                    Toast.makeText(this@TotalJurusan, "Gagal menghapus jurusan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.example.ritamesa.data.model.GeneralResponse>, t: Throwable) {
                Toast.makeText(this@TotalJurusan, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchJurusan() {
        val query = editTextSearch.text.toString().trim().lowercase()
        
        if (query.isEmpty()) {
            listJurusanDisplay.clear()
            listJurusanDisplay.addAll(listJurusanRaw)
        } else {
            listJurusanDisplay.clear()
            listJurusanDisplay.addAll(listJurusanRaw.filter { jurusan ->
                jurusan.name.lowercase().contains(query) ||
                jurusan.code.lowercase().contains(query)
            })
        }
        
        jurusanAdapter.updateData(listJurusanDisplay)
    }
}