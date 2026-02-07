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

            // Validasi berdasarkan role
            when (selectedRole) {
                "Admin" -> {
                    if (username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Email dan kata sandi harus diisi", Toast.LENGTH_SHORT).show()  // DIUBAH
                        return@setOnClickListener
                    }
                    validateAdmin(username, password)
                }

                "Waka", "Guru", "Wali Kelas" -> {
                    if (username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Kode guru dan kata sandi harus diisi", Toast.LENGTH_SHORT).show()  // DIUBAH
                        return@setOnClickListener
                    }
                    validateStaff(username, password)
                }

                "Siswa", "Pengurus" -> {
                    if (username.isEmpty()) {
                        Toast.makeText(this, "NISN harus diisi", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    validateSiswa(username, isPengurus = selectedRole == "Pengurus")
                }

                else -> {
                    Toast.makeText(this, "Role tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateAdmin(username: String, password: String) {
        when {
            username == "admin" && password == "admin123" -> {
                Toast.makeText(this, "Login sebagai Admin", Toast.LENGTH_SHORT).show()
                navigateToAdminDashboard()
            }
            username == "99999" && password == "admin999" -> {
                Toast.makeText(this, "Login sebagai Admin", Toast.LENGTH_SHORT).show()
                navigateToAdminDashboard()
            }
            else -> {
                Toast.makeText(this, "Email atau kata sandi salah", Toast.LENGTH_SHORT).show()  // DIUBAH
            }
        }
    }

    private fun validateStaff(kodeGuru: String, password: String) {
        when (selectedRole) {
            "Waka" -> {
                if (kodeGuru == "waka" && password == "waka123") {
                    Toast.makeText(this, "Login sebagai Waka", Toast.LENGTH_SHORT).show()
                    navigateToWakaDashboard()
                } else {
                    Toast.makeText(this, "Kode guru atau kata sandi salah", Toast.LENGTH_SHORT).show()  // DIUBAH
                }
            }
            "Guru" -> {
                if ((kodeGuru == "guru" && password == "guru123") ||
                    (kodeGuru == "12345" && password == "guru123")) {
                    Toast.makeText(this, "Login sebagai Guru", Toast.LENGTH_SHORT).show()
                    navigateToGuruDashboard()
                } else {
                    Toast.makeText(this, "Kode guru atau kata sandi salah", Toast.LENGTH_SHORT).show()  // DIUBAH
                }
            }
            "Wali Kelas" -> {
                if ((kodeGuru == "wali" && password == "wali123") ||
                    (kodeGuru == "54321" && password == "wakel123")) {
                    Toast.makeText(this, "Login sebagai Wali Kelas", Toast.LENGTH_SHORT).show()
                    navigateToWaliKelasDashboard()
                } else {
                    Toast.makeText(this, "Kode guru atau kata sandi salah", Toast.LENGTH_SHORT).show()  // DIUBAH
                }
            }
        }
    }

    private fun validateSiswa(nisn: String, isPengurus: Boolean) {
        if (isPengurus) {
            if (nisn == "pengurus") {
                Toast.makeText(this, "Login sebagai Pengurus Kelas", Toast.LENGTH_SHORT).show()
                navigateToSiswaDashboard(true)
            } else {
                Toast.makeText(this, "NISN pengurus tidak valid", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (nisn == "siswa") {
                Toast.makeText(this, "Login sebagai Siswa", Toast.LENGTH_SHORT).show()
                navigateToSiswaDashboard(false)
            } else {
                Toast.makeText(this, "NISN tidak valid", Toast.LENGTH_SHORT).show()
            }
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
}