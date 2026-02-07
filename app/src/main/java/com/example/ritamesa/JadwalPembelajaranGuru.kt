package com.example.ritamesa

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

    private val jadwalList = mutableListOf<JadwalItem>()
    private val filteredList = mutableListOf<JadwalItem>()
    private var selectedImageUri: Uri? = null
    private val REQUEST_PICK_IMAGE = 101
    private var currentDialog: Dialog? = null
    private var editingItemId: Int = -1 // Untuk melacak item yang sedang diedit

    data class JadwalItem(
        val id: Int,
        val namaKelas: String,
        val filePath: String?,
        val fileName: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jadwal_pembelajaran_guru)

        initViews()
        setupRecyclerView()
        setupNavigation()
        loadDummyData()
        setupSearch()
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
            showTambahJadwalDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = JadwalAdapter(
            filteredList, // Gunakan filteredList, bukan jadwalList
            onItemClick = { item ->
                // Tampilkan gambar jadwal saat item diklik
                showImageDialog(item)
            },
            onMenuClick = { item, anchorView ->
                showPopupMenu(item, anchorView)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupNavigation() {
        btnHome.setOnClickListener {
            val intent = Intent(this, DashboardWaka::class.java)
            startActivity(intent)
            finish()
        }

        btnRekap.setOnClickListener {
            val intent = Intent(this, DataRekapKehadiranGuru::class.java)
            startActivity(intent)
            finish()
        }

        btnStatistik.setOnClickListener {
            val intent = Intent(this, StatistikWakaa::class.java)
            startActivity(intent)
            finish()
        }

        btnNotifikasi.setOnClickListener {
            val intent = Intent(this, NotifikasiSemuaWaka::class.java)
            startActivity(intent)
            finish()
        }

        btnTugas.setOnClickListener {
            // Tetap di halaman ini karena ini adalah halaman Jadwal
            Toast.makeText(this, "Anda sudah berada di Jadwal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDummyData() {
        jadwalList.clear()
        jadwalList.addAll(listOf(
            JadwalItem(1, "12 Design Komunikasi Visual 2", null, "jadwal_dkv2.png"),
            JadwalItem(2, "11 Teknik Komputer Jaringan", null, "jadwal_tkj.pdf"),
            JadwalItem(3, "10 Multimedia 1", null, "jadwal_mm1.jpg"),
            JadwalItem(4, "12 Rekayasa Perangkat Lunak", null, "jadwal_rpl.png"),
            JadwalItem(5, "11 Akuntansi 2", null, "jadwal_ak2.pdf"),
            JadwalItem(6, "10 Otomatisasi Tata Kelola Perkantoran", null, "jadwal_otkp.jpg")
        ))
        filterJadwal("") // Load filteredList
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterJadwal(s.toString())
            }
        })
    }

    private fun filterJadwal(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(jadwalList)
        } else {
            val lowerQuery = query.lowercase()
            jadwalList.forEach { item ->
                if (item.namaKelas.lowercase().contains(lowerQuery)) {
                    filteredList.add(item)
                }
            }
        }
        adapter.updateList(filteredList)
    }

    private fun showTambahJadwalDialog() {
        editingItemId = -1 // Reset editing item
        selectedImageUri = null // Reset selected image

        val dialog = Dialog(this)
        currentDialog = dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.pop_up_tambah_jadwal)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnUpload = dialog.findViewById<Button>(R.id.btn_upload)
        val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
        val btnPilihFile = dialog.findViewById<ImageButton>(R.id.btn_tambahkanfile)
        val tvNamaFile = dialog.findViewById<TextView>(R.id.namafile)
        val btnDropdown = dialog.findViewById<ImageButton>(R.id.btn_dropdown_arrow2)
        val etKelasJurusan = dialog.findViewById<EditText>(R.id.input_kelasjurusan)

        // Reset fields
        tvNamaFile.text = "Masukkan (png/jpg)"
        etKelasJurusan.text.clear()
        btnUpload.text = "Tambah"

        // Pilih file
        btnPilihFile.setOnClickListener {
            openFilePicker()
        }

        // Dropdown kelas/jurusan
        btnDropdown.setOnClickListener {
            showKelasJurusanDialog(etKelasJurusan)
        }

        // Upload
        btnUpload.setOnClickListener {
            val kelasJurusan = etKelasJurusan.text.toString()
            val fileName = tvNamaFile.text.toString()

            if (kelasJurusan.isEmpty()) {
                Toast.makeText(this, "Pilih kelas/jurusan terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fileName.isEmpty() || fileName == "Masukkan (png/jpg)") {
                Toast.makeText(this, "Pilih file terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newId = if (jadwalList.isNotEmpty()) jadwalList.last().id + 1 else 1
            val newJadwal = JadwalItem(newId, kelasJurusan, selectedImageUri?.toString(), fileName)

            jadwalList.add(newJadwal)
            filterJadwal(searchEditText.text.toString())

            Toast.makeText(this, "Jadwal berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // Batal
        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // Allow all file types
            addCategory(Intent.CATEGORY_OPENABLE)
            // Untuk Android 11+ (API 30+), tambahkan ini
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf"))
        }
        try {
            startActivityForResult(
                Intent.createChooser(intent, "Pilih File Jadwal"),
                REQUEST_PICK_IMAGE
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak ada aplikasi file manager", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showKelasJurusanDialog(etKelasJurusan: EditText) {
        val kelasJurusanList = arrayOf(
            "10 Multimedia 1", "10 Multimedia 2", "10 Teknik Komputer Jaringan",
            "10 Rekayasa Perangkat Lunak", "10 Akuntansi 1", "10 Akuntansi 2",
            "10 Otomatisasi Tata Kelola Perkantoran", "11 Multimedia 1", "11 Multimedia 2",
            "11 Teknik Komputer Jaringan", "11 Rekayasa Perangkat Lunak", "11 Akuntansi 1",
            "11 Akuntansi 2", "11 Otomatisasi Tata Kelola Perkantoran", "12 Multimedia 1",
            "12 Multimedia 2", "12 Teknik Komputer Jaringan", "12 Rekayasa Perangkat Lunak",
            "12 Akuntansi 1", "12 Akuntansi 2", "12 Otomatisasi Tata Kelola Perkantoran",
            "12 Design Komunikasi Visual 1", "12 Design Komunikasi Visual 2"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih Kelas/Jurusan")
            .setItems(kelasJurusanList) { _, which ->
                etKelasJurusan.setText(kelasJurusanList[which])
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showPopupMenu(item: JadwalItem, anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.menuInflater.inflate(R.menu.menu_jadwal, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    showEditJadwalDialog(item)
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

    private fun showEditJadwalDialog(item: JadwalItem) {
        editingItemId = item.id
        selectedImageUri = if (item.filePath != null) Uri.parse(item.filePath) else null

        val dialog = Dialog(this)
        currentDialog = dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.pop_up_tambah_jadwal)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnUpload = dialog.findViewById<Button>(R.id.btn_upload)
        val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
        val btnPilihFile = dialog.findViewById<ImageButton>(R.id.btn_tambahkanfile)
        val tvNamaFile = dialog.findViewById<TextView>(R.id.namafile)
        val btnDropdown = dialog.findViewById<ImageButton>(R.id.btn_dropdown_arrow2)
        val etKelasJurusan = dialog.findViewById<EditText>(R.id.input_kelasjurusan)

        // Set data yang ada
        etKelasJurusan.setText(item.namaKelas)
        tvNamaFile.text = item.fileName
        btnUpload.text = "Update"

        btnPilihFile.setOnClickListener {
            openFilePicker()
        }

        btnDropdown.setOnClickListener {
            showKelasJurusanDialog(etKelasJurusan)
        }

        btnUpload.setOnClickListener {
            val kelasJurusan = etKelasJurusan.text.toString()
            val fileName = tvNamaFile.text.toString()

            if (kelasJurusan.isEmpty()) {
                Toast.makeText(this, "Pilih kelas/jurusan terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fileName.isEmpty() || fileName == "Masukkan (png/jpg)") {
                Toast.makeText(this, "Pilih file terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update item di list
            val index = jadwalList.indexOfFirst { it.id == editingItemId }
            if (index != -1) {
                jadwalList[index] = JadwalItem(
                    id = editingItemId,
                    namaKelas = kelasJurusan,
                    fileName = fileName,
                    filePath = selectedImageUri?.toString() ?: jadwalList[index].filePath
                )
                filterJadwal(searchEditText.text.toString())
                Toast.makeText(this, "Jadwal berhasil diupdate", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Item tidak ditemukan", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
            editingItemId = -1 // Reset
        }

        btnBatal.setOnClickListener {
            editingItemId = -1 // Reset
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(item: JadwalItem) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus jadwal ${item.namaKelas}?")
            .setPositiveButton("Hapus") { _, _ ->
                // Hapus dari jadwalList
                jadwalList.removeIf { it.id == item.id }
                // Refresh filteredList
                filterJadwal(searchEditText.text.toString())
                Toast.makeText(this, "Jadwal berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showImageDialog(item: JadwalItem) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_viewer)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val imageView = dialog.findViewById<ImageView>(R.id.imageView)
        val tvFileName = dialog.findViewById<TextView>(R.id.tvFileName)
        val btnClose = dialog.findViewById<Button>(R.id.btnClose)

        tvFileName.text = "${item.namaKelas} - ${item.fileName}"
        // TODO: Load gambar dari URI jika ada
        imageView.setImageResource(R.drawable.rectangle_247)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                val fileName = getFileNameFromUri(uri)

                // Update TextView di dialog yang sedang aktif
                currentDialog?.let { dialog ->
                    val tvNamaFile = dialog.findViewById<TextView>(R.id.namafile)
                    tvNamaFile?.text = fileName ?: "file_terpilih"
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val displayNameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                        if (displayNameIndex != -1) {
                            result = it.getString(displayNameIndex)
                        }
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(this, "Izin akses file diperlukan", Toast.LENGTH_SHORT).show()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }

    class JadwalAdapter(
        private var items: List<JadwalItem>,
        private val onItemClick: (JadwalItem) -> Unit,
        private val onMenuClick: (JadwalItem, View) -> Unit
    ) : RecyclerView.Adapter<JadwalAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMataPelajaran: TextView = view.findViewById(R.id.tvMataPelajaran)
            val ivSegment: ImageButton = view.findViewById(R.id.ivSegment)
            val btnBackground: ImageButton = view.findViewById(R.id.btnBackground)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mapel_kelas, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvMataPelajaran.text = item.namaKelas

            holder.btnBackground.setOnClickListener {
                onItemClick(item)
            }

            holder.ivSegment.setOnClickListener { view ->
                onMenuClick(item, view)
            }
        }

        override fun getItemCount(): Int = items.size

        fun updateList(newList: List<JadwalItem>) {
            items = newList
            notifyDataSetChanged()
        }
    }
}