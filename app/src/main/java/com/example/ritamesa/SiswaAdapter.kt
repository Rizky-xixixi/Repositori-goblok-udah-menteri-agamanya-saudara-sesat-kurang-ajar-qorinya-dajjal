package com.example.ritamesa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.R
import com.example.ritamesa.data.model.StudentItem

class SiswaAdapter(
    private var listSiswa: List<StudentItem>,
    private val onEditClick: (StudentItem) -> Unit,
    private val onDeleteClick: (StudentItem) -> Unit
) : RecyclerView.Adapter<SiswaAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNo: TextView = itemView.findViewById(R.id.tvNo)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvNisn: TextView = itemView.findViewById(R.id.tvNisn)
        val tvKelas: TextView = itemView.findViewById(R.id.tvKelas)
        val tvJurusan: TextView = itemView.findViewById(R.id.tvKode)
        val tvJk: TextView = itemView.findViewById(R.id.tvJk)
        
        val btnEdit: View = itemView.findViewById(R.id.btnEdit)
        val btnHapus: View = itemView.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crud_datasiswa, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val siswa = listSiswa[position]
        
        holder.tvNo.text = (position + 1).toString()
        holder.tvNama.text = siswa.name
        holder.tvNisn.text = siswa.nisn ?: "-"
        holder.tvKelas.text = siswa.className
        holder.tvJurusan.text = siswa.majorName
        holder.tvJk.text = siswa.gender ?: "-"
        
        holder.btnEdit.setOnClickListener { onEditClick(siswa) }
        holder.btnHapus.setOnClickListener { onDeleteClick(siswa) }
    }

    override fun getItemCount(): Int = listSiswa.size
    
    fun updateData(newList: List<StudentItem>) {
        listSiswa = newList
        notifyDataSetChanged()
    }
}
