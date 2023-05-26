package com.example.pdf_generator.adapters

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pdf_generator.R

class clickedImagePreviewAdapter(private val imgList:ArrayList<Uri>):RecyclerView.Adapter<clickedImagePreviewAdapter.clickedImageViewHolder>() {
    class clickedImageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val imageview= itemView.findViewById<ImageView>(R.id.clicked_iv)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): clickedImageViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.clicked_image_item_list,parent,false)
        return clickedImageViewHolder(view)
    }


    override fun getItemCount(): Int {
        return imgList.size
    }


    override fun onBindViewHolder(holder: clickedImageViewHolder, position: Int) {
        holder.imageview.setImageURI(imgList[position])
    }

}