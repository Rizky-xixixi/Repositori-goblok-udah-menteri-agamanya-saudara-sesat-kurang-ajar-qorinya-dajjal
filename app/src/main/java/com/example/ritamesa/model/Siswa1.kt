package com.example.ritamesa

data class Siswa(
    val nomor: Int,
    val nama: String,
    val nisn: String,
    val kelas: String
) {
    fun getDataKehadiran(): List<KehadiranSiswa> {
        return listOf(
            KehadiranSiswa("Senin, 7 Januari 2026", "Matematika", "XII RPL 2", "07:00", "Hadir", "Hadir tepat waktu"),
            KehadiranSiswa("Senin, 7 Januari 2026", "Fisika", "XII RPL 2", "09:00", "Sakit", "Demam"),
            KehadiranSiswa("Selasa, 8 Januari 2026", "Bahasa Indonesia", "XII RPL 2", "07:00", "Izin", "Keperluan keluarga"),
            KehadiranSiswa("Selasa, 8 Januari 2026", "Kimia", "XII RPL 2", "10:00", "Alpha", "-")
        )
    }
}

data class KehadiranSiswa(
    val tanggal: String,
    val mataPelajaran: String,
    val kelas: String,
    val jam: String,
    val status: String,
    val keterangan: String
)