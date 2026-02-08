package com.example.ritamesa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ritamesa.data.api.ApiClient
import com.example.ritamesa.data.api.ApiService
import com.example.ritamesa.data.model.ScanRequest
import com.example.ritamesa.data.model.ScanResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CameraQRActivity : AppCompatActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var btnBack: ImageButton
    private lateinit var btnFlash: ImageButton
    private lateinit var progressBar: ProgressBar

    private var isFlashOn = false
    private var isScanning = true

    private lateinit var barcodeCallback: BarcodeCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
        private const val LOCATION_PERMISSION_REQUEST = 101
        private const val TAG = "CameraQRActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_qr)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        setupCallback()
        checkPermissionsAndStart()
        setupButtonListeners()
    }

    private fun initViews() {
        barcodeView = findViewById(R.id.barcode_scanner)
        btnBack = findViewById(R.id.btn_back_camera)
        btnFlash = findViewById(R.id.btn_flash)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupCallback() {
        barcodeCallback = object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (!isScanning) return
                isScanning = false
                Log.d(TAG, "QR Scanned: ${result.text}")
                handleQRResult(result.text)
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
            }
        }
    }

    private fun checkPermissionsAndStart() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        val listPermissionsNeeded = ArrayList<String>()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            startCamera()
            getLastLocation()
        }
    }

    private fun startCamera() {
        try {
            barcodeView.decodeContinuous(barcodeCallback)
            barcodeView.resume()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting camera", e)
            Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    lastLocation = location
                    Log.d(TAG, "Location found: ${location.latitude}, ${location.longitude}")
                } else {
                    Log.d(TAG, "Location is null")
                    // Request new location update if needed (omitted for simplicity)
                }
            }
    }

    private fun handleQRResult(qrText: String) {
        progressBar.visibility = View.VISIBLE
        
        // 1. Try to parse Token
        var token = qrText
        try {
            // Check if it's JSON
            if (qrText.trim().startsWith("{")) {
                val json = JSONObject(qrText)
                if (json.has("token")) {
                    token = json.getString("token")
                }
            } else if (qrText.contains("|")) {
                 // Fallback for legacy format generated by simulator?
                 // Or maybe real QR has pipes?
                 // For now, if pipe exists, we assume it's NOT the token unless the structure is known.
                 // Backend expects a simple token string (UUID usually).
                 // If the QR is "ABSENSI|Class|...", this is NOT a valid token for the backend.
                 // But wait, the previous code generated this.
                 // IF real QR codes in the school are generated by the backend, they will likely be just the token or JSON.
                 // Let's rely on the token being the whole string if not JSON, but warn if it looks like the old format?
                 if (qrText.startsWith("ABSENSI|")) {
                      // This is likely a dummy QR from the old simulator.
                      // We can TRY to use it, but backend will likely reject it as "Token invalid".
                      // Unless we programmed the backend to accept "ABSENSI|..."? No.
                      Log.w(TAG, "Warning: Scanned legacy format: $qrText")
                 }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing QR", e)
        }

        // 2. Send to API
        sendScanRequest(token)
    }

    private fun sendScanRequest(token: String) {
        val lat = lastLocation?.latitude
        val lng = lastLocation?.longitude
        
        // Retrieve device ID from session
        val sessionManager = com.example.ritamesa.data.pref.SessionManager(this)
        val deviceId = sessionManager.getDeviceId()
        
        Log.d(TAG, "Sending scan request: token=$token, lat=$lat, lng=$lng, deviceId=$deviceId")

        val request = ScanRequest(
            token = token,
            latitude = lat,
            longitude = lng,
            deviceId = if (deviceId != 0) deviceId else null
        )

        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiService.scanQRCode(request).enqueue(object : Callback<ScanResponse> {
            override fun onResponse(call: Call<ScanResponse>, response: Response<ScanResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val scanResponse = response.body()
                    val attendance = scanResponse?.attendance
                    
                    val subject = attendance?.schedule?.mataPelajaran ?: "Tidak diketahui"
                    val time = attendance?.checkInTime ?: "-"
                    // Try to get teacher name from attendance.teacher.user.name, fallback to schedule or unknown
                    val teacherName = attendance?.teacher?.user?.name ?: "Guru"
                    
                    val message = """
                        Mata Pelajaran: $subject
                        Guru: $teacherName
                        Waktu: $time
                        Status: Berhasil
                    """.trimIndent()

                    androidx.appcompat.app.AlertDialog.Builder(this@CameraQRActivity)
                        .setTitle("Presensi Berhasil")
                        .setMessage(message)
                        .setPositiveButton("OK") { _, _ ->
                            val intent = Intent(this@CameraQRActivity, DashboardSiswaActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Gagal presensi"
                    // Parse json error if possible
                    var displayError = errorMsg
                    try {
                        val errorJson = JSONObject(errorMsg)
                        if (errorJson.has("message")) {
                            displayError = errorJson.getString("message")
                        }
                    } catch (e: Exception) {}
                    
                    androidx.appcompat.app.AlertDialog.Builder(this@CameraQRActivity)
                        .setTitle("Gagal")
                        .setMessage(displayError)
                        .setPositiveButton("Coba Lagi") { _, _ ->
                             isScanning = true
                             barcodeView.resume()
                        }
                        .setNegativeButton("Keluar") { _, _ ->
                            finish()
                        }
                        .show()
                }
            }

            override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@CameraQRActivity, "Error koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
                // Resume scanning
                Handler(Looper.getMainLooper()).postDelayed({
                    isScanning = true
                    barcodeView.resume()
                }, 2000)
            }
        })
    }

    private fun setupButtonListeners() {
        btnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        btnFlash.setOnClickListener {
            toggleFlash()
        }
    }

    private fun toggleFlash() {
        isFlashOn = !isFlashOn
        if (isFlashOn) {
            try {
                barcodeView.setTorchOn()
            } catch (e: Exception) {
            }
            btnFlash.setImageResource(R.drawable.ic_flash_on)
        } else {
            try {
                barcodeView.setTorchOff()
            } catch (e: Exception) {
            }
            btnFlash.setImageResource(R.drawable.ic_flash_off)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            // Check if all permissions are granted
            var allGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    break
                }
            }

            if (allGranted) {
                startCamera()
                getLastLocation()
            } else {
                Toast.makeText(this, "Izin kamera dan lokasi diperlukan", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            barcodeView.resume()
        } catch (e: Exception) {
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            barcodeView.pause()
            barcodeView.setTorchOff()
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            barcodeView.pause()
        } catch (e: Exception) {
        }
    }
}
