package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.model.Guru

class GuruAdapterWaka(
    private val guruList: List<Guru>,
    private val onItemClick: (Guru) -> Unit
) : RecyclerView.Adapter<GuruAdapterWaka.GuruViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guru, parent, false)
        return GuruViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
        val guru = guruList[position]
        holder.bind(guru)

        holder.itemView.findViewById<android.widget.ImageButton>(R.id.btnLihat).setOnClickListener {
            onItemClick(guru)
        }
    }

    override fun getItemCount(): Int = guruList.size

    class GuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(guru: Guru) {
            itemView.findViewById<TextView>(R.id.tvNomor).text = (adapterPosition + 1).toString()
            itemView.findViewById<TextView>(R.id.tvNama).text = guru.nama
            itemView.findViewById<TextView>(R.id.tvTelepon).text = guru.nip
            itemView.findViewById<TextView>(R.id.tvMataPelajaran).text = guru.mataPelajaran
        }
    }
}