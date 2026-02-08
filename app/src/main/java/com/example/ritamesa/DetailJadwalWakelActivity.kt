package com.example.ritamesa

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class DetailJadwalWakelActivity : AppCompatActivity() {

    private lateinit var currentJadwal: DashboardGuruActivity.JadwalData
    private var studentList: List<com.example.ritamesa.data.model.StudentItem> = emptyList()

    // Untuk QR Scanner result
    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val isSuccess = data?.getBooleanExtra(CameraQRActivity.EXTRA_QR_RESULT, false) ?: false

            if (isSuccess) {
                val kelas = data.getStringExtra(CameraQRActivity.EXTRA_KELAS) ?: "-"
                val mapel = data.getStringExtra(CameraQRActivity.EXTRA_MAPEL) ?: "-"
                val tanggal = data.getStringExtra("tanggal") ?: "-"
                val jam = data.getStringExtra("jam") ?: "-"

                Toast.makeText(
                    this,
                    "Absensi berhasil!\n$mapel - $kelas\n$tanggal $jam",
                    Toast.LENGTH_LONG
                ).show()

                // LANGSUNG KE ABSENSI SISWA ACTIVITY SETELAH QR SCAN SUKSES
                val intent = Intent(this, AbsensiSiswaActivity::class.java).apply {
                    putExtra(CameraQRActivity.EXTRA_MAPEL, mapel)
                    putExtra(CameraQRActivity.EXTRA_KELAS, kelas)
                    putExtra("CLASS_ID", currentJadwal.classId)
                    putExtra("SCHEDULE_ID", currentJadwal.id)
                    putExtra("tanggal", tanggal)
                    putExtra("jam", jam)
                }
                startActivity(intent)

            } else {
                Toast.makeText(this, "Gagal scan QR", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_jadwal_wakel)

        val jadwalData = intent.getSerializableExtra("JADWAL_DATA") as? DashboardGuruActivity.JadwalData

        // Inisialisasi view
        val tvNamaMapel: TextView = findViewById(R.id.text_nama_mapel)
        val tvKelas: TextView = findViewById(R.id.title_kelas)
        val tvTanggalWaktu: TextView = findViewById(R.id.tanggal_waktu_mulai)
        val tvMapelDetail: TextView = findViewById(R.id.txt_end_1)
        val tvKelasDetail: TextView = findViewById(R.id.txt_end_2)
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        val btnAbsensi: ImageButton = findViewById(R.id.btn_absensi)
        val btnTidakMengajar1: ImageButton = findViewById(R.id.btn_tidak_mengajar_1)
        val btnTidakMengajar2: ImageButton = findViewById(R.id.btn_tidak_mengajar_2)
        val btnIzinSakit: ImageButton = findViewById(R.id.btn_izin_sakit)

        // Set data dari intent
        jadwalData?.let {
            currentJadwal = it
            tvNamaMapel.text = it.mataPelajaran
            tvKelas.text = it.kelas
            tvTanggalWaktu.text = "${formatTanggalWaktu(it.jam)}"
            tvMapelDetail.text = it.mataPelajaran
            tvKelasDetail.text = it.kelas

            // Fetch real student count
            if (it.classId != null && it.classId != -1) {
                fetchClassDetail(it.classId)
            } else {
                 findViewById<TextView>(R.id.txt_end_3).text = "-"
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnAbsensi.setOnClickListener {
            showAbsensiPopup()
        }

        btnTidakMengajar1.setOnClickListener {
            showTidakMengajarPopup()
        }

        btnTidakMengajar2.setOnClickListener {
            showDispensasiPopup()
        }

        btnIzinSakit.setOnClickListener {
            showIzinSakitPopup()
        }

        // TAMBAHAN: Long click untuk langsung ke absensi (testing tanpa QR)
        btnAbsensi.setOnLongClickListener {
            val intent = Intent(this, AbsensiSiswaActivity::class.java).apply {
                putExtra("MATA_PELAJARAN", currentJadwal.mataPelajaran)
                putExtra("KELAS", currentJadwal.kelas)
                putExtra("CLASS_ID", currentJadwal.classId)
                putExtra("SCHEDULE_ID", currentJadwal.id)
                putExtra("JAM", currentJadwal.jam)
                putExtra("TANGGAL", SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
            }
            startActivity(intent)
            true
        }
    }

    private fun fetchClassDetail(classId: Int) {
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)
        apiService.getClassDetail(classId).enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.ClassDetailResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.ritamesa.data.model.ClassDetailResponse>,
                response: retrofit2.Response<com.example.ritamesa.data.model.ClassDetailResponse>
            ) {
                if (response.isSuccessful) {
                    val classDetail = response.body()
                    studentList = classDetail?.students ?: emptyList()
                    findViewById<TextView>(R.id.txt_end_3).text = studentList.size.toString()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.ClassDetailResponse>, t: Throwable) {
                Log.e("DetailJadwal", "Error fetching class detail", t)
            }
        })
    }

    private fun formatTanggalWaktu(jam: String): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val tanggalSekarang = sdf.format(Date())
        return "$jam $tanggalSekarang"
    }

    private fun showAbsensiPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_absensi)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnPindaiQR: Button = dialog.findViewById(R.id.btn_pindaiqr)
        val btnKembali: Button = dialog.findViewById(R.id.btn_kembali)

        btnPindaiQR.setOnClickListener {
            dialog.dismiss()
            startQRScanner()
        }

        btnKembali.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startQRScanner() {
        val intent = Intent(this, CameraQRActivity::class.java)
        qrScannerLauncher.launch(intent)
    }

    private fun showTidakMengajarPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_tidak_mengajar)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set data otomatis dari jadwal
        val tvNamaMapel: TextView = dialog.findViewById(R.id.tv_nama_mapel)
        val tvKelas: TextView = dialog.findViewById(R.id.tv_kelas)
        val inputKeterangan: EditText = dialog.findViewById(R.id.input_keterangan)
        val inputJam: EditText = dialog.findViewById(R.id.input_mapel)
        val inputTanggal: EditText = dialog.findViewById(R.id.input_tanggal)
        val etCatatan: EditText = dialog.findViewById(R.id.et_catatan)
        val btnKirimIzin: Button = dialog.findViewById(R.id.btn_kirim_izin)
        val btnBatalIzin: Button = dialog.findViewById(R.id.btn_batal_izin)

        // Isi data otomatis
        tvNamaMapel.text = "${currentJadwal.mataPelajaran} - "
        tvKelas.text = currentJadwal.kelas

        val sdfShow = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val sdfApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val tanggalSekarang = sdfShow.format(Date())

        inputJam.setText(currentJadwal.jam)
        inputTanggal.setText(tanggalSekarang)

        inputKeterangan.setOnClickListener { showKeteranganDropdown(inputKeterangan) }
        inputJam.setOnClickListener { showJamMapelDropdown(inputJam) }
        inputTanggal.setOnClickListener { showDatePickerDialog(inputTanggal) }

        btnKirimIzin.setOnClickListener {
            val keterangan = inputKeterangan.text.toString()
            val catatan = etCatatan.text.toString()

            if (keterangan.isEmpty()) {
                Toast.makeText(this, "Harap pilih keterangan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val apiStatus = when(keterangan.lowercase()) {
                "sakit" -> "sakit"
                "izin" -> "izin"
                "izin pulang" -> "izin_pulang"
                else -> "izin"
            }

            val apiDate = try {
                val d = sdfShow.parse(inputTanggal.text.toString())
                sdfApi.format(d!!)
            } catch (e: Exception) { sdfApi.format(Date()) }

            val request = com.example.ritamesa.data.model.AbsenceRequestRequest(
                type = apiStatus,
                date = apiDate,
                reason = catatan.ifEmpty { "Izin Guru (Wakel): $keterangan" },
                scheduleId = currentJadwal.id
            )

            submitAbsence(request, dialog)
        }

        btnBatalIzin.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showIzinSakitPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_izin)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inisialisasi komponen
        val inputKeterangan: EditText = dialog.findViewById(R.id.input_keterangan)
        val inputJam: EditText = dialog.findViewById(R.id.input_jam)
        val inputTanggal: EditText = dialog.findViewById(R.id.input_tanggal)
        val etCatatan: EditText = dialog.findViewById(R.id.et_catatan)
        val btnKirimIzin: Button = dialog.findViewById(R.id.btn_kirim_izin)
        val btnBatalIzin: Button = dialog.findViewById(R.id.btn_batal_izin)
        val btnDropdownKeterangan: ImageButton = dialog.findViewById(R.id.btn_dropdown_arrow)

        val sdfShow = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val sdfApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val tanggalSekarang = sdfShow.format(Date())

        inputJam.setText(currentJadwal.jam)
        inputTanggal.setText(tanggalSekarang)

        inputJam.setOnClickListener { showJamDropdown(inputJam) }
        inputTanggal.setOnClickListener { showDatePickerDialog(inputTanggal) }
        inputKeterangan.setOnClickListener { showKeteranganIzinDropdown(inputKeterangan) }
        btnDropdownKeterangan.setOnClickListener { showKeteranganIzinDropdown(inputKeterangan) }

        btnKirimIzin.setOnClickListener {
            val keterangan = inputKeterangan.text.toString()
            val catatan = etCatatan.text.toString()

            if (keterangan.isEmpty()) {
                Toast.makeText(this, "Harap pilih keterangan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val apiStatus = when(keterangan.lowercase()) {
                "sakit" -> "sakit"
                "izin" -> "izin"
                "izin pulang" -> "izin_pulang"
                else -> "izin"
            }

            val apiDate = try {
                val d = sdfShow.parse(inputTanggal.text.toString())
                sdfApi.format(d!!)
            } catch (e: Exception) { sdfApi.format(Date()) }

            val request = com.example.ritamesa.data.model.AbsenceRequestRequest(
                type = apiStatus,
                date = apiDate,
                reason = catatan.ifEmpty { "Izin Guru (Wakel): $keterangan" },
                scheduleId = currentJadwal.id
            )

            submitAbsence(request, dialog)
        }

        btnBatalIzin.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDispensasiPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_dispensasi)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inisialisasi komponen
        val inputNamaSiswa: EditText = dialog.findViewById(R.id.input_keterangan)
        val inputJam: EditText = dialog.findViewById(R.id.input_jam)
        val inputTanggal: EditText = dialog.findViewById(R.id.input_tanggal)
        val etCatatan: EditText = dialog.findViewById(R.id.et_catatan)
        val btnKirimIzin: Button = dialog.findViewById(R.id.btn_kirim_izin)
        val btnBatalIzin: Button = dialog.findViewById(R.id.btn_batal_izin)

        val sdfShow = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val sdfApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val tanggalSekarang = sdfShow.format(Date())

        inputJam.setText(currentJadwal.jam)
        inputTanggal.setText(tanggalSekarang)

        inputJam.setOnClickListener { showJamDropdown(inputJam) }
        inputTanggal.setOnClickListener { showDatePickerDialog(inputTanggal) }
        inputNamaSiswa.setOnClickListener { showSiswaDropdown(inputNamaSiswa) }

        btnKirimIzin.setOnClickListener {
            val namaSiswa = inputNamaSiswa.text.toString()
            val studentId = inputNamaSiswa.tag as? Int 
            val catatan = etCatatan.text.toString()

            if (namaSiswa.isEmpty() || studentId == null) {
                Toast.makeText(this, "Harap pilih siswa dari daftar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val apiDate = try {
                val d = sdfShow.parse(inputTanggal.text.toString())
                sdfApi.format(d!!)
            } catch (e: Exception) { sdfApi.format(Date()) }

            val request = com.example.ritamesa.data.model.AbsenceRequestRequest(
                type = "izin", 
                date = apiDate,
                reason = "Dispensasi Siswa (By Wakel): $namaSiswa. ${catatan.ifEmpty { "" }}".trim(),
                scheduleId = currentJadwal.id,
                studentId = studentId
            )

            submitAbsence(request, dialog)
        }

        btnBatalIzin.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showKeteranganDropdown(editText: EditText) {
        val items = arrayOf("Sakit", "Izin", "Izin Pulang")

        AlertDialog.Builder(this)
            .setTitle("Pilih Keterangan")
            .setItems(items) { _, which ->
                editText.setText(items[which])
            }
            .show()
    }

    private fun showKeteranganIzinDropdown(editText: EditText) {
        val items = arrayOf("Sakit", "Izin", "Izin Pulang")

        AlertDialog.Builder(this)
            .setTitle("Pilih Keterangan")
            .setItems(items) { _, which ->
                editText.setText(items[which])
            }
            .show()
    }

    private fun showJamMapelDropdown(editText: EditText) {
        val items = arrayOf(
            currentJadwal.jam,
            "Tukar jam dengan guru lain",
            "Jam pengganti"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih Jadwal")
            .setItems(items) { _, which ->
                editText.setText(items[which])
            }
            .show()
    }

    private fun showJamDropdown(editText: EditText) {
        // Parse jam dari format "07:30 - 08:15"
        val jamParts = currentJadwal.jam.split(" - ")
        val items = if (jamParts.size == 2) {
            arrayOf(
                currentJadwal.jam,
                "${jamParts[0]} - ${jamParts[1]} (Full)",
                "${jamParts[0]} (Awal)",
                "${jamParts[1]} (Akhir)"
            )
        } else {
            arrayOf(currentJadwal.jam)
        }

        AlertDialog.Builder(this)
            .setTitle("Pilih Jam Mapel")
            .setItems(items) { _, which ->
                editText.setText(items[which])
            }
            .show()
    }

    private fun showSiswaDropdown(editText: EditText) {
        if (studentList.isEmpty()) {
            Toast.makeText(this, "Data siswa belum dimuat", Toast.LENGTH_SHORT).show()
            return
        }

        val items = studentList.map { "${it.name} - ${it.className}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Siswa")
            .setItems(items) { _, which ->
                editText.setText(items[which])
                editText.tag = studentList[which].id // Store student ID in tag
            }
            .show()
    }

    private fun submitAbsence(request: com.example.ritamesa.data.model.AbsenceRequestRequest, dialog: Dialog) {
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)
        
        val progress = android.app.ProgressDialog(this).apply {
            setMessage("Mengirim pengajuan...")
            setCancelable(false)
            show()
        }

        apiService.submitAbsenceRequest(request).enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.GeneralResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.ritamesa.data.model.GeneralResponse>,
                response: retrofit2.Response<com.example.ritamesa.data.model.GeneralResponse>
            ) {
                progress.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(this@DetailJadwalWakelActivity, "Pengajuan berhasil dikirim", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@DetailJadwalWakelActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.GeneralResponse>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@DetailJadwalWakelActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showGuruDropdown(editText: EditText) {
        val guruList = arrayOf(
            "Budi Santoso - Matematika",
            "Siti Rahayu - Bahasa Indonesia",
            "Ahmad Hidayat - IPA",
            "Dewi Lestari - Bahasa Inggris",
            "Joko Widodo - PPKN",
            "Rina Melati - Seni Budaya",
            "Eko Prasetyo - Olahraga",
            "Maya Sari - Sejarah",
            "Fajar Nugroho - Fisika",
            "Lina Marlina - Kimia"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih Guru")
            .setItems(guruList) { _, which ->
                editText.setText(guruList[which])
            }
            .show()
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(Locale.getDefault(),
                    "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                editText.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }
}