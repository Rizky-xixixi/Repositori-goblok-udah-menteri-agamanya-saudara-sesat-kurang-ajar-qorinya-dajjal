package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RiwayatKehadiranAdapter(
    private val listKehadiran: List<RiwayatKehadiranWaka>
) : RecyclerView.Adapter<RiwayatKehadiranAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTanggal: TextView = view.findViewById(R.id.textView25)
        val tvMataPelajaran: TextView = view.findViewById(R.id.textView27)
        val tvKelasJurusan: TextView = view.findViewById(R.id.textView29)
        val tvStatusLabel: TextView = view.findViewById(R.id.textView30)
        val tvKeterangan: TextView = view.findViewById(R.id.textView33)
        val ivStatusIcon: ImageView = view.findViewById(R.id.imageView32)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_kehadiran_waka, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kehadiran = listKehadiran[position]

        holder.tvTanggal.text = kehadiran.tanggal
        holder.tvMataPelajaran.text = kehadiran.mataPelajaran
        holder.tvKelasJurusan.text = kehadiran.kelasJurusan
        holder.tvKeterangan.text = kehadiran.keterangan

        when (kehadiran.status.lowercase()) {
            "hadir" -> holder.ivStatusIcon.setImageResource(R.drawable.group_556)
            "sakit" -> holder.ivStatusIcon.setImageResource(R.drawable.group_556__1_)
            "izin" -> holder.ivStatusIcon.setImageResource(R.drawable.group_556__2_)
            "alpha", "alpa" -> holder.ivStatusIcon.setImageResource(R.drawable.group_556__3_)
            else -> holder.ivStatusIcon.setImageResource(R.drawable.group_556)
        }
    }

    override fun getItemCount(): Int = listKehadiran.size
}