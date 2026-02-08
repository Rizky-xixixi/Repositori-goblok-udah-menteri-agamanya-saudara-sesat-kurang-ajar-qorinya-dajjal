package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginLanjut : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginLanjut"
    }

    private lateinit var selectedRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_lanjut)

        // Ambil role dari intent - menggunakan konstanta dari LoginAwal
        selectedRole = intent.getStringExtra(LoginAwal.EXTRA_ROLE) ?: ""
        Log.d(TAG, "Selected role: $selectedRole")

        // Atur UI berdasarkan role
        setupUIByRole()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupLoginButton()
    }

    private fun setupUIByRole() {
        val textEmail = findViewById<TextView>(R.id.textView5)
        val edtEmail = findViewById<EditText>(R.id.edtNama)
        val textPassword = findViewById<TextView>(R.id.textView8)
        val edtPassword = findViewById<EditText>(R.id.edtPass)

        when (selectedRole) {
            "Admin" -> {
                // Admin: Email & Kata Sandi
                textEmail.text = "Email"
                edtEmail.hint = "Masukkan Email Anda"
                textPassword.visibility = View.VISIBLE
                edtPassword.visibility = View.VISIBLE
                textPassword.text = "Kata Sandi"  // DIUBAH DARI "Password" KE "Kata Sandi"
            }

            "Waka", "Guru", "Wali Kelas" -> {
                // Waka, Guru, Wali: Kode Guru & Kata Sandi
                textEmail.text = "Kode Guru"
                edtEmail.hint = "Masukkan Kode Guru"
                textPassword.visibility = View.VISIBLE
                edtPassword.visibility = View.VISIBLE
                textPassword.text = "Kata Sandi"  // DIUBAH DARI "Password" KE "Kata Sandi"
            }

            "Siswa", "Pengurus" -> {
                // Siswa & Pengurus: Hanya NISN
                textEmail.text = "NISN"
                edtEmail.hint = "Masukkan NISN"
                textPassword.visibility = View.GONE
                edtPassword.visibility = View.GONE
            }

            else -> {
                // Default: tampilkan semua
                textEmail.text = "Email"
                edtEmail.hint = "Masukkan Email/Kode/NISN"
                textPassword.text = "Kata Sandi"  // DIUBAH DARI "Password" KE "Kata Sandi"
            }
        }
    }

    private fun setupLoginButton() {
        val edtUsername = findViewById<EditText>(R.id.edtNama)
        val edtPassword = findViewById<EditText>(R.id.edtPass)
        val btnMasuk = findViewById<Button>(R.id.btnMasuk)

        btnMasuk.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }
    }

    private fun performLogin(emailOrId: String, password: String) {
        val apiService = com.example.ritamesa.data.api.ApiClient.getClient(this).create(com.example.ritamesa.data.api.ApiService::class.java)
        val request = com.example.ritamesa.data.model.LoginRequest(emailOrId, password)

        // Show loading (optional: add progress bar)
        Toast.makeText(this, "Sedang masuk...", Toast.LENGTH_SHORT).show()

        apiService.login(request).enqueue(object : retrofit2.Callback<com.example.ritamesa.data.model.LoginResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.ritamesa.data.model.LoginResponse>,
                response: retrofit2.Response<com.example.ritamesa.data.model.LoginResponse>
            ) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        saveSessionAndNavigate(loginResponse)
                    } else {
                        Toast.makeText(this@LoginLanjut, "Respon kosong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginLanjut, "Login gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.ritamesa.data.model.LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginLanjut, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Login error", t)
            }
        })
    }

    private fun saveSessionAndNavigate(response: com.example.ritamesa.data.model.LoginResponse) {
        val sessionManager = com.example.ritamesa.data.pref.SessionManager(this)
        sessionManager.saveAuthToken(response.access_token)
        sessionManager.saveUserRole(response.user.role)
        sessionManager.saveUserDetails(response.user.id.toString(), response.user.name)
        sessionManager.savePhotoUrl(response.user.profile?.photoUrl)
        sessionManager.setLoggedIn(true)

        Toast.makeText(this, "Login berhasil sebagai ${response.user.role}", Toast.LENGTH_SHORT).show()

        // Sesuaikan navigasi dengan role dari backend
        // Backend roles: admin, teacher, student
        when (response.user.role) {
            "admin" -> navigateToAdminDashboard()
            "teacher" -> {
                 // Cek selectedRole dari halaman sebelumnya untuk membedakan Guru/Wali/Waka
                 // atau biarkan user memilih dashboard?
                 // Untuk simplifikasi, kita arahkan ke Dashboard Guru dulu
                 // Nanti bisa dikembangkan logika Waka/WaliKelas jika backend support role detail
                 if (selectedRole == "Wali Kelas") navigateToWaliKelasDashboard()
                 else if (selectedRole == "Waka") navigateToWakaDashboard()
                 else navigateToGuruDashboard()
            }
            "student" -> {
                 val isPengurus = selectedRole == "Pengurus" // Atau cek dari response jika ada flag pengurus
                 navigateToSiswaDashboard(isPengurus)
            }
            else -> Toast.makeText(this, "Role tidak dikenali: ${response.user.role}", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi navigasi tetap sama seperti sebelumnya
    private fun navigateToAdminDashboard() {
        try {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka dashboard admin", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to open admin dashboard: ${e.message}", e)
        }
    }

    private fun navigateToGuruDashboard() {
        try {
            val intent = Intent(this, DashboardGuruActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka dashboard guru", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to open guru dashboard: ${e.message}", e)
        }
    }

    private fun navigateToWaliKelasDashboard() {
        try {
            val intent = Intent(this, DashboardWaliKelasActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka dashboard wali kelas", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to open wali kelas dashboard: ${e.message}", e)
        }
    }

    private fun navigateToWakaDashboard() {
        try {
            val intent = Intent(this, DashboardWaka::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka dashboard waka", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to open waka dashboard: ${e.message}", e)
        }
    }

    private fun navigateToSiswaDashboard(isPengurus: Boolean) {
        try {
            val intent = Intent(this, DashboardSiswaActivity::class.java)
            intent.putExtra("IS_PENGURUS", isPengurus)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka dashboard siswa", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to open siswa dashboard: ${e.message}", e)
        }
    }

    // Remove legacy validation methods
}