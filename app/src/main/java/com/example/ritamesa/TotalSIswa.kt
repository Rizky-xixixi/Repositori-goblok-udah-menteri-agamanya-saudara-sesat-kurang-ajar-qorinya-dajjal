package com.example.ritamesa

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TotalSiswa : AppCompatActivity() {

    // ===== ADAPTER =====
    inner class SiswaAdapter(
        private val listSiswa: List<StudentItem>
    ) : RecyclerView.Adapter<SiswaAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNo: TextView = view.findViewById(R.id.tvNo)
            val tvNama: TextView = view.findViewById(R.id.tvNama)
            val tvNisn: TextView = view.findViewById(R.id.tvNisn)
            val tvKelas: TextView = view.findViewById(R.id.tvKelas)
            val tvJurusan: TextView = view.findViewById(R.id.tvKode)
            val tvJk: TextView = view.findViewById(R.id.tvJk)
            val btnEdit: LinearLayout = view.findViewById(R.id.btnEdit)
            val btnHapus: LinearLayout = view.findViewById(R.id.btnHapus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_crud_datasiswa, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val siswa = listSiswa[position]

            holder.tvNo.text = (position + 1).toString()
            holder.tvNama.text = siswa.name
            holder.tvNisn.text = siswa.nisn ?: "-"
            holder.tvKelas.text = siswa.className
            holder.tvJurusan.text = siswa.majorName
            holder.tvJk.text = siswa.gender ?: "-"

            holder.btnEdit.setOnClickListener {
                showEditDialog(siswa)
            }

            holder.btnHapus.setOnClickListener {
                showDeleteDialog(siswa)
            }
        }

        override fun getItemCount(): Int = listSiswa.size
    }

    // ===== COMPONENTS =====
    private lateinit var recyclerView: RecyclerView
    private lateinit var siswaAdapter: SiswaAdapter
    private lateinit var btnTambahContainer: View
    private lateinit var editTextSearch: EditText
    private lateinit var ivSearch: ImageView
    private lateinit var apiService: ApiService
    
    private var listSiswa: MutableList<StudentItem> = mutableListOf()
    private var listClasses: List<ClassItem> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.total_siswa)

        apiService = ApiClient.getClient(this).create(ApiService::class.java)

        initView()
        setupRecyclerView()
        setupActions()
        
        // Fetch Initial Data
        fetchClasses()
        fetchStudents()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvSiswa)
        btnTambahContainer = findViewById(R.id.imageButton13)
        editTextSearch = findViewById(R.id.editTextText)
        ivSearch = findViewById(R.id.imageButton12)

        editTextSearch.hint = "Cari nama / NISN"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        siswaAdapter = SiswaAdapter(listSiswa)
        recyclerView.adapter = siswaAdapter
    }

    private fun setupActions() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        btnTambahContainer.setOnClickListener {
            showAddDialog()
        }

        ivSearch.setOnClickListener {
            searchSiswa()
        }

        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                searchSiswa()
                true
            } else {
                false
            }
        }
    }

    private fun fetchClasses() {
        apiService.getClasses().enqueue(object : Callback<ClassListResponse> {
            override fun onResponse(call: Call<ClassListResponse>, response: Response<ClassListResponse>) {
                if (response.isSuccessful) {
                    listClasses = response.body()?.data ?: listOf()
                }
            }
            override fun onFailure(call: Call<ClassListResponse>, t: Throwable) {
                Log.e("TotalSiswa", "Failed to fetch classes", t)
            }
        })
    }

    private fun fetchStudents(query: String? = null) {
        val call = apiService.getStudents(search = query)
        call.enqueue(object : Callback<StudentListResponse> {
            override fun onResponse(call: Call<StudentListResponse>, response: Response<StudentListResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: listOf()
                    listSiswa.clear()
                    listSiswa.addAll(data)
                    siswaAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@TotalSiswa, "Gagal mengambil data siswa", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StudentListResponse>, t: Throwable) {
                Toast.makeText(this@TotalSiswa, "Error koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchSiswa() {
        val query = editTextSearch.text.toString().trim()
        fetchStudents(if (query.isNotEmpty()) query else null)
    }

    // ===== DIALOGS =====

    private fun showAddDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_tambah_data_siswa)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val inputNama = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
        val inputNisn = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
        val inputJurusan = dialog.findViewById<EditText>(R.id.input_keterangan_jurusan)
        val inputKelas = dialog.findViewById<EditText>(R.id.input_kelas)
        val inputJenis = dialog.findViewById<EditText>(R.id.input_jenis)
        val btnArrowKelas = dialog.findViewById<ImageButton>(R.id.imageButton8)
        val btnArrowJenis = dialog.findViewById<ImageButton>(R.id.imageButton9)
        val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)
        val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)

        // Dropdowns
        var selectedClassId: Int? = null
        
        btnArrowKelas.setOnClickListener {
             showKelasDropdown(inputKelas) { classItem ->
                 selectedClassId = classItem.id
                 inputJurusan.setText(classItem.major?.name ?: "-")
             }
        }

        btnArrowJenis.setOnClickListener {
            showJenisKelaminDropdown(inputJenis)
        }

        btnBatal.setOnClickListener { dialog.dismiss() }

        btnSimpan.setOnClickListener {
            val nama = inputNama.text.toString().trim()
            val nisn = inputNisn.text.toString().trim()
            val jenis = if (inputJenis.text.toString().contains("Laki")) "L" else "P"
            
            if (selectedClassId == null) {
                Toast.makeText(this, "Pilih Kelas terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nama.isEmpty() || nisn.isEmpty()) {
                Toast.makeText(this, "Nama dan NISN wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = CreateStudentRequest(
                name = nama,
                nisn = nisn,
                nis = nisn,
                classId = selectedClassId!!,
                gender = jenis,
                username = nisn
            )

            createStudent(request, dialog)
        }

        dialog.show()
    }

    private fun createStudent(request: CreateStudentRequest, dialog: Dialog) {
        apiService.createStudent(request).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TotalSiswa, "Siswa berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    fetchStudents()
                } else {
                    // Smart Upsert Logic
                    if (response.code() == 422) {
                        try {
                            val errorBody = response.errorBody()?.string()
                            if (errorBody != null && errorBody.contains("nisn")) {
                                // Duplicate NISN detected
                                showSmartUpsertDialog(request, dialog)
                                return
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    Toast.makeText(this@TotalSiswa, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                Toast.makeText(this@TotalSiswa, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showSmartUpsertDialog(request: CreateStudentRequest, parentDialog: Dialog) {
        AlertDialog.Builder(this)
            .setTitle("Siswa Sudah Ada")
            .setMessage("Siswa dengan NISN ${request.nisn} sudah terdaftar. Apakah Anda ingin memperbarui data siswa tersebut?")
            .setPositiveButton("Update") { _, _ ->
                // To update, we need ID. We search by NISN first.
                findAndUpdateStudent(request, parentDialog)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun findAndUpdateStudent(request: CreateStudentRequest, parentDialog: Dialog) {
        // Search by NISN to get ID
        apiService.getStudents(search = request.nisn).enqueue(object : Callback<StudentListResponse> {
            override fun onResponse(call: Call<StudentListResponse>, response: Response<StudentListResponse>) {
                val students = response.body()?.data
                val target = students?.find { it.nisn == request.nisn }
                
                if (target != null) {
                    performUpdate(target.id, request, parentDialog)
                } else {
                    Toast.makeText(this@TotalSiswa, "Gagal menemukan siswa lama untuk diupdate", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<StudentListResponse>, t: Throwable) {
                Toast.makeText(this@TotalSiswa, "Gagal mencari siswa: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun performUpdate(id: Int, request: CreateStudentRequest, parentDialog: Dialog) {
        apiService.updateStudent(id, request).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TotalSiswa, "Data siswa berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    parentDialog.dismiss()
                    fetchStudents()
                } else {
                    Toast.makeText(this@TotalSiswa, "Gagal update: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                Toast.makeText(this@TotalSiswa, "Error update: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditDialog(siswa: StudentItem) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_edit_data_siswa)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val inputNama = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
        val inputNisn = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
        val inputJurusan = dialog.findViewById<EditText>(R.id.input_keterangan_jurusan)
        val inputKelas = dialog.findViewById<EditText>(R.id.input_kelas)
        val inputJenis = dialog.findViewById<EditText>(R.id.input_jenis)
        val btnArrowKelas = dialog.findViewById<ImageButton>(R.id.imageButton8)
        val btnArrowJenis = dialog.findViewById<ImageButton>(R.id.imageButton9)
        val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)
        val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)

        inputNama.setText(siswa.name)
        inputNisn.setText(siswa.nisn)
        inputKelas.setText(siswa.className)
        inputJurusan.setText(siswa.majorName)
        inputJenis.setText(siswa.gender)

        var selectedClassId = siswa.classRoom?.id ?: 0

        btnArrowKelas.setOnClickListener {
             showKelasDropdown(inputKelas) { classItem ->
                 selectedClassId = classItem.id
                 inputJurusan.setText(classItem.major?.name ?: "-")
             }
        }
        btnArrowJenis.setOnClickListener { showJenisKelaminDropdown(inputJenis) }

        btnBatal.setOnClickListener { dialog.dismiss() }
        
        btnSimpan.setOnClickListener {
             val request = CreateStudentRequest(
                name = inputNama.text.toString(),
                nisn = inputNisn.text.toString(),
                nis = inputNisn.text.toString(),
                classId = selectedClassId,
                gender = if(inputJenis.text.toString().contains("P")) "P" else "L",
                username = inputNisn.text.toString()
            )
            performUpdate(siswa.id, request, dialog)
        }
        
        dialog.show()
    }

    private fun showDeleteDialog(siswa: StudentItem) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Hapus siswa ${siswa.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                apiService.deleteStudent(siswa.id).enqueue(object : Callback<GeneralResponse> {
                    override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@TotalSiswa, "Dihapus", Toast.LENGTH_SHORT).show()
                            fetchStudents()
                        } else {
                            Toast.makeText(this@TotalSiswa, "Gagal hapus", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                         Toast.makeText(this@TotalSiswa, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showKelasDropdown(editText: EditText, onSelected: (ClassItem) -> Unit) {
        val items = listClasses.map { "${it.grade} ${it.label}" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Pilih Kelas")
            .setItems(items) { _, which ->
                val selected = listClasses[which]
                editText.setText("${selected.grade} ${selected.label}")
                onSelected(selected)
            }
            .show()
    }

    private fun showJenisKelaminDropdown(editText: EditText) {
        val jenisList = arrayOf("Laki-laki (L)", "Perempuan (P)")
        AlertDialog.Builder(this)
            .setTitle("Pilih Jenis Kelamin")
            .setItems(jenisList) { _, which ->
                editText.setText(if (which == 0) "L" else "P")
            }
            .show()
    }
}