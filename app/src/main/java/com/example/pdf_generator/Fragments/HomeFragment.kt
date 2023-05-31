package com.example.pdf_generator.Fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pdf_generator.Listner.PdfItemClickListener
import com.example.pdf_generator.adapters.PdfAdapter
import com.example.pdf_generator.databinding.FragmentHomeBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class HomeFragment : Fragment(), PdfItemClickListener {

    private var _binding: FragmentHomeBinding?=null
    private val binding get() = _binding!!
    private lateinit var listener: PdfItemClickListener
    private lateinit var adapter: PdfAdapter

    var permissionArray = arrayOf<String>(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listener=this
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
        checkPermissions()
        getFiles()
        binding.llForScanner.setOnClickListener {
            val action=HomeFragmentDirections.actionHomeFragmentToScannerFragment()
            findNavController().navigate(action)
        }

        binding.createPdfFab.setOnClickListener {
            val action=HomeFragmentDirections.actionHomeFragmentToCameraFragment()
            findNavController().navigate(action)
        }

        binding.createPdfSelectImageFab.setOnClickListener{
            openGallery()
        }
    }

    private fun getFiles(){
        val documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val folderName = "PdfGeneratorDocuments"

        val folder = File(documentsDirectory, folderName)
        if (folder.exists() && folder.isDirectory) {

            val pdfFiles = folder.listFiles { file ->
                file.isFile && file.extension.equals("pdf", ignoreCase = true)
            }
            setUpRecyclerView(pdfFiles)
        }
        else Toast.makeText(requireContext(), "Folder Doesn't Exists", Toast.LENGTH_SHORT).show()

    }

    private fun setUpRecyclerView(files: Array<File>?){
        adapter= PdfAdapter(files, listener)
        binding.savedPdfRcv.adapter=adapter
        binding.savedPdfRcv.layoutManager=LinearLayoutManager(requireContext())
    }

    private fun openPdf(file: File){
        val authority = "com.example.pdf_generator.fileprovider"

        val pdfUri = FileProvider.getUriForFile(requireContext(), authority, file)

        val pdfIntent = Intent(Intent.ACTION_VIEW)
        pdfIntent.setDataAndType(pdfUri, "application/pdf")
        pdfIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY

        try {
            startActivity(pdfIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No Pdf Viewer Installed", Toast.LENGTH_SHORT).show()
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

    override fun pdfItemClicked(file: File) {
        openPdf(file)
    }
}
