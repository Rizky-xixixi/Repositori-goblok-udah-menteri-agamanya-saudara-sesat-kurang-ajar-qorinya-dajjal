package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RiwayatKehadiranGuruAdapter(private val dataList: List<RiwayatKehadiranGuruWaka>) :
    RecyclerView.Adapter<RiwayatKehadiranGuruAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTanggal: TextView = itemView.findViewById(R.id.text_tanggal)
        val textWaktu: TextView = itemView.findViewById(R.id.text_waktu)
        val textNama: TextView = itemView.findViewById(R.id.text_nama)
        val textRole: TextView = itemView.findViewById(R.id.text_role)
        val textStatus: TextView = itemView.findViewById(R.id.text_status)
        val textKeterangan: TextView = itemView.findViewById(R.id.text_keterangan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kehadiran_waka, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        holder.textTanggal.text = data.tanggal
        holder.textWaktu.text = data.waktu
        holder.textNama.text = data.nama
        holder.textRole.text = data.role

        holder.textStatus.text = data.status
        holder.textStatus.setTextColor(when (data.status) {
            "Hadir" -> holder.itemView.context.getColor(R.color.status_hadir)
            "Terlambat" -> holder.itemView.context.getColor(R.color.status_terlambat)
            "Izin" -> holder.itemView.context.getColor(R.color.status_izin)
            "Sakit" -> holder.itemView.context.getColor(R.color.status_sakit)
            "Alpha" -> holder.itemView.context.getColor(R.color.status_alpha)
            "Pulang" -> holder.itemView.context.getColor(R.color.status_pulang)
            else -> holder.itemView.context.getColor(R.color.black)
        })

        holder.textKeterangan.text = data.keterangan
    }

    override fun getItemCount(): Int = dataList.size
}