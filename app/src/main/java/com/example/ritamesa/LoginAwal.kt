package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.abs

class LoginAwal : AppCompatActivity() {

    companion object {
        const val EXTRA_ROLE = "SELECTED_ROLE"  // Tambahkan konstanta
    }

    private lateinit var gestureDetector: GestureDetector

    // Simpan role yang dipilih
    private var selectedRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_awal)

        val mainView = findViewById<android.view.View>(R.id.motionLayout)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup dropdown SIMPLE - gak pake popup menu yang ribet
        setupSimpleDropdown()

        gestureDetector = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                private val SWIPE_THRESHOLD = 100
                private val SWIPE_VELOCITY_THRESHOLD = 100

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null) return false
                    val diffY = e2.y - e1.y

                    // Cukup cek swipe up aja
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY < 0) { // Swipe UP
                            navigateToNext()
                            return true
                        }
                    }
                    return false
                }
            }
        )

        mainView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun setupSimpleDropdown() {
        val roleEditText = findViewById<EditText>(R.id.role_login)
        val btnDropdown = findViewById<ImageButton>(R.id.btn_dropdown_arrow)

        // Daftar role
        val roles = arrayOf(
            "Admin",
            "Waka",
            "Guru",
            "Wali Kelas",
            "Siswa",
            "Pengurus"
        )

        // Current index
        var currentIndex = -1

        btnDropdown.setOnClickListener {
            // SIMPLE: Ganti ke role berikutnya (cycle)
            currentIndex = (currentIndex + 1) % roles.size
            selectedRole = roles[currentIndex]
            roleEditText.setText(selectedRole)

            // Kasih feedback
            Toast.makeText(this, "Role: $selectedRole", Toast.LENGTH_SHORT).show()
        }

        // Optional: Kalau klik EditText, juga ganti role
        roleEditText.setOnClickListener {
            btnDropdown.performClick()
        }

        // Set default ke role pertama
        btnDropdown.performClick()
    }

    private fun navigateToNext() {
        if (selectedRole.isEmpty()) {
            Toast.makeText(this, "Pilih role dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, LoginLanjut::class.java)
        intent.putExtra(EXTRA_ROLE, selectedRole)  // Gunakan konstanta
        startActivity(intent)
        finish()
        overridePendingTransition(
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
    }
}