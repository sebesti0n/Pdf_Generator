package com.example.pdf_generator.UI

import android.content.ContentValues
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AppViewModel(): ViewModel(){
     var listOfBitmaps: MutableLiveData<ArrayList<Bitmap>?> = MutableLiveData()

    init{
        listOfBitmaps.postValue(arrayListOf())
    }
    fun setList(list: ArrayList<Bitmap>){
        listOfBitmaps.postValue(list)
    }

    fun getListSize(): Int{
        return listOfBitmaps.value!!.size
    }

    fun deleteElementAtPos(index: Int){
        val list=listOfBitmaps.value
        if(!list.isNullOrEmpty() && index>=0 && index<list.size){
            list.removeAt(index)
            listOfBitmaps.postValue(list)
        }
    }

    fun addElementToList(bitmap: Bitmap){
        var list=listOfBitmaps.value
        if(list!=null) list.add(bitmap)
        else{
            list= arrayListOf()
            list.add(bitmap)
        }
        setList(ArrayList<Bitmap>(list))

    }


}