package com.example.ritamesa

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

enum class StatusDispensasi {
    MENUNGGU,
    DISETUJUI,
    DITOLAK
}