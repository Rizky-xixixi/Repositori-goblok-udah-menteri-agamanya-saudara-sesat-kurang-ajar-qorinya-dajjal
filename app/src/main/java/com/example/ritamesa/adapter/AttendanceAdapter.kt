package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AttendanceAdapter(private val attendanceList: List<Attendance>) :
    RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTanggal: TextView = itemView.findViewById(R.id.text_tanggal)
        val textWaktu: TextView = itemView.findViewById(R.id.text_waktu)
        val textNama: TextView = itemView.findViewById(R.id.text_nama)
        val textClass: TextView = itemView.findViewById(R.id.text_role)
        val textStatus: TextView = itemView.findViewById(R.id.text_status)
        val textKeterangan: TextView = itemView.findViewById(R.id.text_keterangan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kehadiran_siswa_waka, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attendance = attendanceList[position]

        holder.textTanggal.text = attendance.date
        holder.textNama.text = attendance.name
        holder.textClass.text = attendance.classGrade
        holder.textStatus.text = attendance.status
        holder.textKeterangan.text = attendance.keterangan
        holder.textWaktu.text = "07:00"

        when (attendance.status) {
            "Tepat Waktu" -> {
                holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            }
            "Terlambat" -> {
                holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.orange))
            }
            "Izin" -> {
                holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.blue))
            }
            "Sakit" -> {
                holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.purple))
            }
            "Alpha" -> {
                holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            }
        }
    }

    override fun getItemCount(): Int = attendanceList.size
}