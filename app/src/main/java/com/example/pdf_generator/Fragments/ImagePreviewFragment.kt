package com.example.pdf_generator.Fragments

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pdf_generator.R
import com.example.pdf_generator.adapters.ClickedImagePreviewAdapter
import com.example.pdf_generator.databinding.FragmentImagePreviewBinding
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ImagePreviewFragment : Fragment() {


    private var _binding: FragmentImagePreviewBinding? = null
    private val binding get() = _binding!!
    private var imgList: ArrayList<Uri>? = null
    private lateinit var adapter: ClickedImagePreviewAdapter
    private val REQUEST_CODE = 20

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,

                ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imguri = arguments?.let { ImagePreviewFragmentArgs.fromBundle(it).uriImage }
        if (imguri != null) {
            imgList = ArrayList(imguri.asList())
        }

        setclickedImageinRV()
        binding.openDialogBoxForName.setOnClickListener {
            showDialog()
        }
//        checkpermission()

    }

//    private fun checkpermission() {
//        if (checkallPermission())
//        else requestPermission()
//    }

    private fun setclickedImageinRV() {
        adapter = ClickedImagePreviewAdapter(imgList)
        binding.RVImageGrid.layoutManager = GridLayoutManager(requireContext(),3)
        binding.RVImageGrid.adapter = adapter
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialoglayout, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.et_pdfname)
        with(builder) {
            setPositiveButton("Create") { dialog, which ->
                val temp = editText.text.toString()
                if (temp.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter Pdf Name!", Toast.LENGTH_LONG).show()

                } else {
                    createPdf(temp)
                }
            }
            setNegativeButton("Cancel") { dialog, which ->
                Log.d("PKS", "-ve Button Clicked")

            }
            setView(dialogLayout)
            show()

        }

    }
//    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
//        var bitmap: Bitmap? = null
//        try {
//            val contentResolver: ContentResolver = context.contentResolver
//            val inputStream = contentResolver.openInputStream(uri)
//            bitmap = BitmapFactory.decodeStream(inputStream)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return bitmap
//    }
//    private fun scaleBitmapToFitScreenWidth(bitmap: Bitmap, screenWidth: Int): Bitmap {
//        val bitmapWidth = bitmap.width
//        val bitmapHeight = bitmap.height
//        val scaledHeight = (screenWidth.toFloat() / bitmapWidth * bitmapHeight).toInt()
//        val processedBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, scaledHeight, true)
//        val stream = ByteArrayOutputStream()
//        processedBitmap.compress(Bitmap.CompressFormat.WEBP, 50 ,stream)
//        val  byteArray=stream.toByteArray()
//        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//    }
//
//    fun createPdf(pdfFileName: String,uris: MutableList<Uri>) {
//        var bitmaps=mutableListOf<Bitmap>()
//        for(uri in uris){
//            uriToBitmap(requireContext(),uri)?.let { bitmaps.add(it) }
//        }
//
//
//        if(bitmaps.size==0) {
//            Toast.makeText(requireContext(), "Click some images to proceed", Toast.LENGTH_SHORT).show()
//            return
//        }
//        val pdfDocument = PdfDocument()
//
//        val displayMetrics = Resources.getSystem().displayMetrics
//        val screenWidth = displayMetrics.widthPixels
//        for (bitmap in bitmaps) {
//            val scaledBitmap = scaleBitmapToFitScreenWidth(bitmap, screenWidth)
//            val pageInfo = PdfDocument.PageInfo.Builder(scaledBitmap.width, scaledBitmap.height, pdfDocument.pages.size + 1).create()
//            val page = pdfDocument.startPage(pageInfo)
//            page.canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
//            pdfDocument.finishPage(page)
//        }
//        val directory = Environment.getExternalStoragePublicDirectory("PdfGeneratorDocuments")
//        if (!directory.exists()) {directory.mkdirs()}
//
//        val pdfFilePath = "${directory.path}/$pdfFileName.pdf"
//        val pdfFile = File(pdfFilePath)
//
//        try {
//            pdfDocument.writeTo(FileOutputStream(pdfFile))
//            pdfDocument.close()
//            Toast.makeText(requireContext(), "${pdfFileName}.pdf saved successfully", Toast.LENGTH_SHORT).show()
//        } catch (e: IOException) {
//            Log.w(ContentValues.TAG, "Error while creating Pdf: ${e}")
//            Toast.makeText(requireContext(), "Failed to create PDF", Toast.LENGTH_SHORT).show()
//        }
//
//
//    }

    private fun createPdf(s: String) {
        val cw = ContextWrapper(requireContext())
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        val pdfFile = File(
            directory,
            "$s.pdf"
        )
        val document = Document(PageSize.A4)
        if (!pdfFile.exists()) {

            Log.d("path", pdfFile.toString());
            var fos: FileOutputStream? = null;
            try {
                fos = FileOutputStream(pdfFile)
                PdfWriter.getInstance(document, fos)
                Log.w(ContentValues.TAG, "doc opening...")
                document.open()
                Log.w(ContentValues.TAG, "doc opened...")
                Log.w(ContentValues.TAG, "${adapter.getList().size}")
                for (uri in adapter.getList()) {
                    val image = Image.getInstance(uri.toString())
                    Log.w(ContentValues.TAG, "doc adding...")
                    document.add(image)
                }
                Log.w(ContentValues.TAG, "forl done")
                Toast.makeText(requireContext(), "${s}.pdf saved successfully", Toast.LENGTH_SHORT)
                    .show()
                fos.flush()
                fos.close()

                document.close()
                Log.w(ContentValues.TAG, "doc Closed")
            } catch (e: IOException) {
                Log.w(ContentValues.TAG, "Error while creating Pdf: ${e}")
                Toast.makeText(requireContext(), "Failed to create PDF", Toast.LENGTH_SHORT).show()

            }
        }
        }
        //private
//fun checkallPermission() = REQUIRED_PERMISSIONS.all{
//    ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
//}
//
//    @Deprecated("Deprecated in Java")
//    override fun onRequestPermissionsResult(
//        requestCode
//        : Int,
//        permissions
//        : Array<String>,
//        grantResults
//        : IntArray)
//    {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CODE) {
//            if (checkallPermission()) { binding.openDialogBoxForName.setOnClickListener {
//                showDialog()
//            } }
//            else { Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show() }
//        }
//    }
//
//    private fun requestPermission() {
//        val activityResultLauncher =
//            registerForActivityResult(
//                ActivityResultContracts.RequestMultiplePermissions())
//            { permissions ->
//                var permissionGranted = true
//
//                permissions.entries.forEach {
//                    if (it.key in CameraFragment.REQUIRED_PERMISSIONS && it.value == false)
//                        permissionGranted = false
//                }
//                if (!permissionGranted) Toast.makeText(requireContext(), "Permission denied",Toast.LENGTH_SHORT).show()
//                else binding.openDialogBoxForName.setOnClickListener {
//                    showDialog()
//                }
//            }
//        activityResultLauncher.launch(CameraFragment.REQUIRED_PERMISSIONS)
//    }
        override fun onResume() {
            super.onResume()
            (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        }

        override fun onStop() {
            super.onStop()
            (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        }

}