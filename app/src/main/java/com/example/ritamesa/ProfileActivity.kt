package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.User
import com.example.ritamesa.data.pref.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ProfileActivity"
    }

    private lateinit var imgProfile: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtRole: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtAdditionalInfo: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnBack: ImageButton
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)
        apiService = ApiClient.getClient(this).create(ApiService::class.java)

        initViews()
        loadProfileFromSession() // Show cached data first
        fetchProfileFromApi()    // Then fetch fresh data
        setupListeners()
    }

    private fun initViews() {
        imgProfile = findViewById(R.id.img_profile)
        txtName = findViewById(R.id.txt_name)
        txtRole = findViewById(R.id.txt_role)
        txtEmail = findViewById(R.id.txt_email)
        txtAdditionalInfo = findViewById(R.id.txt_additional_info)
        btnLogout = findViewById(R.id.btn_logout)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun loadProfileFromSession() {
        // Show cached data immediately while API loads
        txtName.text = sessionManager.getUserName() ?: "Memuat..."
        txtRole.text = formatRole(sessionManager.getUserRole())
        txtEmail.text = ""
        txtAdditionalInfo.text = ""
        
        val photoUrl = sessionManager.getPhotoUrl()
        loadProfileImage(photoUrl)
    }

    private fun fetchProfileFromApi() {
        apiService.getCurrentUser().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        updateUI(user)
                        // Update session with fresh data
                        sessionManager.saveUserDetails(user.id.toString(), user.name)
                        sessionManager.saveUserRole(user.role)
                        user.profile?.photoUrl?.let { sessionManager.savePhotoUrl(it) }
                    }
                } else {
                    Log.e(TAG, "Failed to fetch profile: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "Error fetching profile: ${t.message}")
            }
        })
    }

    private fun updateUI(user: User) {
        txtName.text = user.name
        txtRole.text = formatRole(user.role)
        txtEmail.text = user.email ?: ""
        
        // Build additional info based on role
        val additionalInfo = buildAdditionalInfo(user)
        txtAdditionalInfo.text = additionalInfo
        
        // Load profile image
        loadProfileImage(user.profile?.photoUrl)
    }

    private fun formatRole(role: String?): String {
        return when (role?.lowercase()) {
            "student" -> "Siswa"
            "teacher" -> "Guru"
            "admin" -> "Administrator"
            "vice_principal" -> "Wakil Kepala Sekolah"
            else -> role?.replaceFirstChar { it.uppercase() } ?: "User"
        }
    }

    private fun buildAdditionalInfo(user: User): String {
        val parts = mutableListOf<String>()
        
        user.profile?.let { profile ->
            profile.className?.let { parts.add("Kelas: $it") }
            profile.nis?.let { if (it.isNotEmpty()) parts.add("NIS: $it") }
            profile.nip?.let { if (it.isNotEmpty()) parts.add("NIP: $it") }
        }
        
        if (user.isClassOfficer) {
            parts.add("Pengurus Kelas")
        }
        
        return parts.joinToString(" • ")
    }

    private fun loadProfileImage(photoUrl: String?) {
        val defaultImage = when (sessionManager.getUserRole()?.lowercase()) {
            "teacher" -> R.drawable.profile_guru
            else -> R.drawable.profile_siswa
        }
        
        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .placeholder(defaultImage)
                .error(defaultImage)
                .into(imgProfile)
        } else {
            imgProfile.setImageResource(defaultImage)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Keluar")
            .setMessage("Yakin ingin keluar dari akun?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
        
        // Call API Logout
        apiService.logout().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // Ignore result, just clear session locally
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Ignore error
            }
        })

        // Clear Session
        val sessionManager = SessionManager(this)
        sessionManager.clearSession()

        // Navigate to Login
        val intent = Intent(this, LoginAwal::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
