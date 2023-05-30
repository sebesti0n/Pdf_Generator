package com.example.pdf_generator.Listner

import android.net.Uri
import android.view.View

interface ItemClickListner {
fun onItemClick(view:View,imgUri: Uri,position: Int)
fun onDeleteBtnClick(view:View,position: Int)
}