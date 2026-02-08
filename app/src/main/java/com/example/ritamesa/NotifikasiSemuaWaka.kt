package com.example.ritamesa

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotifikasiSemuaWaka : AppCompatActivity() {

    data class NotifikasiItem(
        val id: Int,
        val nama: String,
        val pesan: String,
        val detail: String,
        val waktu: String,
        val tipe: String,
        val kategori: String,
        val jamKe: Int = 0,
        val kelasJurusan: String = ""
    )

    inner class NotifikasiAdapter(
        private val listNotifikasi: List<NotifikasiItem>
    ) : RecyclerView.Adapter<NotifikasiAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textAlert: TextView = view.findViewById(R.id.text_alert)
            val jamNotifikasi: TextView = view.findViewById(R.id.jam_notifikasi)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notif_semua_waka, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val notif = listNotifikasi[position]

            holder.textAlert.text = "${notif.nama}: ${notif.pesan}"
            holder.jamNotifikasi.text = "${notif.waktu} (Jam ke-${notif.jamKe})"

            holder.itemView.setOnClickListener {
                showDetailPopup(notif)
            }
        }

        override fun getItemCount(): Int = listNotifikasi.size
    }

    private lateinit var rvHariIni: RecyclerView
    private lateinit var rvKemarin: RecyclerView
    private lateinit var rvBulanLalu: RecyclerView
    private lateinit var layoutSemua: LinearLayout
    private lateinit var layoutJamKe: LinearLayout
    private lateinit var tvPilihKelas: TextView
    private lateinit var tvJamKe: TextView
    private lateinit var btnDropdownJamKe: ImageButton
    private lateinit var tvHariIni: TextView
    private lateinit var tvKemarin: TextView
    private lateinit var tvBulanLalu: TextView

    private var filterKategori = "Semua"
    private var filterJamKe: Int = 0
    private var filterKelasJurusan = "Semua Kelas"
    private var isFilterSemuaActive = true

    private val semuaNotifikasi = mutableListOf<NotifikasiItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notifikasi_semua_waka)

        initViews()
        setupRecyclerViews()
        setupFilterActions()
        setupFooterNavigation()
        setupDateFilter()
        setupJenisFilter()
        
        fetchNotifications()
    }
    
    private fun fetchNotifications() {
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)
        
        apiService.getNotifications().enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.NotificationResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.ritamesa.data.model.NotificationResponse>,
                response: retrofit2.Response<com.example.ritamesa.data.model.NotificationResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()?.notifications ?: emptyList()
                    semuaNotifikasi.clear()
                    
                     val mappedList = data.map { item ->
                        var parsedJamKe = 0
                        var parsedKelas = "-"
                        
                        try {
                            if (item.detail.contains("Jam ke-")) {
                                val jamPart = item.detail.substringAfter("Jam ke-").substringBefore(")")
                                parsedJamKe = jamPart.toIntOrNull() ?: 0
                            }
                            
                            if (item.detail.contains(" - ")) {
                                parsedKelas = item.detail.substringBefore(" - ").trim()
                            } else {
                                parsedKelas = item.detail
                            }
                        } catch (e: Exception) { }

                        NotifikasiItem(
                            id = item.id.hashCode(), 
                            nama = "Sistem", 
                            pesan = item.message,
                            detail = item.detail,
                            waktu = item.time,
                            tipe = item.type,
                            kategori = if (item.type == "dinas" || item.type == "tepat_waktu" || item.type == "terlambat" && item.message.contains("Anda")) "guru" else "siswa",
                            jamKe = parsedJamKe,
                            kelasJurusan = parsedKelas 
                        )
                    }
                    semuaNotifikasi.addAll(mappedList)
                    filterData()
                } else {
                    Toast.makeText(this@NotifikasiSemuaWaka, "Gagal memuat notifikasi", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.NotificationResponse>, t: Throwable) {
                Toast.makeText(this@NotifikasiSemuaWaka, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initViews() {
        rvHariIni = findViewById(R.id.rvNotifHariIni)
        rvKemarin = findViewById(R.id.rvNotifKemarin)
        rvBulanLalu = findViewById(R.id.rvNotifBulanLalu)

        layoutSemua = findViewById(R.id.icon_notif_belum)
        layoutJamKe = findViewById(R.id.icon_notif_semua)
        tvPilihKelas = findViewById(R.id.tvPilihKelas)
        tvJamKe = findViewById(R.id.tvJamKe)
        btnDropdownJamKe = findViewById(R.id.btnDropdownJamKe)

        tvHariIni = findViewById(R.id.tvHariIni)
        tvKemarin = findViewById(R.id.tvKemarin)
        tvBulanLalu = findViewById(R.id.tvBulanLalu)
    }

    private fun setupRecyclerViews() {
        rvHariIni.layoutManager = LinearLayoutManager(this)
        rvKemarin.layoutManager = LinearLayoutManager(this)
        rvBulanLalu.layoutManager = LinearLayoutManager(this)

        rvHariIni.minimumHeight = 300
        rvKemarin.minimumHeight = 200
        rvBulanLalu.minimumHeight = 120
    }

    private fun showDetailPopup(notif: NotifikasiItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.pop_up_detail_notif_tepat_waktu_waka, null)

        val textViewNama = dialogView.findViewById<TextView>(R.id.textViewNama)
        val textViewTanggal = dialogView.findViewById<TextView>(R.id.textViewTanggal)
        val textViewDetail = dialogView.findViewById<TextView>(R.id.textViewDetail)
        val btnTutup = dialogView.findViewById<Button>(R.id.btnTutup)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)

        val dialog = dialogBuilder.create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        val today = Calendar.getInstance().time
        val formattedDate = dateFormat.format(today)

        when (notif.tipe) {
            "tepat_waktu" -> {
                textViewNama.text = notif.nama
                textViewTanggal.text = formattedDate
                textViewDetail.text = """
                    Status: Tepat Waktu
                    Waktu: ${notif.waktu}
                    Role: ${if (notif.kategori == "guru") "Guru" else "Siswa"}
                    Kelas/Jurusan: ${notif.kelasJurusan}
                    Mata Pelajaran: ${notif.detail}
                    Jam ke: ${notif.jamKe}
                """.trimIndent()
            }
            "terlambat" -> {
                textViewNama.text = notif.nama
                textViewTanggal.text = formattedDate
                textViewDetail.text = """
                    Status: Terlambat
                    Waktu: ${notif.waktu}
                    Role: ${if (notif.kategori == "guru") "Guru" else "Siswa"}
                    Kelas/Jurusan: ${notif.kelasJurusan}
                    Detail: ${notif.pesan}
                    Jam ke: ${notif.jamKe}
                """.trimIndent()
            }
            "alpha" -> {
                textViewNama.text = notif.nama
                textViewTanggal.text = formattedDate
                textViewDetail.text = """
                    Status: Alpha/Tidak Hadir
                    Waktu: ${notif.waktu}
                    Role: ${if (notif.kategori == "guru") "Guru" else "Siswa"}
                    Kelas/Jurusan: ${notif.kelasJurusan}
                    Detail: ${notif.pesan}
                    Jam ke: ${notif.jamKe}
                """.trimIndent()
            }
            "sakit" -> {
                textViewNama.text = notif.nama
                textViewTanggal.text = formattedDate
                textViewDetail.text = """
                    Status: Sakit
                    Waktu: ${notif.waktu}
                    Role: ${if (notif.kategori == "guru") "Guru" else "Siswa"}
                    Kelas/Jurusan: ${notif.kelasJurusan}
                    Detail: ${notif.pesan}
                    Jam ke: ${notif.jamKe}
                """.trimIndent()
            }
            "rapor" -> {
                textViewNama.text = notif.nama
                textViewTanggal.text = formattedDate
                textViewDetail.text = """
                    Status: Rapor Kehadiran
                    Waktu: ${notif.waktu}
                    Keterangan: Rapor Tersedia
                    Detail: ${notif.detail}
                    Jam ke: ${notif.jamKe}
                """.trimIndent()
            }
        }

        btnTutup.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupFilterActions() {
        layoutSemua.setOnClickListener {
            activateSemuaFilter()
        }

        layoutJamKe.setOnClickListener {
            activateJamKeFilter()
        }

        btnDropdownJamKe.setOnClickListener {
            showJamKePopupMenu()
        }

        tvJamKe.setOnClickListener {
            showJamKePopupMenu()
        }

        activateSemuaFilter()

        tvPilihKelas.setOnClickListener {
            showKelasJurusanDialog()
        }

        findViewById<ImageButton>(R.id.btnArrowKelas).setOnClickListener {
            showKelasJurusanDialog()
        }
    }

    private fun activateSemuaFilter() {
        isFilterSemuaActive = true
        layoutSemua.setBackgroundResource(R.drawable.rectangle_86)
        layoutJamKe.background = null
        tvJamKe.text = "Jam ke-"
        filterJamKe = 0
        updateSectionTitles()
        filterData()
    }

    private fun activateJamKeFilter() {
        isFilterSemuaActive = false
        layoutJamKe.setBackgroundResource(R.drawable.rectangle_86)
        layoutSemua.background = null

        if (tvJamKe.text == "Jam ke-" || tvJamKe.text == "Jam ke-") {
            showJamKePopupMenu()
        } else {
            filterData()
        }
    }

    private fun showJamKePopupMenu() {
        val popupMenu = PopupMenu(this, btnDropdownJamKe)
        val menu = popupMenu.menu

        for (i in 1..10) {
            menu.add(0, i, 0, "Jam ke $i")
        }

        popupMenu.setOnMenuItemClickListener { item ->
            val jamKe = item.itemId
            filterJamKe = jamKe
            tvJamKe.text = "Jam ke $jamKe"

            activateJamKeFilter()

            true
        }

        popupMenu.show()
    }

    private fun setupDateFilter() {
        val btnCalendar = findViewById<ImageButton>(R.id.btnCalendar)
        val tvTanggal = findViewById<TextView>(R.id.tvTanggal)

        val currentDate = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        tvTanggal.text = dateFormat.format(currentDate.time)

        btnCalendar.setOnClickListener {
            showDatePickerDialog(tvTanggal)
        }

        tvTanggal.setOnClickListener {
            showDatePickerDialog(tvTanggal)
        }
    }

    private fun showDatePickerDialog(tvTanggal: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                tvTanggal.text = dateFormat.format(selectedDate.time)

                filterData()
                Toast.makeText(this, "Filter tanggal: ${tvTanggal.text}", Toast.LENGTH_SHORT).show()
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun setupJenisFilter() {
        val btnArrowFilter = findViewById<ImageButton>(R.id.btnArrowFilter)
        val tvFilter = findViewById<TextView>(R.id.tvFilter)

        btnArrowFilter.setOnClickListener {
            showJenisPopupMenu(tvFilter)
        }

        tvFilter.setOnClickListener {
            showJenisPopupMenu(tvFilter)
        }
    }

    private fun showJenisPopupMenu(tvFilter: TextView) {
        val jenisList = arrayOf("Semua", "Guru", "Siswa", "Tepat Waktu", "Terlambat", "Alpha", "Sakit", "Rapor")

        AlertDialog.Builder(this)
            .setTitle("Pilih Jenis Notifikasi")
            .setItems(jenisList) { _, which ->
                val selected = jenisList[which]
                tvFilter.text = selected
                filterKategori = selected
                filterData()
                Toast.makeText(this, "Filter: $selected", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setupFooterNavigation() {
        try {
            findViewById<View>(R.id.btnHome)?.setOnClickListener {
                try {
                    val intent = Intent(this, DashboardWaka::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal membuka Dashboard", Toast.LENGTH_SHORT).show()
                }
            }

            findViewById<View>(R.id.btnContacts)?.setOnClickListener {
                try {
                    val intent = Intent(this, DataRekapKehadiranGuru::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal membuka Data Rekap Guru", Toast.LENGTH_SHORT).show()
                }
            }

            findViewById<View>(R.id.btnAssignment)?.setOnClickListener {
                try {
                    val intent = Intent(this, JadwalPembelajaranGuru::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal membuka Jadwal Pembelajaran", Toast.LENGTH_SHORT).show()
                }
            }

            findViewById<View>(R.id.btnChart)?.setOnClickListener {
                try {
                    val intent = Intent(this, StatistikWakaa::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal membuka Statistik", Toast.LENGTH_SHORT).show()
                }
            }

            findViewById<View>(R.id.btnNotif)?.setOnClickListener {
                findViewById<ScrollView>(R.id.scrollContent)?.smoothScrollTo(0, 0)
                filterData()
                Toast.makeText(this, "Notifikasi direfresh", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error setup navigasi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterData() {
        var filteredList = semuaNotifikasi

        filteredList = when (filterKategori) {
            "Guru" -> filteredList.filter { it.kategori == "guru" }
            "Siswa" -> filteredList.filter { it.kategori == "siswa" }
            "Tepat Waktu" -> filteredList.filter { it.tipe == "tepat_waktu" }
            "Terlambat" -> filteredList.filter { it.tipe == "terlambat" }
            "Alpha" -> filteredList.filter { it.tipe == "alpha" }
            "Sakit" -> filteredList.filter { it.tipe == "sakit" }
            "Rapor" -> filteredList.filter { it.tipe == "rapor" }
            else -> filteredList
        }

        if (!isFilterSemuaActive && filterJamKe > 0) {
            filteredList = filteredList.filter { it.jamKe == filterJamKe }
        }

        if (filterKelasJurusan != "Semua Kelas") {
            filteredList = filteredList.filter { it.kelasJurusan == filterKelasJurusan }
        }

        val hariIniList = filteredList.take(5)
        val kemarinList = filteredList.drop(5).take(5)
        val bulanLaluList = filteredList.drop(10)

        rvHariIni.adapter = NotifikasiAdapter(hariIniList)
        rvKemarin.adapter = NotifikasiAdapter(kemarinList)
        rvBulanLalu.adapter = NotifikasiAdapter(bulanLaluList)

        updateSectionVisibility(hariIniList, kemarinList, bulanLaluList)
        updateSectionTitlesWithFilter()
    }

    private fun updateSectionVisibility(
        hariIniList: List<NotifikasiItem>,
        kemarinList: List<NotifikasiItem>,
        bulanLaluList: List<NotifikasiItem>
    ) {
        tvHariIni.visibility = if (hariIniList.isEmpty()) View.GONE else View.VISIBLE
        rvHariIni.visibility = if (hariIniList.isEmpty()) View.GONE else View.VISIBLE

        tvKemarin.visibility = if (kemarinList.isEmpty()) View.GONE else View.VISIBLE
        rvKemarin.visibility = if (kemarinList.isEmpty()) View.GONE else View.VISIBLE

        tvBulanLalu.visibility = if (bulanLaluList.isEmpty()) View.GONE else View.VISIBLE
        rvBulanLalu.visibility = if (bulanLaluList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateSectionTitles() {
        if (filterJamKe > 0 && !isFilterSemuaActive) {
            tvHariIni.text = "Jam ke $filterJamKe (Hari Ini)"
            tvKemarin.text = "Jam ke $filterJamKe (Kemarin)"
            tvBulanLalu.text = "Jam ke $filterJamKe (Bulan Lalu)"
        } else {
            tvHariIni.text = "Hari Ini"
            tvKemarin.text = "Kemarin"
            tvBulanLalu.text = "Bulan Lalu"
        }
    }

    private fun updateSectionTitlesWithFilter() {
        var additionalInfo = ""

        if (filterKelasJurusan != "Semua Kelas") {
            additionalInfo = " - $filterKelasJurusan"
        }

        if (filterJamKe > 0 && !isFilterSemuaActive) {
            tvHariIni.text = "Jam ke $filterJamKe (Hari Ini)$additionalInfo"
            tvKemarin.text = "Jam ke $filterJamKe (Kemarin)$additionalInfo"
            tvBulanLalu.text = "Jam ke $filterJamKe (Bulan Lalu)$additionalInfo"
        } else {
            tvHariIni.text = "Hari Ini$additionalInfo"
            tvKemarin.text = "Kemarin$additionalInfo"
            tvBulanLalu.text = "Bulan Lalu$additionalInfo"
        }

        if (filterKategori != "Semua") {
            tvHariIni.text = "${tvHariIni.text} - $filterKategori"
            tvKemarin.text = "${tvKemarin.text} - $filterKategori"
            tvBulanLalu.text = "${tvBulanLalu.text} - $filterKategori"
        }
    }

    private fun showKelasJurusanDialog() {
        val kelasJurusan = arrayOf(
            "Semua Kelas",
            "X RPL 1", "X RPL 2", "X RPL 3",
            "XI RPL 1", "XI RPL 2", "XI RPL 3",
            "XII RPL 1", "XII RPL 2", "XII RPL 3",
            "XII TKJ 1", "XII TKJ 2",
            "XII IPA 1", "XII IPA 2",
            "XII IPS 1", "XII IPS 2",
            "XII Mekatronika 1"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih Kelas/Jurusan")
            .setItems(kelasJurusan) { _, which ->
                val selected = kelasJurusan[which]
                filterKelasJurusan = selected
                tvPilihKelas.text = selected
                filterData()
                Toast.makeText(this, "Filter: $selected", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onBackPressed() {
        try {
            val intent = Intent(this, DashboardWaka::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            super.onBackPressed()
        }
    }
}