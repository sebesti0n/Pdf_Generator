package com.example.pdf_generator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pdf_generator.Listner.PdfItemClickListener
import com.example.pdf_generator.R
import java.io.File

class PdfAdapter(val list: Array<File>?,val listener: PdfItemClickListener): RecyclerView.Adapter<PdfAdapter.PdfViewHolder>(){
    class PdfViewHolder(val view: View): RecyclerView.ViewHolder(view){
        val name: TextView =view.findViewById(R.id.file_name_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        return PdfViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.pdf_item_view, parent, false))
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val file=list!![position]
        holder.name.text=file.name
        holder.itemView.setOnClickListener {
            listener.pdfItemClicked(file)
        }
    }
}