package com.example.ritamesa.model

data class Guru(
    val nama: String,
    val nip: String,
    val mataPelajaran: String
) {
    fun getDataKehadiran(): List<Kehadiran> {
        return listOf(
            Kehadiran("Senin, 7 Januari 2026", "Bahasa", "12", "07:00", "Hadir", "Hadir"),
            Kehadiran("Senin, 7 Januari 2026", "Bahasa", "12", "07:00", "Sakit", "Demam"),
            Kehadiran("Senin, 7 Januari 2026", "Bahasa", "12", "07:00", "Izin", "Rapat"),
            Kehadiran("Senin, 7 Januari 2026", "Bahasa", "12", "07:00", "Alpha", "-")
        )
    }
}

data class Kehadiran(
    val tanggal: String,
    val mataPelajaran: String,
    val kelas: String,
    val jam: String,
    val status: String,
    val keterangan: String
)

annotation class Guru1
