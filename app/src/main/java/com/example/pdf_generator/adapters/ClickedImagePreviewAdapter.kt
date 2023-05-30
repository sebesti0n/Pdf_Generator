package com.example.pdf_generator.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pdf_generator.Listner.ItemClickListner
import com.example.pdf_generator.R

class ClickedImagePreviewAdapter( private val imgList:ArrayList<Uri>?):RecyclerView.Adapter<ClickedImagePreviewAdapter.clickedImageViewHolder>() {



private var listData:MutableList<Uri> = imgList as MutableList<Uri>

    fun getList():MutableList<Uri>{
        return listData
    }
    inner class clickedImageViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        fun bind(uri:Uri,idx:Int){

            val imageview= itemView.findViewById<ImageView>(R.id.clicked_iv)
            val btndelete=itemView.findViewById<ImageButton>(R.id.item_delete_btn)

            imageview.setImageURI(uri)
            btndelete.setOnClickListener {
                deleteitem(idx)
            }
        }

    }
    fun deleteitem(idx:Int){
        listData.removeAt(idx)
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): clickedImageViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.clicked_image_item_list,parent,false)
        return clickedImageViewHolder(view)
    }


    override fun getItemCount(): Int {
        return listData.size
    }


    override fun onBindViewHolder(holder: clickedImageViewHolder, position: Int) {
        holder.bind(listData[position],position)
    }


}