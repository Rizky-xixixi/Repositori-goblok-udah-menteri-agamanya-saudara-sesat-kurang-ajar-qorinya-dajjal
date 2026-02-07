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

class RekapKehadiranGuru : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GuruAdapter
    private lateinit var editTextSearch: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnMenu: ImageButton

    // Data guru
    private val guruList = listOf(
        Guru("1", "Dr. Ahmad Sudrajat", "2006041001", "Matematika"),
        Guru("2", "Siti Aminah, M.Pd.", "2006041002", "Bahasa Indonesia"),
        Guru("3", "Agus Wijaya, S.Pd.", "2006041003", "IPA"),
        Guru("4", "Dewi Lestari, M.Pd.", "2006041004", "IPS"),
        Guru("5", "Rudi Hartono, S.Pd.", "2006041005", "PJOK"),
        Guru("6", "Budi Santoso, M.Pd.", "2006041006", "Bahasa Inggris"),
        Guru("7", "Maya Indah, S.Pd.", "2006041007", "Seni Budaya"),
        Guru("8", "Hendra Pratama, S.Pd.", "2006041008", "Pemrograman"),
        Guru("9", "Sri Wahyuni, M.Pd.", "2006041009", "Sejarah"),
        Guru("10", "Fajar Nugroho, S.Pd.", "2006041010", "Kimia")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rekap_kehadiran_guru)

        initView()
        setupRecyclerView()
        setupActions()
        setupBottomNavigation()
        setupSearch()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.recyclerViewGuru)
        editTextSearch = findViewById(R.id.editTextText5)
        btnBack = findViewById(R.id.btnBack)
        btnMenu = findViewById(R.id.buttonmenu)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = GuruAdapter(guruList) { guru ->
            showPopupDetailGuru(guru)
        }
        recyclerView.adapter = adapter
    }

    private fun showPopupDetailGuru(guru: Guru) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_guru_detail, null)

        // Set data guru
        popupView.findViewById<TextView>(R.id.tvPopupNama).text = guru.nama
        popupView.findViewById<TextView>(R.id.tvPopupNip).text = guru.nip
        popupView.findViewById<TextView>(R.id.tvPopupMapel).text = guru.mataPelajaran

        val container = popupView.findViewById<LinearLayout>(R.id.containerKehadiran)
        setupDataKehadiranGuru(container, guru)

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

    private fun setupDataKehadiranGuru(container: LinearLayout, guru: Guru) {
        container.removeAllViews()

        getGuruKehadiranData(guru).forEach { kehadiran ->
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_kehadiran_popup, container, false)

            itemView.findViewById<TextView>(R.id.tvTanggal).text = kehadiran.tanggal
            itemView.findViewById<TextView>(R.id.tvMapelKelas).text = kehadiran.mataPelajaranKelas
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

    private fun getGuruKehadiranData(guru: Guru): List<KehadiranGuru> {
        return when (guru.nomor) {
            "1" -> listOf(
                KehadiranGuru("Senin, 7 Januari 2026", "Matematika / XII RPL 1", "07:00 - 08:30", "Hadir", "Mengajar sesuai jadwal"),
                KehadiranGuru("Selasa, 8 Januari 2026", "Matematika / XII RPL 2", "08:45 - 10:15", "Hadir", "Mengajar dengan baik"),
                KehadiranGuru("Rabu, 9 Januari 2026", "Matematika / XI RPL 1", "10:30 - 12:00", "Izin", "Izin dinas"),
                KehadiranGuru("Kamis, 10 Januari 2026", "Matematika / XI RPL 2", "13:15 - 14:45", "Hadir", "Mengajar sesuai jadwal"),
                KehadiranGuru("Jumat, 11 Januari 2026", "Matematika / X RPL 1", "07:00 - 08:30", "Terlambat", "Terlambat 10 menit")
            )
            "2" -> listOf(
                KehadiranGuru("Senin, 7 Januari 2026", "Bahasa Indonesia / XII TKJ 1", "08:45 - 10:15", "Hadir", "Mengajar sesuai jadwal"),
                KehadiranGuru("Selasa, 8 Januari 2026", "Bahasa Indonesia / XII TKJ 2", "10:30 - 12:00", "Hadir", "Mengajar dengan baik"),
                KehadiranGuru("Rabu, 9 Januari 2026", "Bahasa Indonesia / XI TKJ 1", "13:15 - 14:45", "Sakit", "Izin sakit"),
                KehadiranGuru("Kamis, 10 Januari 2026", "Bahasa Indonesia / XI TKJ 2", "07:00 - 08:30", "Hadir", "Mengajar sesuai jadwal"),
                KehadiranGuru("Jumat, 11 Januari 2026", "Bahasa Indonesia / X TKJ 1", "08:45 - 10:15", "Hadir", "Mengajar dengan baik")
            )
            "3" -> listOf(
                KehadiranGuru("Senin, 7 Januari 2026", "IPA / XII MM 1", "10:30 - 12:00", "Hadir", "Mengajar sesuai jadwal"),
                KehadiranGuru("Selasa, 8 Januari 2026", "IPA / XII MM 2", "13:15 - 14:45", "Hadir", "Mengajar dengan baik"),
                KehadiranGuru("Rabu, 9 Januari 2026", "IPA / XI MM 1", "07:00 - 08:30", "Hadir", "Mengajar sesuai jadwal"),
                KehadiranGuru("Kamis, 10 Januari 2026", "IPA / XI MM 2", "08:45 - 10:15", "Alpha", "Tidak hadir tanpa keterangan"),
                KehadiranGuru("Jumat, 11 Januari 2026", "IPA / X MM 1", "10:30 - 12:00", "Hadir", "Mengajar dengan baik")
            )
            else -> listOf(
                KehadiranGuru("Senin, 7 Januari 2026", "${guru.mataPelajaran} / XII", "07:00 - 08:30", "Hadir", "Mengajar sesuai jadwal"),
                KehadiranGuru("Selasa, 8 Januari 2026", "${guru.mataPelajaran} / XI", "08:45 - 10:15", "Hadir", "Mengajar dengan baik"),
                KehadiranGuru("Rabu, 9 Januari 2026", "${guru.mataPelajaran} / X", "10:30 - 12:00", "Hadir", "Mengajar sesuai jadwal"),
                KehadiranGuru("Kamis, 10 Januari 2026", "${guru.mataPelajaran} / XII", "13:15 - 14:45", "Hadir", "Mengajar sesuai jadwal"),
                KehadiranGuru("Jumat, 11 Januari 2026", "${guru.mataPelajaran} / XI", "07:00 - 08:30", "Hadir", "Mengajar dengan baik")
            )
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
                performSearch(s.toString().trim())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // BUTTON SEARCH CLEAR
        findViewById<ImageButton>(R.id.imageButton17).setOnClickListener {
            editTextSearch.text.clear()
            editTextSearch.requestFocus()
            adapter.filter("")
            Toast.makeText(this, "Menampilkan semua data guru", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSearch(query: String) {
        adapter.filter(query)

        if (query.isNotEmpty() && adapter.itemCount == 0) {
            Toast.makeText(this, "Tidak ditemukan guru dengan kata kunci '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_rekap_switch, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_guru -> {
                    // Sudah di halaman guru
                    Toast.makeText(this, "Sudah di halaman Guru", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_siswa -> {
                    // Pindah ke halaman rekap siswa
                    val intent = Intent(this, RekapKehadiranSiswa::class.java)
                    startActivity(intent)
                    finish()
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

        // Contacts - ke Rekap Kehadiran (Siswa)
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            val intent = Intent(this, RekapKehadiranSiswa::class.java)
            startActivity(intent)
            finish()
        }

        // Bar Chart - ke Statistik
        findViewById<ImageButton>(R.id.imageButton5).setOnClickListener {
            val intent = Intent(this, StatistikKehadiran::class.java)
            startActivity(intent)
            finish()
        }

        // Notifications - ke Notifikasi
        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            val intent = Intent(this, NotifikasiSemua::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Data class untuk guru
    data class Guru(
        val nomor: String,
        val nama: String,
        val nip: String,
        val mataPelajaran: String
    )

    // Data class untuk kehadiran guru
    data class KehadiranGuru(
        val tanggal: String,
        val mataPelajaranKelas: String,
        val jam: String,
        val status: String,
        val keterangan: String
    )

    // Adapter untuk RecyclerView dengan filter
    class GuruAdapter(
        private var guruList: List<Guru>,
        private val onLihatClickListener: (Guru) -> Unit
    ) : RecyclerView.Adapter<GuruAdapter.GuruViewHolder>() {

        private var filteredList: List<Guru> = guruList

        inner class GuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
            val tvNama: TextView = itemView.findViewById(R.id.tvNama)
            val tvTelepon: TextView = itemView.findViewById(R.id.tvTelepon)
            val tvMataPelajaran: TextView = itemView.findViewById(R.id.tvMataPelajaran)
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lihat_rekap_guru, parent, false)
            return GuruViewHolder(view)
        }

        override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
            val guru = filteredList[position]
            holder.tvNomor.text = guru.nomor
            holder.tvNama.text = guru.nama
            holder.tvTelepon.text = guru.nip
            holder.tvMataPelajaran.text = guru.mataPelajaran
        }

        override fun getItemCount(): Int = filteredList.size

        // Fungsi untuk filter data
        fun filter(query: String) {
            filteredList = if (query.isEmpty()) {
                guruList
            } else {
                val lowercaseQuery = query.lowercase()
                guruList.filter {
                    it.nama.lowercase().contains(lowercaseQuery) ||
                            it.nip.lowercase().contains(lowercaseQuery) ||
                            it.mataPelajaran.lowercase().contains(lowercaseQuery)
                }
            }
            notifyDataSetChanged()
        }
    }
}