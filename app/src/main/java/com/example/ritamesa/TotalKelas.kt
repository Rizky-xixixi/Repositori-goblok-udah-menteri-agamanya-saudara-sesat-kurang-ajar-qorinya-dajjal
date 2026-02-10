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
import com.example.ritamesa.data.model.ClassListResponse
import com.example.ritamesa.data.model.MajorListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TotalKelas : AppCompatActivity() {

    // ===== DATA LIST =====
    private val listKelasRaw = ArrayList<Kelas>()
    private val listKelasDisplay = ArrayList<Kelas>()

    // ===== DATA DROPDOWN =====
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

    private val majorsForDropdown = ArrayList<com.example.ritamesa.data.model.MajorItem>()

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        kelasAdapter = KelasAdapter(
            listKelasDisplay,
            onEditClickListener = { kelas ->
                showEditDialog(kelas)
            },
            onDeleteClickListener = { kelas ->
                showDeleteConfirmation(kelas)
            }
        )
        recyclerView.adapter = kelasAdapter
    }
    
    private fun fetchKelasData() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        apiService.getClasses().enqueue(object : Callback<ClassListResponse> {
            override fun onResponse(call: Call<ClassListResponse>, response: Response<ClassListResponse>) {
                if (response.isSuccessful) {
                    val classItems = response.body()?.data ?: emptyList()
                    
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
                } else {
                    Toast.makeText(this@TotalKelas, "Gagal memuat data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<ClassListResponse>, t: Throwable) {
                Toast.makeText(this@TotalKelas, "Error koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("TotalKelas", "Error fetching classes", t)
            }
        })
    }

    private fun fetchMajorsForDropdown() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.getMajors().enqueue(object : Callback<MajorListResponse> {
            override fun onResponse(call: Call<MajorListResponse>, response: Response<MajorListResponse>) {
                if (response.isSuccessful) {
                    majorsForDropdown.clear()
                    majorsForDropdown.addAll(response.body()?.data ?: emptyList())
                }
            }
            override fun onFailure(call: Call<MajorListResponse>, t: Throwable) {}
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
            showAddDialog()
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
        
        fetchMajorsForDropdown()
    }

    private fun showAddDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_tambah_kelas)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etJurusan = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
        val etKelas = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
        val arrowJurusan = dialog.findViewById<ImageButton>(R.id.arrowJurusan)
        val arrowKelas = dialog.findViewById<ImageButton>(R.id.imageButton9)
        val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)
        val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)

        var selectedMajorId: Int? = null

        arrowJurusan.setOnClickListener {
            val names = majorsForDropdown.map { it.name }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Pilih Jurusan")
                .setItems(names) { _, which ->
                    val major = majorsForDropdown[which]
                    etJurusan.setText(major.name)
                    selectedMajorId = major.id
                }
                .show()
        }

        arrowKelas.setOnClickListener {
            val kelasNames = mutableListOf<String>()
            for (t in listTingkatan) {
                for (r in listRombel) {
                    kelasNames.add("$t $r")
                }
            }
            AlertDialog.Builder(this)
                .setTitle("Pilih Kelas")
                .setItems(kelasNames.toTypedArray()) { _, which ->
                    etKelas.setText(kelasNames[which])
                }
                .show()
        }

        btnBatal.setOnClickListener { dialog.dismiss() }

        btnSimpan.setOnClickListener {
            val classStr = etKelas.text.toString().trim()
            if (classStr.isEmpty() || selectedMajorId == null) {
                Toast.makeText(this, "Jurusan dan Kelas harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parts = classStr.split(" ")
            val grade = parts[0]
            val label = if (parts.size > 1) parts[1] else "-"

            createClass(grade, label, selectedMajorId!!, dialog)
        }

        dialog.show()
    }

    private fun createClass(grade: String, label: String, majorId: Int, dialog: Dialog) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val request = com.example.ritamesa.data.model.ClassCreate(grade, label, majorId)

        apiService.createClass(request).enqueue(object : Callback<com.example.ritamesa.data.model.GeneralResponse> {
            override fun onResponse(call: Call<com.example.ritamesa.data.model.GeneralResponse>, response: Response<com.example.ritamesa.data.model.GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TotalKelas, "Kelas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    fetchKelasData()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@TotalKelas, "Gagal menambah kelas", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<com.example.ritamesa.data.model.GeneralResponse>, t: Throwable) {
                Toast.makeText(this@TotalKelas, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditDialog(kelas: Kelas) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_edit_kelas)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etJurusan = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
        val etKelas = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
        val arrowJurusan = dialog.findViewById<ImageButton>(R.id.arrowJurusan)
        val arrowKelas = dialog.findViewById<ImageButton>(R.id.imageButton9)
        val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)
        val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)

        etJurusan.setText(kelas.namaJurusan)
        etKelas.setText(kelas.namaKelas)

        var selectedMajorId: Int? = majorsForDropdown.find { it.name == kelas.namaJurusan }?.id

        arrowJurusan.setOnClickListener {
            val names = majorsForDropdown.map { it.name }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Pilih Jurusan")
                .setItems(names) { _, which ->
                    val major = majorsForDropdown[which]
                    etJurusan.setText(major.name)
                    selectedMajorId = major.id
                }
                .show()
        }

        arrowKelas.setOnClickListener {
            val kelasNames = mutableListOf<String>()
            for (t in listTingkatan) {
                for (r in listRombel) {
                    kelasNames.add("$t $r")
                }
            }
            AlertDialog.Builder(this)
                .setTitle("Pilih Kelas")
                .setItems(kelasNames.toTypedArray()) { _, which ->
                    etKelas.setText(kelasNames[which])
                }
                .show()
        }

        btnBatal.setOnClickListener { dialog.dismiss() }

        btnSimpan.setOnClickListener {
            val classStr = etKelas.text.toString().trim()
            if (classStr.isEmpty() || selectedMajorId == null) {
                Toast.makeText(this, "Jurusan dan Kelas harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parts = classStr.split(" ")
            val grade = parts[0]
            val label = if (parts.size > 1) parts[1] else "-"

            updateClass(kelas.id, grade, label, selectedMajorId!!, dialog)
        }

        dialog.show()
    }

    private fun updateClass(id: Int, grade: String, label: String, majorId: Int, dialog: Dialog) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val request = com.example.ritamesa.data.model.ClassCreate(grade, label, majorId)

        apiService.updateClass(id, request).enqueue(object : Callback<com.example.ritamesa.data.model.GeneralResponse> {
            override fun onResponse(call: Call<com.example.ritamesa.data.model.GeneralResponse>, response: Response<com.example.ritamesa.data.model.GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TotalKelas, "Kelas berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    fetchKelasData()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@TotalKelas, "Gagal memperbarui kelas", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<com.example.ritamesa.data.model.GeneralResponse>, t: Throwable) {
                Toast.makeText(this@TotalKelas, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(kelas: Kelas) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kelas")
            .setMessage("Apakah Anda yakin ingin menghapus kelas ${kelas.namaKelas}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteClass(kelas.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteClass(id: Int) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.deleteClass(id).enqueue(object : Callback<com.example.ritamesa.data.model.GeneralResponse> {
            override fun onResponse(call: Call<com.example.ritamesa.data.model.GeneralResponse>, response: Response<com.example.ritamesa.data.model.GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TotalKelas, "Kelas berhasil dihapus", Toast.LENGTH_SHORT).show()
                    fetchKelasData()
                } else {
                    Toast.makeText(this@TotalKelas, "Gagal menghapus kelas", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<com.example.ritamesa.data.model.GeneralResponse>, t: Throwable) {
                Toast.makeText(this@TotalKelas, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchKelas() {
        val query = editTextSearch.text.toString().trim().lowercase()
        
        if (query.isEmpty()) {
            listKelasDisplay.clear()
            listKelasDisplay.addAll(listKelasRaw)
        } else {
            listKelasDisplay.clear()
            listKelasDisplay.addAll(listKelasRaw.filter { kelas ->
                kelas.namaKelas.lowercase().contains(query) ||
                kelas.namaJurusan.lowercase().contains(query) ||
                kelas.waliKelas.lowercase().contains(query)
            })
        }
        
        kelasAdapter.updateData(listKelasDisplay)
    }
}
