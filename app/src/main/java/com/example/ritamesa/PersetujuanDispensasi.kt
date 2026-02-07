package com.example.ritamesa

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PersetujuanDispensasi : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DispensasiAdapter
    private lateinit var allDispensasiList: MutableList<Dispensasi>
    private var currentFilter: StatusDispensasi? = null
    private lateinit var searchEditText: EditText
    private lateinit var kelasDropdown: TextView

    private val kelasList = listOf(
        "Semua Kelas",
        "10 Rekayasa Perangkat Lunak 1",
        "10 Rekayasa Perangkat Lunak 2",
        "10 Rekayasa Perangkat Lunak 3",
        "10 Teknik Komputer Jaringan 1",
        "10 Teknik Komputer Jaringan 2",
        "10 Teknik Komputer Jaringan 3",
        "10 Desain Komunikasi Visual 1",
        "10 Desain Komunikasi Visual 2",
        "10 Desain Komunikasi Visual 3",
        "11 Rekayasa Perangkat Lunak 1",
        "11 Rekayasa Perangkat Lunak 2",
        "11 Rekayasa Perangkat Lunak 3",
        "11 Teknik Komputer Jaringan 1",
        "11 Teknik Komputer Jaringan 2",
        "11 Teknik Komputer Jaringan 3",
        "11 Desain Komunikasi Visual 1",
        "11 Desain Komunikasi Visual 2",
        "11 Desain Komunikasi Visual 3",
        "12 Rekayasa Perangkat Lunak 1",
        "12 Rekayasa Perangkat Lunak 2",
        "12 Rekayasa Perangkat Lunak 3",
        "12 Teknik Komputer Jaringan 1",
        "12 Teknik Komputer Jaringan 2",
        "12 Teknik Komputer Jaringan 3",
        "12 Desain Komunikasi Visual 1",
        "12 Desain Komunikasi Visual 2",
        "12 Desain Komunikasi Visual 3"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.persetujuan_dispensasi)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeDummyData()
        setupRecyclerView()
        setupFilterButtons()
        setupBackButton()
        setupSearch()
        setupKelasDropdown()
    }

    private fun initializeDummyData() {
        allDispensasiList = mutableListOf(
            Dispensasi(
                namaSiswa = "Ahmad Rizki",
                kelas = "12 Rekayasa Perangkat Lunak 2",
                mataPelajaran = "Bahasa Indonesia",
                hari = "Senin",
                tanggal = "5 Januari 2026",
                jamKe = "1-2",
                guruPengajar = "Ibu Siti Rahayu",
                catatan = "Mengikuti lomba",
                status = StatusDispensasi.MENUNGGU
            ),
            Dispensasi(
                namaSiswa = "Siti Nurhaliza",
                kelas = "12 Teknik Komputer Jaringan 1",
                mataPelajaran = "Matematika",
                hari = "Selasa",
                tanggal = "6 Januari 2026",
                jamKe = "3-4",
                guruPengajar = "Pak Budi Santoso",
                catatan = "Sakit",
                status = StatusDispensasi.DISETUJUI
            ),
            Dispensasi(
                namaSiswa = "Budi Prasetyo",
                kelas = "10 Rekayasa Perangkat Lunak 1",
                mataPelajaran = "Pemrograman Web",
                hari = "Rabu",
                tanggal = "7 Januari 2026",
                jamKe = "5-6",
                guruPengajar = "Pak Andi Wijaya",
                catatan = "Keperluan keluarga",
                status = StatusDispensasi.DITOLAK
            ),
            Dispensasi(
                namaSiswa = "Dewi Kartika",
                kelas = "12 Desain Komunikasi Visual 1",
                mataPelajaran = "Desain Grafis",
                hari = "Kamis",
                tanggal = "8 Januari 2026",
                jamKe = "2-3",
                guruPengajar = "Ibu Maya Kusuma",
                catatan = "Mengikuti workshop",
                status = StatusDispensasi.MENUNGGU
            ),
            Dispensasi(
                namaSiswa = "Eko Prasetyo",
                kelas = "10 Teknik Komputer Jaringan 1",
                mataPelajaran = "Sistem Operasi",
                hari = "Jumat",
                tanggal = "9 Januari 2026",
                jamKe = "1-2",
                guruPengajar = "Pak Joko Widodo",
                catatan = "Sakit kepala",
                status = StatusDispensasi.MENUNGGU
            ),
            Dispensasi(
                namaSiswa = "Fitri Handayani",
                kelas = "12 Rekayasa Perangkat Lunak 2",
                mataPelajaran = "Bahasa Inggris",
                hari = "Senin",
                tanggal = "12 Januari 2026",
                jamKe = "4-5",
                guruPengajar = "Ibu Linda Sari",
                catatan = "Urusan administrasi",
                status = StatusDispensasi.DISETUJUI
            ),
            Dispensasi(
                namaSiswa = "Gilang Ramadhan",
                kelas = "10 Rekayasa Perangkat Lunak 1",
                mataPelajaran = "Animasi 2D",
                hari = "Selasa",
                tanggal = "13 Januari 2026",
                jamKe = "3-4",
                guruPengajar = "Pak Dedi Susanto",
                catatan = "Mengikuti pelatihan",
                status = StatusDispensasi.MENUNGGU
            )
        )
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewDispensasi)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DispensasiAdapter(allDispensasiList) { dispensasi ->
            if (dispensasi.status == StatusDispensasi.MENUNGGU) {
                showDetailDialog(dispensasi)
            }
        }

        recyclerView.adapter = adapter
    }

    private fun setupFilterButtons() {
        val buttonSemua: Button = findViewById(R.id.buttonSemua)
        val buttonMenunggu: Button = findViewById(R.id.buttonMenunggu)
        val buttonDisetujui: Button = findViewById(R.id.buttonDisetujui)
        val buttonDitolak: Button = findViewById(R.id.buttonDitolak)

        buttonSemua.setOnClickListener {
            currentFilter = null
            filterList(null, searchEditText.text.toString(), kelasDropdown.text.toString())
            highlightButton(buttonSemua)
        }

        buttonMenunggu.setOnClickListener {
            currentFilter = StatusDispensasi.MENUNGGU
            filterList(StatusDispensasi.MENUNGGU, searchEditText.text.toString(), kelasDropdown.text.toString())
            highlightButton(buttonMenunggu)
        }

        buttonDisetujui.setOnClickListener {
            currentFilter = StatusDispensasi.DISETUJUI
            filterList(StatusDispensasi.DISETUJUI, searchEditText.text.toString(), kelasDropdown.text.toString())
            highlightButton(buttonDisetujui)
        }

        buttonDitolak.setOnClickListener {
            currentFilter = StatusDispensasi.DITOLAK
            filterList(StatusDispensasi.DITOLAK, searchEditText.text.toString(), kelasDropdown.text.toString())
            highlightButton(buttonDitolak)
        }

        highlightButton(buttonSemua)
    }

    private fun highlightButton(selectedButton: Button) {
        val buttonSemua: Button = findViewById(R.id.buttonSemua)
        val buttonMenunggu: Button = findViewById(R.id.buttonMenunggu)
        val buttonDisetujui: Button = findViewById(R.id.buttonDisetujui)
        val buttonDitolak: Button = findViewById(R.id.buttonDitolak)

        buttonSemua.setBackgroundResource(R.drawable.button_filter_unselected)
        buttonSemua.setTextColor(Color.BLACK)

        buttonMenunggu.setBackgroundResource(R.drawable.button_filter_unselected)
        buttonMenunggu.setTextColor(Color.BLACK)

        buttonDisetujui.setBackgroundResource(R.drawable.button_filter_unselected)
        buttonDisetujui.setTextColor(Color.BLACK)

        buttonDitolak.setBackgroundResource(R.drawable.button_filter_unselected)
        buttonDitolak.setTextColor(Color.BLACK)

        selectedButton.setBackgroundResource(R.drawable.button_filter_selected)
        selectedButton.setTextColor(Color.WHITE)
    }

    private fun filterList(status: StatusDispensasi?, searchQuery: String, selectedKelas: String) {
        val filteredList = allDispensasiList.filter { dispensasi ->
            val statusMatch = status == null || dispensasi.status == status

            val nameMatch = searchQuery.isEmpty() ||
                    dispensasi.namaSiswa.contains(searchQuery, ignoreCase = true)

            val kelasMatch = when {
                selectedKelas == "Semua Kelas" -> true
                selectedKelas == "Kelas/jurusan" -> true
                else -> dispensasi.kelas.contains(selectedKelas, ignoreCase = true)
            }

            statusMatch && nameMatch && kelasMatch
        }
        adapter.updateList(filteredList)
    }

    private fun setupBackButton() {
        val backButton: ImageButton = findViewById(R.id.imageButton2)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupSearch() {
        searchEditText = findViewById(R.id.searchEditText)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterList(currentFilter, s.toString(), kelasDropdown.text.toString())
            }
        })
    }

    private fun setupKelasDropdown() {
        kelasDropdown = findViewById(R.id.textView45)
        val dropdownButton: ImageButton = findViewById(R.id.imageButton4)

        dropdownButton.setOnClickListener {
            showKelasDropdownDialog()
        }

        kelasDropdown.setOnClickListener {
            showKelasDropdownDialog()
        }
    }

    private fun showKelasDropdownDialog() {
        try {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dropdown_kelas_layout)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.8).toInt(),
                (resources.displayMetrics.heightPixels * 0.6).toInt()
            )

            val listView = dialog.findViewById<ListView>(R.id.listViewKelas)
            val adapter = ArrayAdapter(this, R.layout.dropdown_kelas, kelasList)
            listView.adapter = adapter

            listView.setOnItemClickListener { _, _, position, _ ->
                val selectedKelas = kelasList[position]
                kelasDropdown.text = selectedKelas
                filterList(currentFilter, searchEditText.text.toString(), selectedKelas)
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDetailDialog(dispensasi: Dispensasi) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.detail_persetujuan_dispensasi)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            (resources.displayMetrics.heightPixels * 0.85).toInt()
        )

        dialog.findViewById<TextView>(R.id.textView)?.text = dispensasi.namaSiswa
        dialog.findViewById<TextView>(R.id.textView2)?.text = dispensasi.hari
        dialog.findViewById<TextView>(R.id.textView3)?.text = dispensasi.tanggal
        dialog.findViewById<TextView>(R.id.textView19)?.text = dispensasi.jamKe
        dialog.findViewById<TextView>(R.id.textView20)?.text = dispensasi.kelas
        dialog.findViewById<TextView>(R.id.textView21)?.text = dispensasi.mataPelajaran
        dialog.findViewById<TextView>(R.id.textView22)?.text = dispensasi.guruPengajar
        dialog.findViewById<TextView>(R.id.textView24)?.text = dispensasi.catatan

        dialog.findViewById<Button>(R.id.button)?.setOnClickListener {
            val index = allDispensasiList.indexOf(dispensasi)
            if (index != -1) {
                allDispensasiList[index] = dispensasi.copy(status = StatusDispensasi.DISETUJUI)
                filterList(currentFilter, searchEditText.text.toString(), kelasDropdown.text.toString())
                Toast.makeText(this, "Dispensasi ${dispensasi.namaSiswa} disetujui", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.button6)?.setOnClickListener {
            val index = allDispensasiList.indexOf(dispensasi)
            if (index != -1) {
                allDispensasiList[index] = dispensasi.copy(status = StatusDispensasi.DITOLAK)
                filterList(currentFilter, searchEditText.text.toString(), kelasDropdown.text.toString())
                Toast.makeText(this, "Dispensasi ${dispensasi.namaSiswa} ditolak", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}