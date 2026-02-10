package com.example.ritamesa.model

data class Guru(
    val nama: String,
    val nip: String,
    val mataPelajaran: String
)

data class Kehadiran(
    val tanggal: String,
    val mataPelajaran: String,
    val kelas: String,
    val jam: String,
    val status: String,
    val keterangan: String
)

annotation class Guru1
