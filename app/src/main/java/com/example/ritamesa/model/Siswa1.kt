package com.example.ritamesa

data class Siswa(
    val nomor: Int,
    val nama: String,
    val nisn: String,
    val kelas: String
)

data class KehadiranSiswa(
    val tanggal: String,
    val mataPelajaran: String,
    val kelas: String,
    val jam: String,
    val status: String,
    val keterangan: String
)