package com.example.ritamesa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.HomeroomAttendanceItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranKelasActivity : AppCompatActivity() {

    private lateinit var recyclerRiwayat: RecyclerView
    private lateinit var txtHadirCount: TextView
    private lateinit var txtIzinCount: TextView
    private lateinit var txtSakitCount: TextView
    private lateinit var txtAlphaCount: TextView
    private lateinit var txtFilterTanggal: TextView
    private lateinit var txtJumlahSiswa: TextView
    
    // Buttons
    private lateinit var btnHadir: ImageButton
    private lateinit var btnSakit: ImageButton
    private lateinit var btnIzin: ImageButton
    private lateinit var btnAlpha: ImageButton
    
    // Bottom Nav
    private lateinit var btnHome: ImageButton
    private lateinit var btnChart: ImageButton
    private lateinit var btnNotif: ImageButton

    private val allData = mutableListOf<HomeroomAttendanceItem>()
    private val filteredData = mutableListOf<HomeroomAttendanceItem>()
    private lateinit var adapter: RiwayatKelasAdapter
    
    private var selectedDate = Calendar.getInstance()
    private var filterStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.riwayat_kehadiran_kelas)

        initViews()
        setupListeners()
        setupRecyclerView()
        
        updateTanggalDisplay()
        loadDataFromApi()
    }

    private fun initViews() {
        recyclerRiwayat = findViewById(R.id.recycler_riwayat)
        txtHadirCount = findViewById(R.id.txt_hadir_count)
        txtIzinCount = findViewById(R.id.txt_izin_count)
        txtSakitCount = findViewById(R.id.txt_sakit_count)
        txtAlphaCount = findViewById(R.id.txt_alpha_count)
        txtFilterTanggal = findViewById(R.id.text_filter_tanggal)
        txtJumlahSiswa = findViewById(R.id.text_jumlah_siswa)

        btnHadir = findViewById(R.id.button_hadir)
        btnSakit = findViewById(R.id.button_sakit)
        btnIzin = findViewById(R.id.button_izin)
        btnAlpha = findViewById(R.id.button_alpha)

        btnHome = findViewById(R.id.btnHome)
        btnChart = findViewById(R.id.btnChart)
        btnNotif = findViewById(R.id.btnNotif)
    }

    private fun setupListeners() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, month)
            selectedDate.set(Calendar.DAY_OF_MONTH, day)
            updateTanggalDisplay()
            loadDataFromApi()
        }

        val showDateDialog = {
            DatePickerDialog(
                this,
                dateSetListener,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        txtFilterTanggal.setOnClickListener { showDateDialog() }
        findViewById<ImageView>(R.id.icon_calendar).setOnClickListener { showDateDialog() }

        btnHadir.setOnClickListener { toggleFilter("present") }
        btnSakit.setOnClickListener { toggleFilter("sick") }
        btnIzin.setOnClickListener { toggleFilter("excused") } // or 'izin'
        btnAlpha.setOnClickListener { toggleFilter("absent") }

        // Nav
        btnHome.setOnClickListener {
            startActivity(Intent(this, DashboardWaliKelasActivity::class.java))
            finish()
        }
        btnChart.setOnClickListener {
            startActivity(Intent(this, TindakLanjutWaliKelasActivity::class.java))
        }
        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotifikasiWaliKelasActivity::class.java))
        }
        
        // Assignment Btn (Reset Filter)
        findViewById<ImageButton>(R.id.btnAssigment).setOnClickListener {
            selectedDate = Calendar.getInstance()
            filterStatus = null
            updateTanggalDisplay()
            loadDataFromApi()
            Toast.makeText(this, "Filter direset ke hari ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTanggalDisplay() {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val formatted = sdf.format(selectedDate.time)
        val finalDate = if (formatted.isNotEmpty()) formatted[0].uppercaseChar() + formatted.substring(1) else formatted
        txtFilterTanggal.text = finalDate
    }

    private fun setupRecyclerView() {
        adapter = RiwayatKelasAdapter(filteredData)
        recyclerRiwayat.layoutManager = LinearLayoutManager(this)
        recyclerRiwayat.adapter = adapter
    }

    private fun loadDataFromApi() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(selectedDate.time)
        
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        apiService.getHomeroomAttendance(fromDate = dateStr, toDate = dateStr).enqueue(object : Callback<List<HomeroomAttendanceItem>> {
            override fun onResponse(
                call: Call<List<HomeroomAttendanceItem>>,
                response: Response<List<HomeroomAttendanceItem>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    allData.clear()
                    allData.addAll(data)
                    applyLocalFilter()
                    updateCounts()
                } else {
                    Toast.makeText(this@RiwayatKehadiranKelasActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<HomeroomAttendanceItem>>, t: Throwable) {
                Toast.makeText(this@RiwayatKehadiranKelasActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun toggleFilter(status: String) {
        if (filterStatus == status) {
            filterStatus = null
            resetButtonStates()
        } else {
            filterStatus = status
            updateButtonStates(status)
        }
        applyLocalFilter()
    }
    
    private fun applyLocalFilter() {
        filteredData.clear()
        if (filterStatus == null) {
            filteredData.addAll(allData)
        } else {
            filteredData.addAll(allData.filter { 
                val s = it.status.toLowerCase()
                when(filterStatus) {
                    "present" -> s == "present" || s == "late" || s == "hadir"
                    "sick" -> s == "sick" || s == "sakit"
                    "excused" -> s == "excused" || s == "izin" || s == "permit"
                    "absent" -> s == "absent" || s == "alpha"
                    else -> false
                }
            })
        }
        adapter.notifyDataSetChanged()
        txtJumlahSiswa.text = "Total Data: ${filteredData.size}"
    }

    private fun updateCounts() {
        var h = 0; var s = 0; var i = 0; var a = 0
        allData.forEach {
            val status = it.status.toLowerCase()
            if (status == "present" || status == "late" || status == "hadir") h++
            else if (status == "sick" || status == "sakit") s++
            else if (status == "excused" || status == "izin" || status == "permit") i++
            else if (status == "absent" || status == "alpha") a++
        }
        txtHadirCount.text = h.toString()
        txtSakitCount.text = s.toString()
        txtIzinCount.text = i.toString()
        txtAlphaCount.text = a.toString()
    }
    
    private fun updateButtonStates(activeStatus: String) {
        resetButtonStates()
        when(activeStatus) {
            "present" -> btnHadir.setImageResource(R.drawable.btn_guru_hadir_active)
            "sick" -> btnSakit.setImageResource(R.drawable.btn_guru_sakit_active)
            "excused" -> btnIzin.setImageResource(R.drawable.btn_guru_izin_active)
            "absent" -> btnAlpha.setImageResource(R.drawable.btn_guru_alpha_active)
        }
    }
    
    private fun resetButtonStates() {
        btnHadir.setImageResource(R.drawable.btn_guru_hadir)
        btnSakit.setImageResource(R.drawable.btn_guru_sakit)
        btnIzin.setImageResource(R.drawable.btn_guru_izin)
        btnAlpha.setImageResource(R.drawable.btn_guru_alpha)
    }

    // Inner Adapter Class
    inner class RiwayatKelasAdapter(private val dataList: List<HomeroomAttendanceItem>) : RecyclerView.Adapter<RiwayatKelasAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtNama: TextView = itemView.findViewById(R.id.MataPelajaran)
            val txtKet: TextView = itemView.findViewById(R.id.TextKeteranganAbsen) 
            val imgStatus: ImageView = itemView.findViewById(R.id.BadgeKehadiran)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // Using existing layout if possible
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_kehadiran_siswa, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataList[position]
            
            // Re-mapping to item_riwayat_kehadiran_siswa IDs for now:
            val txtName = holder.itemView.findViewById<TextView>(R.id.MataPelajaran) // Use Subject/Title field for Name
            val txtSession = holder.itemView.findViewById<TextView>(R.id.Session) // Use Session for Class/Time
            val txtKet = holder.itemView.findViewById<TextView>(R.id.TextKeteranganAbsen)
            val imgBadge = holder.itemView.findViewById<ImageView>(R.id.BadgeKehadiran)

            txtName.text = item.student?.user?.name ?: "Siswa"
            txtSession.text = item.schedule?.subjectInfo?.name ?: "-" // Show Subject
            
            val s = item.status.toLowerCase()
            val statusIndo = when(s) {
                "present" -> "Hadir"
                "sick" -> "Sakit"
                "excused" -> "Izin"
                "absent" -> "Alpha"
                else -> "Hadir"
            }
            txtKet.text = statusIndo
            
            when(s) {
                 "present" -> imgBadge.setImageResource(R.drawable.siswa_hadir_wakel)
                 "sick" -> imgBadge.setImageResource(R.drawable.siswa_sakit_wakel)
                 "excused" -> imgBadge.setImageResource(R.drawable.siswa_izin_wakel)
                 "absent" -> imgBadge.setImageResource(R.drawable.siswa_alpha_wakel)
                 else -> imgBadge.setImageResource(R.drawable.siswa_hadir_wakel)
            }
        }

        override fun getItemCount() = dataList.size
    }
}