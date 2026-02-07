package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotifikasiAdapterWaka(
    private val dataList: List<Map<String, String>>,
    private val isGuru: Boolean,
    private val onItemClickListener: ((Map<String, String>) -> Unit)? = null
) : RecyclerView.Adapter<NotifikasiAdapterWaka.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtAlert: TextView = itemView.findViewById(R.id.text_alert)
        val txtJam: TextView = itemView.findViewById(R.id.jam_notifikasi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notif_semua_waka, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        val type = data["type"] ?: ""
        val nama = data["nama"] ?: ""
        val detail = data["detail"] ?: ""
        val jam = data["time"] ?: ""

        val pesan = if (isGuru) {
            "$nama"
        } else {
            when (type) {
                "alpha" -> "$nama tidak hadir (Alpha)"
                "izin" -> "$nama izin tidak masuk"
                "sakit" -> "$nama sakit"
                "terlambat" -> "$nama terlambat"
                else -> nama
            }
        }

        holder.txtAlert.text = pesan
        holder.txtJam.text = jam

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(data)
        }
    }

    override fun getItemCount(): Int = dataList.size
}