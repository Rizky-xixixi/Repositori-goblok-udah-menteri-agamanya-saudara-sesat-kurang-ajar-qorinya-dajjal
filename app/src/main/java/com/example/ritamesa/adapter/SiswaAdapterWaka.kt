package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SiswaAdapterWaka(
    private val listSiswa: List<Siswa>,
    private val onLihatClick: (Siswa) -> Unit
) : RecyclerView.Adapter<SiswaAdapterWaka.SiswaViewHolder>() {

    class SiswaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvTelepon: TextView = itemView.findViewById(R.id.tvTelepon)
        val tvMataPelajaran: TextView = itemView.findViewById(R.id.tvMataPelajaran)
        val btnLihat: ImageButton = itemView.findViewById(R.id.btnLihat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_siswa, parent, false)
        return SiswaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
        val siswa = listSiswa[position]

        holder.tvNomor.text = siswa.nomor.toString()
        holder.tvNama.text = siswa.nama
        holder.tvTelepon.text = siswa.nisn
        holder.tvMataPelajaran.text = siswa.kelas

        holder.btnLihat.setOnClickListener {
            onLihatClick(siswa)
        }
    }

    override fun getItemCount(): Int = listSiswa.size
}