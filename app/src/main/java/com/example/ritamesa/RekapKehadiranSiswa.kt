package com.example.ritamesa

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RekapKehadiranSiswa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var rekapAdapter: RekapSiswaAdapter
    private lateinit var editTextSearch: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnMenu: ImageButton

    // Data dummy siswa - menggunakan struktur yang sama dengan DataRekapkehadiranSiswa
    private val siswaList = mutableListOf(
        SiswaRekap(1, "Ahmad Rizki", "0012345678", "XII RPL 1"),
        SiswaRekap(2, "Siti Nurhaliza", "0012345679", "XII RPL 2"),
        SiswaRekap(3, "Budi Santoso", "0012345680", "XII RPL 1"),
        SiswaRekap(4, "Dewi Lestari", "0012345681", "XII TKJ 1"),
        SiswaRekap(5, "Eko Prasetyo", "0012345682", "XII RPL 2"),
        SiswaRekap(6, "Fitria Ayu", "0012345683", "XII DKV 1"),
        SiswaRekap(7, "Galih Pratama", "0012345684", "XII RPL 1"),
        SiswaRekap(8, "Hana Kartika", "0012345685", "XII TKJ 2"),
        SiswaRekap(9, "Irfan Hakim", "0012345686", "XII RPL 2"),
        SiswaRekap(10, "Joko Widodo", "0012345687", "XII DKV 2"),
        SiswaRekap(11, "Kartini Sari", "0012345688", "XI RPL 1"),
        SiswaRekap(12, "Lestari Wati", "0012345689", "XI TKJ 1"),
        SiswaRekap(13, "Mulyadi", "0012345690", "XI DKV 1"),
        SiswaRekap(14, "Nurul Hikmah", "0012345691", "X RPL 1"),
        SiswaRekap(15, "Oktaviani", "0012345692", "X TKJ 1")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rekap_kehadiran_siswa)

        initView()
        setupRecyclerView()
        setupActions()
        setupBottomNavigation()
        setupSearch()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvKehadiran)
        editTextSearch = findViewById(R.id.editTextText5)
        btnBack = findViewById(R.id.btnBack)
        btnMenu = findViewById(R.id.buttonmenu)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        rekapAdapter = RekapSiswaAdapter(
            siswaList,
            onLihatClickListener = { siswa ->
                showPopupDetailSiswa(siswa)
            }
        )
        recyclerView.adapter = rekapAdapter
    }

    private fun showPopupDetailSiswa(siswa: SiswaRekap) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_siswa_detail, null)

        // Set data siswa
        popupView.findViewById<TextView>(R.id.tvPopupNama).text = siswa.nama
        popupView.findViewById<TextView>(R.id.tvPopupNisn).text = siswa.nisn
        popupView.findViewById<TextView>(R.id.tvPopupKelas).text = siswa.kelas

        val container = popupView.findViewById<LinearLayout>(R.id.containerKehadiran)
        setupDataKehadiranSiswa(container, siswa)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set background transparan untuk efek blur
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 20f
        popupWindow.isOutsideTouchable = true

        // Set opacity untuk background popup
        val popupContainer = popupView.findViewById<View>(R.id.popupContainer)
        popupContainer.alpha = 0.95f  // Sedikit transparan untuk efek blur

        popupView.findViewById<Button>(R.id.btnTutupPopup).setOnClickListener {
            popupWindow.dismiss()
        }

        // Tambahkan background semi-transparan untuk area di luar popup
        val backgroundView = View(this)
        backgroundView.setBackgroundColor(Color.parseColor("#80000000")) // Hitam semi-transparan
        val rootView = window.decorView.rootView as ViewGroup
        rootView.addView(backgroundView)

        popupWindow.showAtLocation(window.decorView.rootView, android.view.Gravity.CENTER, 0, 0)

        // Hapus background view saat popup ditutup
        popupWindow.setOnDismissListener {
            rootView.removeView(backgroundView)
        }
    }

    private fun setupDataKehadiranSiswa(container: LinearLayout, siswa: SiswaRekap) {
        container.removeAllViews()

        siswa.getDataKehadiran().forEach { kehadiran ->
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_kehadiran_popup, container, false)

            itemView.findViewById<TextView>(R.id.tvTanggal).text = kehadiran.tanggal
            itemView.findViewById<TextView>(R.id.tvMapelKelas).text = kehadiran.mataPelajaran
            itemView.findViewById<TextView>(R.id.tvJam).text = kehadiran.jam
            itemView.findViewById<TextView>(R.id.tvStatus).text = kehadiran.status
            itemView.findViewById<TextView>(R.id.tvKeterangan).text = kehadiran.keterangan

            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            when (kehadiran.status.lowercase()) {
                "hadir" -> tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                "sakit" -> tvStatus.setTextColor(Color.parseColor("#FF9800"))
                "izin" -> tvStatus.setTextColor(Color.parseColor("#2196F3"))
                "alpha" -> tvStatus.setTextColor(Color.parseColor("#F44336"))
                "terlambat" -> tvStatus.setTextColor(Color.parseColor("#FF9800"))
            }

            container.addView(itemView)
        }
    }

    private fun setupActions() {
        // BUTTON BACK
        btnBack.setOnClickListener {
            finish()
        }

        // BUTTON MENU (More Vert)
        btnMenu.setOnClickListener {
            showPopupMenu(it)
        }
    }

    private fun setupSearch() {
        // SEARCH TEXT LISTENER
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString().trim())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // BUTTON SEARCH CLEAR
        findViewById<ImageButton>(R.id.imageButton17).setOnClickListener {
            editTextSearch.text.clear()
            editTextSearch.requestFocus()
            rekapAdapter.filter("")
            Toast.makeText(this, "Menampilkan semua data siswa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterData(query: String) {
        rekapAdapter.filter(query)

        if (query.isNotEmpty() && rekapAdapter.itemCount == 0) {
            Toast.makeText(this, "Tidak ditemukan siswa dengan kata kunci '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_rekap_switch, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_guru -> {
                    // Pindah ke halaman rekap guru
                    val intent = Intent(this, RekapKehadiranGuru::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.menu_siswa -> {
                    // Sudah di halaman siswa
                    Toast.makeText(this, "Sudah di halaman Siswa", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }

        popup.show()
    }

    private fun setupBottomNavigation() {
        // Home - ke Dashboard
        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }

        // Contacts (Active) - ke Data Rekap (halaman ini)
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            Toast.makeText(this, "Sudah di halaman Data Rekap", Toast.LENGTH_SHORT).show()
        }

        // Bar Chart - ke Statistik
        findViewById<ImageButton>(R.id.imageButton5).setOnClickListener {
            try {
                val intent = Intent(this, StatistikKehadiran::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        // Notifications - ke Notifikasi
        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            try {
                val intent = Intent(this, NotifikasiSemua::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Data class untuk siswa
    data class SiswaRekap(
        val id: Int,
        val nama: String,
        val nisn: String,
        val kelas: String
    ) {
        // Method untuk mendapatkan data kehadiran (dummy data)
        fun getDataKehadiran(): List<Kehadiran> {
            return listOf(
                Kehadiran("Senin, 7 Januari 2026", "Bahasa Indonesia / $kelas", "07:00", "Hadir", "Hadir tepat waktu"),
                Kehadiran("Selasa, 8 Januari 2026", "Matematika / $kelas", "08:45", "Hadir", "Hadir tepat waktu"),
                Kehadiran("Rabu, 9 Januari 2026", "Bahasa Inggris / $kelas", "10:30", "Sakit", "Izin sakit"),
                Kehadiran("Kamis, 10 Januari 2026", "Pemrograman Dasar / $kelas", "13:15", "Izin", "Izin keluarga"),
                Kehadiran("Jumat, 11 Januari 2026", "Basis Data / $kelas", "07:00", "Alpha", "Tidak hadir tanpa keterangan")
            )
        }
    }

    // Data class untuk kehadiran
    data class Kehadiran(
        val tanggal: String,
        val mataPelajaran: String,
        val jam: String,
        val status: String,
        val keterangan: String
    )

    // Adapter untuk RecyclerView siswa
    class RekapSiswaAdapter(
        private var dataList: List<SiswaRekap>,
        private val onLihatClickListener: (SiswaRekap) -> Unit
    ) : RecyclerView.Adapter<RekapSiswaAdapter.SiswaViewHolder>() {

        private var filteredList: List<SiswaRekap> = dataList

        inner class SiswaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
            val tvNama: TextView = itemView.findViewById(R.id.tvNama)
            val tvNisn: TextView = itemView.findViewById(R.id.tvTelepon)
            val tvKelasJurusan: TextView = itemView.findViewById(R.id.tvMataPelajaran)
            val btnLihat: ImageButton = itemView.findViewById(R.id.btnLihat)

            init {
                btnLihat.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onLihatClickListener(filteredList[position])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lihat_rekap_guru, parent, false)
            return SiswaViewHolder(view)
        }

        override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
            val siswa = filteredList[position]
            holder.tvNomor.text = (position + 1).toString()
            holder.tvNama.text = siswa.nama
            holder.tvNisn.text = siswa.nisn
            holder.tvKelasJurusan.text = siswa.kelas
        }

        override fun getItemCount(): Int = filteredList.size

        fun filter(query: String) {
            filteredList = if (query.isEmpty()) {
                dataList
            } else {
                val lowercaseQuery = query.lowercase()
                dataList.filter { siswa ->
                    siswa.nama.lowercase().contains(lowercaseQuery) ||
                            siswa.nisn.lowercase().contains(lowercaseQuery) ||
                            siswa.kelas.lowercase().contains(lowercaseQuery)
                }
            }
            notifyDataSetChanged()
        }

        fun updateData(newData: List<SiswaRekap>) {
            dataList = newData
            filteredList = newData
            notifyDataSetChanged()
        }
    }
}