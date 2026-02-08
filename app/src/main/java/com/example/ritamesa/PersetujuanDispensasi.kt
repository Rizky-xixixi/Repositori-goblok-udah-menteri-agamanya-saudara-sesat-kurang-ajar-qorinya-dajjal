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
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.AbsenceRequestItem
import com.example.ritamesa.data.model.AbsenceRequestResponse
import com.example.ritamesa.data.model.GeneralResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Enum to match existing usage if needed, or map strings
enum class StatusDispensasi {
    MENUNGGU, DISETUJUI, DITOLAK
}

// Wrapper for adapter compatibility if we don't change adapter
data class Dispensasi(
    val id: Int,
    val namaSiswa: String,
    val kelas: String,
    val mataPelajaran: String,
    val hari: String,
    val tanggal: String,
    val jamKe: String,
    val guruPengajar: String,
    val catatan: String,
    val status: StatusDispensasi
)

class PersetujuanDispensasi : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DispensasiAdapter
    private var allDispensasiList: MutableList<Dispensasi> = mutableListOf()
    private var currentFilter: StatusDispensasi? = null
    private lateinit var searchEditText: EditText
    private lateinit var kelasDropdown: TextView
    private lateinit var apiService: ApiService

    private val kelasList = listOf(
        "Semua Kelas",
        "X RPL 1", "X RPL 2", "X RPL 3",
        "XI RPL 1", "XI RPL 2", "XI RPL 3",
        "XII RPL 1", "XII RPL 2", "XII RPL 3",
        "XII TKJ 1", "XII TKJ 2",
        "XII IPA 1", "XII IPA 2",
        "XII IPS 1", "XII IPS 2",
        "XII Mekatronika 1"
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

        apiService = ApiClient.getClient(this).create(ApiService::class.java)

        setupRecyclerView()
        setupFilterButtons()
        setupBackButton()
        setupSearch()
        setupKelasDropdown()
        
        fetchAbsenceRequests()
    }
    
    private fun fetchAbsenceRequests() {
        val pd = android.app.ProgressDialog(this)
        pd.setMessage("Memuat data...")
        pd.show()
        
        apiService.getAbsenceRequests().enqueue(object : Callback<AbsenceRequestResponse> {
            override fun onResponse(call: Call<AbsenceRequestResponse>, response: Response<AbsenceRequestResponse>) {
                pd.dismiss()
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    allDispensasiList.clear()
                    
                    data.forEach { item ->
                         val statusEnum = when(item.status) {
                            "approved" -> StatusDispensasi.DISETUJUI
                            "rejected" -> StatusDispensasi.DITOLAK
                            else -> StatusDispensasi.MENUNGGU
                        }
                        
                        allDispensasiList.add(Dispensasi(
                            id = item.id,
                            namaSiswa = item.studentName,
                            kelas = item.className,
                            mataPelajaran = item.schedule?.subjectInfo?.name ?: "-",
                            hari = "-", // Parse from date if needed
                            tanggal = item.date,
                            jamKe = "${item.schedule?.startTime} - ${item.schedule?.endTime}",
                            guruPengajar = item.schedule?.teacher?.user?.name ?: "-",
                            catatan = item.reason ?: "-",
                            status = statusEnum
                        ))
                    }
                    // Initial filter
                    filterList(currentFilter, searchEditText.text.toString(), kelasDropdown.text.toString())
                } else {
                    Toast.makeText(this@PersetujuanDispensasi, "Gagal memuat data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AbsenceRequestResponse>, t: Throwable) {
                pd.dismiss()
                Toast.makeText(this@PersetujuanDispensasi, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewDispensasi)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DispensasiAdapter(mutableListOf()) { dispensasi ->
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
        // Set default text safely
        if (kelasDropdown.text.toString() == "Kelas/jurusan") {
             kelasDropdown.text = "Semua Kelas"
        }

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
            // dialog.window?.setLayout(...) // Use layout vars if needed

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
            processApproval(dispensasi, true, dialog)
        }

        dialog.findViewById<Button>(R.id.button6)?.setOnClickListener {
            processApproval(dispensasi, false, dialog)
        }

        dialog.show()
    }
    
    private fun processApproval(dispensasi: Dispensasi, isApproved: Boolean, dialog: Dialog) {
        val pd = android.app.ProgressDialog(this)
        pd.setMessage("Memproses...")
        pd.show()
        
        val call = if (isApproved) apiService.approveAbsence(dispensasi.id) else apiService.rejectAbsence(dispensasi.id)
        
        call.enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                pd.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(this@PersetujuanDispensasi, "Berhasil " + (if(isApproved) "disetujui" else "ditolak"), Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    fetchAbsenceRequests() // Refresh list
                } else {
                    Toast.makeText(this@PersetujuanDispensasi, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                pd.dismiss()
                Toast.makeText(this@PersetujuanDispensasi, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
