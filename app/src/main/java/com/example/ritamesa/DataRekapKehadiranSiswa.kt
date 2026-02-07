package com.example.ritamesa

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.PopupMenu
import android.widget.Toast

class DataRekapkehadiranSiswa : AppCompatActivity() {

    private lateinit var rvSiswa: RecyclerView
    private lateinit var editTextSearch: EditText
    private lateinit var siswaAdapter: SiswaAdapterWaka
    private val allSiswaList = mutableListOf<Siswa>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_rekapkehadiran_siswa)

        rvSiswa = findViewById(R.id.rvKehadiran)
        editTextSearch = findViewById(R.id.editTextText5)

        setupNavigation()
        createSiswaData()
        setupRecyclerView()
        setupSearch()
    }

    private fun createSiswaData() {
        allSiswaList.clear()
        allSiswaList.addAll(listOf(
            Siswa(1, "Ahmad Rizki", "0012345678", "XII RPL 1"),
            Siswa(2, "Siti Nurhaliza", "0012345679", "XII RPL 2"),
            Siswa(3, "Budi Santoso", "0012345680", "XII RPL 1"),
            Siswa(4, "Dewi Lestari", "0012345681", "XII TKJ 1"),
            Siswa(5, "Eko Prasetyo", "0012345682", "XII RPL 2"),
            Siswa(6, "Fitria Ayu", "0012345683", "XII DKV 1"),
            Siswa(7, "Galih Pratama", "0012345684", "XII RPL 1"),
            Siswa(8, "Hana Kartika", "0012345685", "XII TKJ 2"),
            Siswa(9, "Irfan Hakim", "0012345686", "XII RPL 2"),
            Siswa(10, "Joko Widodo", "0012345687", "XII DKV 2"),
            Siswa(11, "Kartini Sari", "0012345688", "XI RPL 1"),
            Siswa(12, "Lestari Wati", "0012345689", "XI TKJ 1"),
            Siswa(13, "Mulyadi", "0012345690", "XI DKV 1"),
            Siswa(14, "Nurul Hikmah", "0012345691", "X RPL 1"),
            Siswa(15, "Oktaviani", "0012345692", "X TKJ 1")
        ))
    }

    private fun setupRecyclerView() {
        rvSiswa.layoutManager = LinearLayoutManager(this)

        siswaAdapter = SiswaAdapterWaka(allSiswaList) { siswa ->
            showPopupDetailSiswa(siswa)
        }

        rvSiswa.adapter = siswaAdapter
    }

    private fun showPopupDetailSiswa(siswa: Siswa) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_siswa_detail, null)

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

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 20f
        popupWindow.isOutsideTouchable = true

        popupView.findViewById<Button>(R.id.btnTutupPopup).setOnClickListener {
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(window.decorView.rootView, android.view.Gravity.CENTER, 0, 0)
    }

    private fun setupDataKehadiranSiswa(container: LinearLayout, siswa: Siswa) {
        container.removeAllViews()

        siswa.getDataKehadiran().forEach { kehadiran ->
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_kehadiran_popup, container, false)

            itemView.findViewById<TextView>(R.id.tvTanggal).text = kehadiran.tanggal
            itemView.findViewById<TextView>(R.id.tvMapelKelas).text =
                "${kehadiran.mataPelajaran} / ${kehadiran.kelas}"
            itemView.findViewById<TextView>(R.id.tvJam).text = kehadiran.jam
            itemView.findViewById<TextView>(R.id.tvStatus).text = kehadiran.status
            itemView.findViewById<TextView>(R.id.tvKeterangan).text = kehadiran.keterangan

            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            when (kehadiran.status.lowercase()) {
                "hadir" -> tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                "sakit" -> tvStatus.setTextColor(Color.parseColor("#FF9800"))
                "izin" -> tvStatus.setTextColor(Color.parseColor("#2196F3"))
                "alpha" -> tvStatus.setTextColor(Color.parseColor("#F44336"))
            }

            container.addView(itemView)
        }
    }

    private fun setupSearch() {
        editTextSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString().trim())
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<ImageButton>(R.id.imageButton17).setOnClickListener {
            editTextSearch.text.clear()
            editTextSearch.requestFocus()
            siswaAdapter = SiswaAdapterWaka(allSiswaList) { siswa ->
                showPopupDetailSiswa(siswa)
            }
            rvSiswa.adapter = siswaAdapter
            Toast.makeText(this, "Menampilkan semua data siswa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterData(query: String) {
        val filteredList = if (query.isEmpty()) {
            allSiswaList
        } else {
            val lowercaseQuery = query.lowercase()
            allSiswaList.filter { siswa ->
                siswa.nama.lowercase().contains(lowercaseQuery) ||
                        siswa.nisn.lowercase().contains(lowercaseQuery) ||
                        siswa.kelas.lowercase().contains(lowercaseQuery)
            }
        }

        siswaAdapter = SiswaAdapterWaka(filteredList) { siswa ->
            showPopupDetailSiswa(siswa)
        }
        rvSiswa.adapter = siswaAdapter

        if (query.isNotEmpty() && filteredList.isEmpty()) {
            Toast.makeText(this, "Tidak ditemukan siswa dengan kata kunci '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNavigation() {
        setupMoreVertButton()
        setupHomeButton()
        setupDataRekapButton()
        setupJadwalButton()
        setupStatistikButton()
        setupNotifikasiButton()
    }

    private fun setupMoreVertButton() {
        findViewById<ImageButton>(R.id.imageButton5).setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.menu_data_rekap, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_guru -> {
                        val intent = android.content.Intent(this, DataRekapKehadiranGuru::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.menu_siswa -> {
                        Toast.makeText(this, "Sudah di halaman Siswa", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun setupHomeButton() {
        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            val intent = android.content.Intent(this, DashboardWaka::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupDataRekapButton() {
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            Toast.makeText(this, "Sudah di halaman Data Rekap", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupJadwalButton() {
        findViewById<ImageButton>(R.id.imageButton4).setOnClickListener {
            try {
                val intent = android.content.Intent(this, JadwalPembelajaranGuru::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupStatistikButton() {
        findViewById<ImageButton>(R.id.imageButton55).setOnClickListener {
            try {
                val intent = android.content.Intent(this, StatistikWakaa::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNotifikasiButton() {
        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            try {
                val intent = android.content.Intent(this, NotifikasiSemuaWaka::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }
}