package com.example.pdf_generator.Fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pdf_generator.databinding.FragmentHomeBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding?=null
    private val binding get() = _binding!!

    private lateinit var imageuriList:ArrayList<Uri>
    private lateinit var imageBitmapList:ArrayList<Bitmap>
    var permissionArray = arrayOf<String>(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageuriList= ArrayList()
        imageBitmapList= ArrayList()
        //Permission checking...
       checkPermissions()


        binding.createPdfFab.setOnClickListener {
            val action=HomeFragmentDirections.actionHomeFragmentToCameraFragment()
            findNavController().navigate(action)
        }
        binding.createPdfSelectImageFab.setOnClickListener{
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Images"), 123)
    }

    private fun checkPermissions() {
        if((ActivityCompat.checkSelfPermission(
                requireContext(),permissionArray[0])!= PackageManager.PERMISSION_GRANTED)&&
            (ActivityCompat.checkSelfPermission(
                requireContext(),permissionArray[1])!= PackageManager.PERMISSION_GRANTED)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionArray,123)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==123 && resultCode==RESULT_OK){
            if (data != null) {
                if(data.getClipData()!=null) {
                    val x = data.getClipData()!!.getItemCount()
                    for (i in 0..x)
                        imageuriList.add(data.getClipData()!!.getItemAt(i).getUri())

                    for(i in imageuriList){
                        getBitmapFromUri(i)?.let { imageBitmapList.add(it) }
                    }

                    createPdf(imageBitmapList, "sample ${randomNumberGeneratorForTest()}")
                }else if(data.data !=null){
                    val imguri= data.data!!.path
                    imageuriList.add(Uri.parse(imguri))
                }
            }
                }
            }
    private fun randomNumberGeneratorForTest(): Int = kotlin.random.Random.nextInt()

    private fun scaleBitmapToFitScreenWidth(bitmap: Bitmap, screenWidth: Int): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val scaledHeight = (screenWidth.toFloat() / bitmapWidth * bitmapHeight).toInt()
        val processedBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, scaledHeight, true)
        val stream = ByteArrayOutputStream()
        processedBitmap.compress(Bitmap.CompressFormat.WEBP, 50 ,stream)
        val  byteArray=stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun createPdf(bitmaps: List<Bitmap>, pdfFileName: String) {
        if(bitmaps.size==0) {
            Toast.makeText(requireContext(), "Select some images to proceed", Toast.LENGTH_SHORT).show()
            return
        }
        val pdfDocument = PdfDocument()

        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidth = displayMetrics.widthPixels
        for (bitmap in bitmaps) {
            val scaledBitmap = scaleBitmapToFitScreenWidth(bitmap, screenWidth)
            val pageInfo = PdfDocument.PageInfo.Builder(scaledBitmap.width, scaledBitmap.height, pdfDocument.pages.size + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)
        }
        val directory = Environment.getExternalStoragePublicDirectory("PdfGeneratorDocuments")
        if (!directory.exists()) {directory.mkdirs()}

        val pdfFilePath = "${directory.path}/$pdfFileName.pdf"
        val pdfFile = File(pdfFilePath)

        try {
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()
            Toast.makeText(requireContext(), "${pdfFileName}.pdf saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.w(ContentValues.TAG, "Error while creating Pdf: ${e}")
            Toast.makeText(requireContext(), "Failed to create PDF", Toast.LENGTH_SHORT).show()
        }

//        val action=HomeFragmentDirections.actionHomeFragmentToCameraFragment()
//        findNavController().navigate(action)
    }
    private fun getBitmapFromUri(it: Uri): Bitmap? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(it)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Failed to load image: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            null
        }
    }

}
