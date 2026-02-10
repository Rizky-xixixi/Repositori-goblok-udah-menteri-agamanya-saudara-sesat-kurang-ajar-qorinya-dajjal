package com.example.ritamesa

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.ClassItem
import com.example.ritamesa.data.model.ClassListResponse
import com.example.ritamesa.data.model.ClassCreate
import com.example.ritamesa.data.model.GeneralResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Locale
import java.io.FileOutputStream

class JadwalPembelajaranGuru : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JadwalAdapter
    private lateinit var searchEditText: EditText
    private lateinit var btnTambah: ImageButton
    private lateinit var btnHome: ImageButton
    private lateinit var btnRekap: ImageButton
    private lateinit var btnStatistik: ImageButton
    private lateinit var btnNotifikasi: ImageButton
    private lateinit var btnTugas: ImageButton

    private val classList = mutableListOf<ClassItem>()
    private val filteredList = mutableListOf<ClassItem>()
    private var selectedClassIdForUpload: Int? = null

    // Launcher for picking image
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null && selectedClassIdForUpload != null) {
                uploadImage(selectedClassIdForUpload!!, uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jadwal_pembelajaran_guru)

        try {
            initViews()
            setupRecyclerView()
            setupNavigation()
            setupSearch()

            loadClassesFromApi()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewSchedule)
        searchEditText = findViewById(R.id.editTextText)
        btnTambah = findViewById(R.id.btnTambah)

        // Bottom Navigation
        btnHome = findViewById(R.id.imageButton2)
        btnRekap = findViewById(R.id.imageButton3)
        btnStatistik = findViewById(R.id.imageButton5)
        btnNotifikasi = findViewById(R.id.imageButton6)
        btnTugas = findViewById(R.id.imageButton13)

        btnTambah.setOnClickListener {
            // Tampilkan dialog untuk menambah kelas baru
            showAddClassDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = JadwalAdapter(
            filteredList,
            onItemClick = { classItem ->
                if (classItem.scheduleImagePath != null) {
                    showImageDialog(classItem)
                } else {
                    Toast.makeText(this, "Belum ada gambar jadwal", Toast.LENGTH_SHORT).show()
                    // Tawarkan untuk upload gambar
                    showUploadOptionDialog(classItem)
                }
            },
            onUploadClick = { classItem ->
                selectedClassIdForUpload = classItem.id
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                pickImageLauncher.launch(intent)
            },
            onMenuClick = { classItem, anchorView ->
                showPopupMenu(classItem, anchorView)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun showAddClassDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Tambah Kelas Baru")
            .setView(R.layout.pop_up_tambah_jadwal) // Gunakan layout yang sama
            .setPositiveButton("Tambah", null) // Null untuk custom handling
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val btnTambah = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val etKelasJurusan = dialog.findViewById<EditText>(R.id.input_kelasjurusan)
            val btnPilihFile = dialog.findViewById<ImageButton>(R.id.btn_tambahkanfile)
            val tvNamaFile = dialog.findViewById<TextView>(R.id.namafile)
            val btnDropdown = dialog.findViewById<ImageButton>(R.id.btn_dropdown_arrow2)

            btnTambah.setOnClickListener {
                val className = etKelasJurusan?.text.toString()

                if (className.isEmpty()) {
                    Toast.makeText(this, "Nama kelas tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Panggil API untuk tambah kelas
                addNewClass(className)
                dialog.dismiss()
            }

            // Setup dropdown dan file picker seperti di file kedua
            btnDropdown?.setOnClickListener {
                showKelasJurusanDialog(etKelasJurusan)
            }

            btnPilihFile?.setOnClickListener {
                selectedClassIdForUpload = null // Reset untuk upload nanti
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                pickImageLauncher.launch(intent)
            }
        }

        dialog.show()
    }

    private fun addNewClass(className: String) {
        val (grade, label) = parseClassString(className)
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)

        // Buat Request Body
        val request = com.example.ritamesa.data.model.ClassCreate(
            grade = grade,
            label = label,
            majorId = null // Optional, logic to match major name could be added here
        )

        // Panggil API untuk create class
        apiService.createClass(request).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Kelas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    loadClassesFromApi() // Refresh data
                } else {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Gagal menambahkan kelas: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                Toast.makeText(this@JadwalPembelajaranGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateClass(classId: Int, newName: String, existingMajorId: Int?) {
        val (grade, label) = parseClassString(newName)
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)

        val request = ClassCreate(
            grade = grade,
            label = label,
            majorId = existingMajorId
        )

        apiService.updateClass(classId, request).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Kelas berhasil diupdate", Toast.LENGTH_SHORT).show()
                    loadClassesFromApi()
                } else {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Gagal mengupdate kelas: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                Toast.makeText(this@JadwalPembelajaranGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseClassString(input: String): Pair<String, String> {
        val parts = input.trim().split(" ", limit = 2)
        return if (parts.size >= 2) {
            Pair(parts[0], parts[1])
        } else {
            // Fallback if no space
            Pair(input, "-")
        }
    }

    private fun showPopupMenu(item: ClassItem, anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.menuInflater.inflate(R.menu.menu_jadwal, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    showEditClassDialog(item)
                    true
                }
                R.id.menu_hapus -> {
                    showDeleteConfirmation(item)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showEditClassDialog(item: ClassItem) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Kelas")
            .setView(R.layout.pop_up_tambah_jadwal)
            .setPositiveButton("Update", null)
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val btnUpdate = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val etKelasJurusan = dialog.findViewById<EditText>(R.id.input_kelasjurusan)
            val tvNamaFile = dialog.findViewById<TextView>(R.id.namafile)
            val btnDropdown = dialog.findViewById<ImageButton>(R.id.btn_dropdown_arrow2)

            // Set data existing
            etKelasJurusan?.setText(item.name)
            tvNamaFile?.text = "Klik untuk upload gambar" // Atau nama file yang ada

            btnUpdate.setOnClickListener {
                val newName = etKelasJurusan?.text.toString()

                if (newName.isEmpty()) {
                    Toast.makeText(this, "Nama kelas tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Panggil API untuk update
                updateClass(item.id, newName, item.major?.id)
                dialog.dismiss()
            }

            btnDropdown?.setOnClickListener {
                showKelasJurusanDialog(etKelasJurusan)
            }
        }

        dialog.show()
    }

    private fun updateClass(classId: Int, newName: String) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)

        // Parse the name to extract grade and label
        val parts = newName.split(" ")
        val grade = parts[0]
        val label = if (parts.size > 1) parts[1] else ""

        val request = ClassCreate(
            grade = grade,
            label = label,
            majorId = null
        )

        apiService.updateClass(classId, request).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Kelas berhasil diupdate", Toast.LENGTH_SHORT).show()
                    loadClassesFromApi()
                } else {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Gagal mengupdate kelas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                Toast.makeText(this@JadwalPembelajaranGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(item: ClassItem) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus kelas ${item.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteClass(item.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteClass(classId: Int) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)

        apiService.deleteClass(classId).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Kelas berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadClassesFromApi()
                } else {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Gagal menghapus kelas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                Toast.makeText(this@JadwalPembelajaranGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUploadOptionDialog(item: ClassItem) {
        AlertDialog.Builder(this)
            .setTitle("Jadwal Kosong")
            .setMessage("Kelas ${item.name} belum memiliki jadwal. Upload gambar jadwal?")
            .setPositiveButton("Upload") { _, _ ->
                selectedClassIdForUpload = item.id
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                pickImageLauncher.launch(intent)
            }
            .setNegativeButton("Nanti", null)
            .show()
    }

    private fun showKelasJurusanDialog(etKelasJurusan: EditText?) {
        if (classList.isEmpty()) {
            Toast.makeText(this, "Data kelas belum dimuat", Toast.LENGTH_SHORT).show()
            loadClassesFromApi()
            return
        }

        val items = classList.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Kelas/Jurusan")
            .setItems(items) { _, which ->
                etKelasJurusan?.setText(items[which])
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun uploadImage(classId: Int, uri: Uri) {
        val file = getFileFromUri(uri) ?: return
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val pd = android.app.ProgressDialog(this)
        pd.setMessage("Uploading schedule...")
        pd.show()

        apiService.uploadClassScheduleImage(classId, body).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                pd.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Jadwal berhasil diupload", Toast.LENGTH_SHORT).show()
                    loadClassesFromApi()
                } else {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Gagal upload: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                pd.dismiss()
                Toast.makeText(this@JadwalPembelajaranGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getFileFromUri(uri: Uri): File? {
        val contentResolver = contentResolver
        val fileName = "upload_${System.currentTimeMillis()}.jpg"
        val file = File(cacheDir, fileName)
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun setupNavigation() {
        btnHome.setOnClickListener {
            startActivity(Intent(this, DashboardGuruActivity::class.java))
            finish()
        }
        btnRekap.setOnClickListener {
            startActivity(Intent(this, RekapKehadiranGuru::class.java))
            finish()
        }
        btnStatistik.setOnClickListener {
            startActivity(Intent(this, StatistikKehadiran::class.java))
            finish()
        }
        btnNotifikasi.setOnClickListener {
            startActivity(Intent(this, NotifikasiGuruActivity::class.java))
            finish()
        }
        btnTugas.setOnClickListener {
            // Already here
        }
    }

    private fun loadClassesFromApi() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.getClasses().enqueue(object : Callback<ClassListResponse> {
            override fun onResponse(call: Call<ClassListResponse>, response: Response<ClassListResponse>) {
                if (response.isSuccessful) {
                    classList.clear()
                    classList.addAll(response.body()?.data ?: emptyList())
                    filterClasses(searchEditText.text.toString())
                }
            }
            override fun onFailure(call: Call<ClassListResponse>, t: Throwable) {
                Toast.makeText(this@JadwalPembelajaranGuru, "Gagal memuat kelas: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterClasses(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterClasses(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(classList)
        } else {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            classList.forEach {
                if (it.name.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredList.add(it)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun showImageDialog(classItem: ClassItem) {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_image_viewer, null)
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val btnClose = view.findViewById<Button>(R.id.btnClose)

        Glide.with(this)
            .load(classItem.scheduleImagePath)
            .into(imageView)

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.setView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    inner class JadwalAdapter(
        private val items: List<ClassItem>,
        private val onItemClick: (ClassItem) -> Unit,
        private val onUploadClick: (ClassItem) -> Unit,
        private val onMenuClick: (ClassItem, View) -> Unit
    ) : RecyclerView.Adapter<JadwalAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMataPelajaran: TextView = view.findViewById(R.id.tvMataPelajaran)
            val btnBackground: ImageButton = view.findViewById(R.id.btnBackground)
            val ivSegment: ImageButton = view.findViewById(R.id.ivSegment) // Tambahkan menu button
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mapel_kelas, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvMataPelajaran.text = item.name

            holder.btnBackground.setOnClickListener {
                onItemClick(item)
            }

            // Menu untuk edit/hapus
            holder.ivSegment.setOnClickListener { view ->
                onMenuClick(item, view)
            }

            // Long click untuk upload
            holder.btnBackground.setOnLongClickListener {
                onUploadClick(item)
                true
            }
        }

        override fun getItemCount(): Int = items.size
    }
}