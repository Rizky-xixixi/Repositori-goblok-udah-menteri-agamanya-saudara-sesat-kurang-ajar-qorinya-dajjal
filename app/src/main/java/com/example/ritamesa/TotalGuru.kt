package com.example.ritamesa

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.model.CreateTeacherRequest
import com.example.ritamesa.data.model.GeneralResponse
import com.example.ritamesa.data.model.UpdateTeacherRequest

class TotalGuru : AppCompatActivity() {

    // ===== DATA LIST =====
    private var listGuru = mutableListOf<com.example.ritamesa.data.model.TeacherItem>()
    private var filteredList = mutableListOf<com.example.ritamesa.data.model.TeacherItem>()
    private var listSubjects = mutableListOf<com.example.ritamesa.data.model.SubjectItem>()

    // ===== COMPONENTS =====
    private lateinit var recyclerView: RecyclerView
    private lateinit var guruAdapter: GuruAdapter
    private lateinit var editTextSearch: EditText
    private lateinit var apiService: com.example.ritamesa.data.api.ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.total_guru)

        apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)

        initView()
        setupRecyclerView()
        setupActions()
        loadTeachersFromApi()
        fetchSubjects()
    }

    private fun loadTeachersFromApi() {
        val pd = android.app.ProgressDialog(this)
        pd.setMessage("Memuat data...")
        pd.show()

        apiService.getTeachers().enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.TeacherListResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.ritamesa.data.model.TeacherListResponse>,
                response: retrofit2.Response<com.example.ritamesa.data.model.TeacherListResponse>
            ) {
                pd.dismiss()
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        listGuru.clear()
                        listGuru.addAll(data)
                        filterGuru(editTextSearch.text.toString())
                    }
                } else {
                    Toast.makeText(this@TotalGuru, "Gagal memuat data guru", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.TeacherListResponse>, t: Throwable) {
                pd.dismiss()
                Toast.makeText(this@TotalGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchSubjects() {
        apiService.getSubjects(-1).enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.SubjectListResponse> {
            override fun onResponse(call: retrofit2.Call<com.example.ritamesa.data.model.SubjectListResponse>, response: retrofit2.Response<com.example.ritamesa.data.model.SubjectListResponse>) {
                if (response.isSuccessful) {
                    listSubjects.clear()
                    listSubjects.addAll(response.body()?.data ?: emptyList())
                }
            }
            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.SubjectListResponse>, t: Throwable) {
                Log.e("TotalGuru", "Failed to fetch subjects", t)
            }
        })
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvGuru)
        editTextSearch = findViewById(R.id.editTextText7)
        editTextSearch.hint = "Cari nama guru"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        guruAdapter = GuruAdapter(filteredList,
            onEditClick = { guru, _ ->
                showEditDialog(guru)
            },
            onDeleteClick = { guru, _ ->
                showDeleteConfirmation(guru)
            }
        )
        recyclerView.adapter = guruAdapter
    }

    private fun setupActions() {
        // BUTTON BACK
        findViewById<ImageButton>(R.id.imageView36).setOnClickListener {
            finish()
        }

        // BUTTON TAMBAH
        val btnTambah = findViewById<LinearLayout>(R.id.imageButton23)
        btnTambah.setOnClickListener {
            showAddDialog()
        }

        // BUTTON SEARCH
        findViewById<ImageButton>(R.id.imageButton17).setOnClickListener {
            filterGuru(editTextSearch.text.toString())
        }

        // ENTER KEY LISTENER UNTUK SEARCH
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                filterGuru(editTextSearch.text.toString())
                true
            } else {
                false
            }
        }
        
        editTextSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterGuru(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun filterGuru(query: String) {
        val q = query.trim()
        filteredList.clear()
        if (q.isEmpty()) {
            filteredList.addAll(listGuru)
        } else {
            filteredList.addAll(listGuru.filter {
                it.nama.contains(q, true) ||
                it.nip?.contains(q, true) == true
            })
        }
        guruAdapter.notifyDataSetChanged()
    }

    private fun showAddDialog() {
        try {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.pop_up_tambah_data_guru)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(true)

            val inputNama = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
            val inputNip = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
            val inputKode = dialog.findViewById<EditText>(R.id.input_keterangan_jurusan)
            val inputMapel = dialog.findViewById<EditText>(R.id.input_kelas)
            val inputKeterangan = dialog.findViewById<EditText>(R.id.input_jenis)
            val btnArrowMapel = dialog.findViewById<ImageButton>(R.id.imageButton8)
            val btnArrowRole = dialog.findViewById<ImageButton>(R.id.imageButton9)
            val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
            val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)

            btnArrowMapel?.setOnClickListener {
                showMapelDropdown(inputMapel)
            }

            btnArrowRole?.setOnClickListener {
                showKeteranganDropdown(inputKeterangan)
            }

            btnBatal?.setOnClickListener {
                dialog.dismiss()
            }

            btnSimpan?.setOnClickListener {
                val nama = inputNama?.text?.toString()?.trim() ?: ""
                val nip = inputNip?.text?.toString()?.trim() ?: ""
                val kode = inputKode?.text?.toString()?.trim() ?: ""
                val mapel = inputMapel?.text?.toString()?.trim() ?: ""
                val keterangan = inputKeterangan?.text?.toString()?.trim() ?: ""

                if (nama.isEmpty() || nip.isEmpty() || kode.isEmpty() || mapel.isEmpty() || keterangan.isEmpty()) {
                    Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                showSaveConfirmation("Tambah") {
                    createTeacher(nama, nip, kode, mapel, keterangan, dialog)
                }
            }

            dialog.show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun createTeacher(nama: String, nip: String, kode: String, mapel: String, keterangan: String, dialog: Dialog) {
        val pd = android.app.ProgressDialog(this)
        pd.setMessage("Menyimpan data...")
        pd.show()

        val request = CreateTeacherRequest(
            name = nama,
            username = nip, // Default username uses NIP
            email = null,
            password = "password123", // Default password
            nip = nip,
            phone = null,
            contact = null,
            homeroomClassId = null,
            subject = mapel
        )

        apiService.createTeacher(request).enqueue(object : retrofit2.Callback<GeneralResponse> {
            override fun onResponse(call: retrofit2.Call<GeneralResponse>, response: retrofit2.Response<GeneralResponse>) {
                pd.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(this@TotalGuru, "Data guru berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadTeachersFromApi()
                } else {
                    Toast.makeText(this@TotalGuru, "Gagal menambahkan: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<GeneralResponse>, t: Throwable) {
                pd.dismiss()
                Toast.makeText(this@TotalGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditDialog(guru: com.example.ritamesa.data.model.TeacherItem) {
        try {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.pop_up_tambah_data_guru)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(true)

            val inputNama = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
            val inputNip = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
            val inputKode = dialog.findViewById<EditText>(R.id.input_keterangan_jurusan)
            val inputMapel = dialog.findViewById<EditText>(R.id.input_kelas)
            val inputKeterangan = dialog.findViewById<EditText>(R.id.input_jenis)
            val btnArrowMapel = dialog.findViewById<ImageButton>(R.id.imageButton8)
            val btnArrowRole = dialog.findViewById<ImageButton>(R.id.imageButton9)
            val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
            val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)

            dialog.setTitle("Edit Data Guru")

            inputNama?.setText(guru.name)
            inputNip?.setText(guru.nip)
            // inputKode?.setText(guru.kode) // TeacherItem might not have code, using stub
            inputMapel?.setText(guru.subject)
            // inputKeterangan?.setText(guru.keterangan) // stub

            btnArrowMapel?.setOnClickListener {
                showMapelDropdown(inputMapel)
            }

            btnArrowRole?.setOnClickListener {
                showKeteranganDropdown(inputKeterangan)
            }

            btnBatal?.setOnClickListener {
                dialog.dismiss()
            }

            btnSimpan?.setOnClickListener {
                val nama = inputNama?.text?.toString()?.trim() ?: ""
                val mapel = inputMapel?.text?.toString()?.trim() ?: ""
                // Nip might not be editable or ignored if not changed

                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                showSaveConfirmation("Edit") {
                    updateTeacher(guru.id, nama, mapel, dialog)
                }
            }

            dialog.show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun updateTeacher(id: Int, nama: String, mapel: String, dialog: Dialog) {
        val pd = android.app.ProgressDialog(this)
        pd.setMessage("Mengupdate data...")
        pd.show()

        val request = UpdateTeacherRequest(
            name = nama,
            email = null,
            phone = null,
            contact = null,
            homeroomClassId = null,
            subject = mapel
        )

        apiService.updateTeacher(id, request).enqueue(object : retrofit2.Callback<GeneralResponse> {
            override fun onResponse(call: retrofit2.Call<GeneralResponse>, response: retrofit2.Response<GeneralResponse>) {
                pd.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(this@TotalGuru, "Data guru berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadTeachersFromApi()
                } else {
                    Toast.makeText(this@TotalGuru, "Gagal mengupdate: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<GeneralResponse>, t: Throwable) {
                pd.dismiss()
                Toast.makeText(this@TotalGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(guru: com.example.ritamesa.data.model.TeacherItem) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin akan menghapus data ${guru.name}?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                 val pd = android.app.ProgressDialog(this)
                 pd.setMessage("Menghapus data...")
                 pd.show()
                 
                 apiService.deleteTeacher(guru.id).enqueue(object : retrofit2.Callback<GeneralResponse> {
                    override fun onResponse(call: retrofit2.Call<GeneralResponse>, response: retrofit2.Response<GeneralResponse>) {
                        pd.dismiss()
                        if (response.isSuccessful) {
                            Toast.makeText(this@TotalGuru, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                            loadTeachersFromApi()
                        } else {
                            Toast.makeText(this@TotalGuru, "Gagal menghapus: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: retrofit2.Call<GeneralResponse>, t: Throwable) {
                        pd.dismiss()
                        Toast.makeText(this@TotalGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                 })
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showSaveConfirmation(action: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Yakin ${action.lowercase()} data?")
            .setPositiveButton("Ya, Simpan") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showMapelDropdown(editText: EditText?) {
        if (listSubjects.isEmpty()) {
            Toast.makeText(this, "Data mapel belum dimuat, mencoba lagi...", Toast.LENGTH_SHORT).show()
            fetchSubjects()
            return
        }

        val items = listSubjects.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Mapel")
            .setItems(items) { _, which ->
                editText?.setText(items[which])
            }
            .show()
    }

    private fun showKeteranganDropdown(editText: EditText?) {
        val keteranganList = arrayOf("Guru", "Waka", "Admin")

        AlertDialog.Builder(this)
            .setTitle("Pilih Keterangan")
            .setItems(keteranganList) { _, which ->
                editText?.setText(keteranganList[which])
            }
            .show()
    }
}