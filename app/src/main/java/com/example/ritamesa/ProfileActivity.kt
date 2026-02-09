package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.pref.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtRole: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        loadProfileData()
        setupListeners()
    }

    private fun initViews() {
        imgProfile = findViewById(R.id.img_profile)
        txtName = findViewById(R.id.txt_name)
        txtRole = findViewById(R.id.txt_role)
        btnLogout = findViewById(R.id.btn_logout)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun loadProfileData() {
        val sessionManager = SessionManager(this)
        val name = sessionManager.getUserName()
        val role = sessionManager.getUserRole()
        val photoUrl = sessionManager.getPhotoUrl()

        txtName.text = name ?: "User"
        txtRole.text = role ?: "Role"

        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .placeholder(R.drawable.profile_siswa) // Default
                .error(R.drawable.profile_siswa)
                .into(imgProfile)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {
            performLogout()
        }

        findViewById<Button>(R.id.btn_edit_profile).setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun showEditProfileDialog() {
        // Since pop_up_edit_profile layout doesn't exist, we use a standard AlertDialog for Password Change
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Ganti Password")
        
        val container = android.widget.LinearLayout(this)
        container.orientation = android.widget.LinearLayout.VERTICAL
        val padding = (16 * resources.displayMetrics.density).toInt()
        container.setPadding(padding, padding / 2, padding, 0)

        val input = android.widget.EditText(this)
        input.hint = "Password Baru"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        container.addView(input)
        
        builder.setView(container)
        
        builder.setPositiveButton("Simpan") { _, _ ->
            val newPass = input.text.toString()
            if (newPass.isNotEmpty()) {
                updatePassword(newPass)
            }
        }
        builder.setNegativeButton("Batal") { dialogI, _ -> dialogI.cancel() }
        
        builder.show()
    }

    private fun updatePassword(password: String) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val sessionManager = SessionManager(this)
        val name = sessionManager.getUserName() ?: "User"
        val request = com.example.ritamesa.data.model.UpdateProfileRequest(
            name = name
        )

        val pd = android.app.ProgressDialog(this)
        pd.setMessage("Updating profile...")
        pd.show()

        apiService.updateProfile(request).enqueue(object : Callback<com.example.ritamesa.data.model.GeneralResponse> {
            override fun onResponse(
                call: Call<com.example.ritamesa.data.model.GeneralResponse>,
                response: Response<com.example.ritamesa.data.model.GeneralResponse>
            ) {
                pd.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Password berhasil diubah", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Gagal mengubah password: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.example.ritamesa.data.model.GeneralResponse>, t: Throwable) {
                pd.dismiss()
                Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
