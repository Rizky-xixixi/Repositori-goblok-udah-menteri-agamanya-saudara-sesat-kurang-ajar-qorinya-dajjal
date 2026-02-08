package com.example.ritamesa

import android.app.DatePickerDialog
import android.os.Bundle
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
import com.example.ritamesa.data.model.SchoolAttendanceResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranSiswa : AppCompatActivity() {

    // ===== DATA CLASS =====
    data class KehadiranItem(
        val id: Int,
        val nama: String,
        val role: String, // "Guru", "Wali Kelas", "Siswa"
        val status: String, // "hadir", "terlambat", "izin", "sakit", "alpha"
        val waktu: String,
        val tanggal: String,
        val keterangan: String,
        val statusDetail: String // "Tepat Waktu", "Terlambat", "Izin", "Sakit", "Alpha"
    )

    // ===== ADAPTER =====
    inner class KehadiranAdapter(
        private var listKehadiran: List<KehadiranItem>
    ) : RecyclerView.Adapter<KehadiranAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textTanggal: TextView = view.findViewById(R.id.text_tanggal)
            val textWaktu: TextView = view.findViewById(R.id.text_waktu)
            val textNama: TextView = view.findViewById(R.id.text_nama)
            val textRole: TextView = view.findViewById(R.id.text_role)
            val textStatus: TextView = view.findViewById(R.id.text_status)
            val textKeterangan: TextView = view.findViewById(R.id.text_keterangan)
            val rootView: View = view.findViewById(R.id.root_item)
        }

        fun updateData(newList: List<KehadiranItem>) {
            listKehadiran = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_kehadiran, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = listKehadiran[position]

            holder.textTanggal.text = item.tanggal
            holder.textWaktu.text = item.waktu
            holder.textNama.text = item.nama
            holder.textRole.text = item.role
            holder.textStatus.text = item.statusDetail
            holder.textKeterangan.text = item.keterangan

            // Set warna teks status
            holder.textStatus.setTextColor(when (item.statusDetail) {
                "Tepat Waktu", "Hadir" -> holder.itemView.context.getColor(R.color.status_hadir)
                "Terlambat" -> holder.itemView.context.getColor(R.color.status_terlambat)
                "Izin" -> holder.itemView.context.getColor(R.color.status_izin)
                "Sakit" -> holder.itemView.context.getColor(R.color.status_sakit)
                "Alpha" -> holder.itemView.context.getColor(R.color.status_alpha)
                "Pulang" -> holder.itemView.context.getColor(R.color.status_pulang)
                else -> holder.itemView.context.getColor(R.color.black)
            })

            holder.rootView.setOnClickListener {
                Toast.makeText(
                    this@RiwayatKehadiranSiswa,
                    "${item.nama}: ${item.statusDetail}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun getItemCount(): Int = listKehadiran.size
    }

    // ===== COMPONENTS =====
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: KehadiranAdapter
    private lateinit var tvTotalTitle: TextView
    private lateinit var tvTotalNumber: TextView
    private lateinit var tvHadirValue: TextView
    private lateinit var tvTerlambatValue: TextView
    private lateinit var tvIzinValue: TextView
    private lateinit var tvSakitValue: TextView
    private lateinit var tvAlphaValue: TextView
    private lateinit var textStatus: TextView
    private lateinit var textRole: TextView
    private lateinit var textTanggal: TextView
    private lateinit var btnDropdownStatus: ImageButton
    private lateinit var btnDropdownRole: ImageButton
    private lateinit var btnCalendar: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnMenu: ImageButton

    // ===== STATE =====
    private var currentStatus = "Semua" // API param
    private var currentRole = "Semua"   // API param
    private var currentDate = Calendar.getInstance() // API param

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.riwayat_kehadiran_siswa)

        initViews()
        setupRecyclerView()
        setupFilters()
        setupNavigation()
        
        loadDataFromApi()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvRiwayatKehadiran)
        tvTotalTitle = findViewById(R.id.tvTotalTitle)
        tvTotalNumber = findViewById(R.id.tvTotalNumber)
        tvHadirValue = findViewById(R.id.tvHadirValue)
        tvTerlambatValue = findViewById(R.id.tvTerlambatValue)
        tvIzinValue = findViewById(R.id.tvIzinValue)
        tvSakitValue = findViewById(R.id.tvSakitValue)
        tvAlphaValue = findViewById(R.id.tvAlphaValue)

        textStatus = findViewById(R.id.textStatus)
        textRole = findViewById(R.id.textRole)
        textTanggal = findViewById(R.id.textTanggal)
        btnDropdownStatus = findViewById(R.id.btnDropdownStatus)
        btnDropdownRole = findViewById(R.id.btnDropdownRole)
        btnCalendar = findViewById(R.id.btnCalendar)
        btnBack = findViewById(R.id.btnBack)
        btnMenu = findViewById(R.id.btnMenu)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        textTanggal.text = dateFormat.format(currentDate.time)

        textRole.text = "Semua"
        tvTotalTitle.text = "Total Data"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter = KehadiranAdapter(emptyList())
        recyclerView.adapter = adapter
    }

    private fun setupFilters() {
        btnDropdownStatus.setOnClickListener { showStatusPopupMenu() }
        textStatus.setOnClickListener { showStatusPopupMenu() }

        btnDropdownRole.setOnClickListener { showRolePopupMenu() }
        textRole.setOnClickListener { showRolePopupMenu() }

        btnCalendar.setOnClickListener { showDatePickerDialog() }
        textTanggal.setOnClickListener { showDatePickerDialog() }
    }

    private fun setupNavigation() {
        btnBack.setOnClickListener { finish() }
        btnMenu.setOnClickListener { showExportImportMenu() }
    }

    private fun showStatusPopupMenu() {
        val statusList = arrayOf("Semua", "hadir", "terlambat", "izin", "sakit", "alpha") // Match API values

        AlertDialog.Builder(this)
            .setTitle("Pilih Status Kehadiran")
            .setItems(statusList) { _, which ->
                val selected = statusList[which]
                textStatus.text = selected.capitalize(Locale.getDefault())
                currentStatus = selected
                loadDataFromApi()
            }
            .show()
    }

    private fun showRolePopupMenu() {
        val roleList = arrayOf("Semua", "Guru", "Siswa")

        AlertDialog.Builder(this)
            .setTitle("Pilih Peran")
            .setItems(roleList) { _, which ->
                val selected = roleList[which]
                textRole.text = selected
                currentRole = selected

                if (selected == "Semua") {
                    tvTotalTitle.text = "Total Data"
                } else {
                    tvTotalTitle.text = "Total $selected"
                }

                loadDataFromApi()
            }
            .show()
    }

    private fun showDatePickerDialog() {
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                currentDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                textTanggal.text = dateFormat.format(currentDate.time)
                loadDataFromApi()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun loadDataFromApi() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        Toast.makeText(this, "Memuat data...", Toast.LENGTH_SHORT).show()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(currentDate.time)
        
        // Map local status to API status if needed, or pass directly
        val apiStatus = if (currentStatus == "Semua") null else currentStatus
        val apiRole = if (currentRole == "Semua") null else currentRole

        apiService.getSchoolAttendanceHistory(
            date = dateStr,
            status = apiStatus,
            role = apiRole,
            page = 1 // Fetch page 1 for now (pagination could be added later)
        ).enqueue(object : Callback<SchoolAttendanceResponse> {
            override fun onResponse(
                call: Call<SchoolAttendanceResponse>,
                response: Response<SchoolAttendanceResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val items = data.data.map { apiItem ->
                        val name = if (apiItem.attendeeType == "student") {
                            apiItem.student?.user?.name ?: "Siswa"
                        } else {
                            apiItem.teacher?.user?.name ?: "Guru"
                        }
                        
                        val roleDisplay = if (apiItem.attendeeType == "student") "Siswa" else "Guru"
                        
                        // Parse time
                        val timeStr = apiItem.checkedInTime?.substringAfter(" ")?.substringBeforeLast(":") ?: "-"
                        
                        // Parse date for display
                        val dateDisplay = try {
                           val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                           val sdfOut = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
                           val dateObj = sdfIn.parse(apiItem.date ?: dateStr)
                           sdfOut.format(dateObj!!)
                        } catch (e: Exception) {
                            apiItem.date ?: dateStr
                        }

                        val statusLabel = when (apiItem.status) {
                            "present" -> "Hadir"
                            "late" -> "Terlambat"
                            "sick" -> "Sakit"
                            "excused" -> "Izin"
                            "absent" -> "Alpha"
                            "dinas" -> "Dinas"
                            else -> apiItem.status.capitalize()
                        }
                        
                        val detailLabel = when (apiItem.status) {
                            "present" -> "Tepat Waktu"
                            "late" -> "Terlambat"
                            "sick" -> "Sakit"
                            "excused" -> "Izin"
                            "absent" -> "Alpha"
                             else -> "-"
                        }

                        KehadiranItem(
                            id = apiItem.id,
                            nama = name,
                            role = roleDisplay,
                            status = if (apiItem.status == "present") "hadir" else apiItem.status,
                            waktu = timeStr,
                            tanggal = dateDisplay,
                            keterangan = if (apiItem.status == "late") "Terlambat" else statusLabel,
                            statusDetail = detailLabel
                        )
                    }
                    
                    adapter.updateData(items)
                    updateStatistics(items) // Update stats based on loaded page (limited to 20 for now)
                    
                    tvTotalNumber.text = data.data.size.toString() + "+" // Indicate more data might exist
                    
                } else {
                    Toast.makeText(this@RiwayatKehadiranSiswa, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SchoolAttendanceResponse>, t: Throwable) {
                Toast.makeText(this@RiwayatKehadiranSiswa, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateStatistics(list: List<KehadiranItem>) {
        // Note: This only counts visible items (pagination limitation)
        // For full stats, we should use the Dashboard API stats, but for now this gives immediate feedback on list
        val hadir = list.count { it.status == "hadir" || it.status == "present" }
        val terlambat = list.count { it.status == "late" || it.statusDetail == "Terlambat" }
        val izin = list.count { it.status == "excused" || it.status == "izin" || it.statusDetail == "Izin" }
        val sakit = list.count { it.status == "sick" || it.status == "sakit" || it.statusDetail == "Sakit" }
        val alpha = list.count { it.status == "absent" || it.status == "alpha" || it.statusDetail == "Alpha" }
        
        tvHadirValue.text = hadir.toString()
        tvTerlambatValue.text = terlambat.toString()
        tvIzinValue.text = izin.toString()
        tvSakitValue.text = sakit.toString()
        tvAlphaValue.text = alpha.toString()
    }

    private fun showExportImportMenu() {
        val popupMenu = androidx.appcompat.widget.PopupMenu(this, btnMenu)
        popupMenu.menu.add("📤 Export ke Excel")
        popupMenu.menu.add("📥 Import dari Excel")
        popupMenu.menu.add("🖨️ Print Laporan")
        popupMenu.show()
    }
}