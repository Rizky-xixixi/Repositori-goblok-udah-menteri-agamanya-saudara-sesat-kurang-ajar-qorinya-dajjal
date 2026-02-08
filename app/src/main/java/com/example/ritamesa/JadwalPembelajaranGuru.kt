package com.example.ritamesa

import android.app.Activity
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
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.ClassItem
import com.example.ritamesa.data.model.GeneralResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
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
             // For simplicity in this UI, we might ask which class to upload for
             // But usually upload is per-item. 
             // Implementing "Add" button as "Refresh" or "Help" for now since upload is usually per item
             loadClassesFromApi()
             Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show()
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
                }
            },
            onUploadClick = { classItem ->
                selectedClassIdForUpload = classItem.id
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                pickImageLauncher.launch(intent)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun uploadImage(classId: Int, uri: Uri) {
        val file = getFileFromUri(uri)
        if (file == null) {
            Toast.makeText(this, "Gagal mengambil file", Toast.LENGTH_SHORT).show()
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        Toast.makeText(this, "Mengupload gambar...", Toast.LENGTH_SHORT).show()

        apiService.uploadClassScheduleImage(classId, body).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Upload Berhasil", Toast.LENGTH_SHORT).show()
                    loadClassesFromApi() // Refresh
                } else {
                    Toast.makeText(this@JadwalPembelajaranGuru, "Gagal Upload: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                 Toast.makeText(this@JadwalPembelajaranGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getFileFromUri(uri: Uri): File? {
        try {
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", ".jpg", cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun setupNavigation() {
        btnHome.setOnClickListener { navigateTo(DashboardWaka::class.java) }
        btnRekap.setOnClickListener { navigateTo(DataRekapKehadiranGuru::class.java) }
        btnStatistik.setOnClickListener { navigateTo(StatistikWakaa::class.java) }
        btnNotifikasi.setOnClickListener { navigateTo(NotifikasiSemuaWaka::class.java) }
        btnTugas.setOnClickListener {
            Toast.makeText(this, "Anda sudah berada di Jadwal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
        finish()
    }

    private fun loadClassesFromApi() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        // Show loading?
        
        apiService.getClasses().enqueue(object : Callback<List<ClassItem>> {
            override fun onResponse(call: Call<List<ClassItem>>, response: Response<List<ClassItem>>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    classList.clear()
                    classList.addAll(data)
                    filterClasses(searchEditText.text.toString())
                }
            }

            override fun onFailure(call: Call<List<ClassItem>>, t: Throwable) {
                Toast.makeText(this@JadwalPembelajaranGuru, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterClasses(s.toString())
            }
        })
    }

    private fun filterClasses(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(classList)
        } else {
            val lowerQuery = query.lowercase()
            classList.forEach { item ->
                if (item.name.lowercase().contains(lowerQuery)) {
                    filteredList.add(item)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
    
    private fun showImageDialog(item: ClassItem) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(item.scheduleImagePath) 
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak dapat membuka gambar", Toast.LENGTH_SHORT).show()
        }
    }

    class JadwalAdapter(
        private val items: List<ClassItem>,
        private val onItemClick: (ClassItem) -> Unit,
        private val onUploadClick: (ClassItem) -> Unit
    ) : RecyclerView.Adapter<JadwalAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMataPelajaran: TextView = view.findViewById(R.id.tvMataPelajaran)
            val btnBackground: ImageButton = view.findViewById(R.id.btnBackground)
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
            // Add long click to upload/replace
            holder.btnBackground.setOnLongClickListener {
                onUploadClick(item)
                true
            }
        }

        override fun getItemCount(): Int = items.size
    }
}