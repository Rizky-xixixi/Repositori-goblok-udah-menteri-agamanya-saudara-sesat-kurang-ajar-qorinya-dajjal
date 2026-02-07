package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DispensasiAdapter(
    private var dispensasiList: List<Dispensasi>,
    private val onItemClick: (Dispensasi) -> Unit
) : RecyclerView.Adapter<DispensasiAdapter.DispensasiViewHolder>() {

    inner class DispensasiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNamaSiswa: TextView = itemView.findViewById(R.id.textViewNamaSiswa)
        val textViewKelas: TextView = itemView.findViewById(R.id.textViewKelas)
        val textViewMataPelajaran: TextView = itemView.findViewById(R.id.textViewMataPelajaran)
        val imageViewStatus: ImageView = itemView.findViewById(R.id.imageViewStatus)
        val imageViewCheckmark: ImageView = itemView.findViewById(R.id.imageViewCheckmark)
        val imageViewCancel: ImageView = itemView.findViewById(R.id.imageViewCancel)
        val itemLayout: View = itemView.findViewById(R.id.itemLayout)

        fun bind(dispensasi: Dispensasi) {
            textViewNamaSiswa.text = "${dispensasi.namaSiswa} -"
            textViewKelas.text = dispensasi.kelas
            textViewMataPelajaran.text = dispensasi.mataPelajaran

            when (dispensasi.status) {
                StatusDispensasi.MENUNGGU -> {
                    imageViewStatus.setImageResource(R.drawable.menunggusye)
                    imageViewStatus.visibility = View.VISIBLE
                    imageViewCheckmark.visibility = View.GONE
                    imageViewCancel.visibility = View.GONE
                }
                StatusDispensasi.DISETUJUI -> {
                    imageViewStatus.setImageResource(R.drawable.diset)
                    imageViewStatus.visibility = View.VISIBLE
                    imageViewCheckmark.visibility = View.VISIBLE
                    imageViewCancel.visibility = View.GONE
                }
                StatusDispensasi.DITOLAK -> {
                    imageViewStatus.setImageResource(R.drawable.ditol)
                    imageViewStatus.visibility = View.VISIBLE
                    imageViewCheckmark.visibility = View.GONE
                    imageViewCancel.visibility = View.VISIBLE
                }
            }

            if (dispensasi.status == StatusDispensasi.MENUNGGU) {
                itemLayout.isClickable = true
                itemLayout.isFocusable = true
                itemLayout.setOnClickListener {
                    onItemClick(dispensasi)
                }
            } else {
                itemLayout.isClickable = false
                itemLayout.isFocusable = false
                itemLayout.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DispensasiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dispensasi, parent, false)
        return DispensasiViewHolder(view)
    }

    override fun onBindViewHolder(holder: DispensasiViewHolder, position: Int) {
        holder.bind(dispensasiList[position])
    }

    override fun getItemCount(): Int = dispensasiList.size

    fun updateList(newList: List<Dispensasi>) {
        dispensasiList = newList
        notifyDataSetChanged()
    }

    fun filterByStatus(status: StatusDispensasi?) {
        // Implementasi filter akan ditangani di Activity
    }
}