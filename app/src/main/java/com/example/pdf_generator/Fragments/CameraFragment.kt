package com.example.pdf_generator.Fragments
import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pdf_generator.databinding.FragmentCameraBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CameraFragment : Fragment()
{
    private var _binding:FragmentCameraBinding?=null
    private val binding get()=_binding!!

    private lateinit var bitmapList:ArrayList<Bitmap>
    private var imgList:ArrayList<Uri>?=null
    private var cameraManager:CameraManager?=null
    private var imgCapture:ImageCapture?=null
    private lateinit var getcameraID:String
    private val REQUEST_CODE = 20

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        val REQUIRED_PERMISSIONS =
            mutableListOf (
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
        _binding= FragmentCameraBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        bitmapList=ArrayList()
        imgList= ArrayList()
        if (checkallPermission()) startCamera()
        else requestPermission()
        binding.captureBtn.setOnClickListener{ captureImage() }

        binding.saveBtn.setOnClickListener { createPdf(bitmapList, "sample ${randomNumberGeneratorForTest()}")}
        binding.homeBtn.setOnClickListener {
            val action=CameraFragmentDirections.actionCameraFragmentToHomeFragment()
            findNavController().navigate(action)
        }
        binding.saveBtn.setOnClickListener {
            if (imgList!!.isEmpty()) {
                Toast.makeText(requireContext(), "click some images", Toast.LENGTH_SHORT).show()
            } else {
//                val bundle = Bundle()
//                bundle.putParcelableArrayList("uri", imgList)
//
//                val receiverFragment = ImagePreviewFragment()
//                receiverFragment.apply { arguments=bundle }
                val imguri:Array<Uri> = imgList!!.toTypedArray()
                Toast.makeText(requireContext(), "${imguri.size}", Toast.LENGTH_SHORT).show()
                val action = CameraFragmentDirections.actionCameraFragmentToImagePreviewFragment(imguri)
                findNavController().navigate(action)
            }
        }


    }

//    private fun setclickedImageinRV() {
//        val adapter=ClickedImagePreviewAdapter(imgList)
//        binding.clickedImgRecyclerView.layoutManager= LinearLayoutManager(requireContext(),
//            RecyclerView.HORIZONTAL,false)
//        binding.clickedImgRecyclerView.adapter=adapter
//    }

    private
    fun checkallPermission() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode
        : Int,
        permissions
        : Array<String>,
        grantResults
        : IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (checkallPermission()) { startCamera() }
            else { Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun requestPermission() {
        val activityResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions())
            { permissions ->
                var permissionGranted = true

                permissions.entries.forEach {
                        if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                            permissionGranted = false
                    }
                if (!permissionGranted) Toast.makeText(requireContext(), "Permission denied",Toast.LENGTH_SHORT).show()
                else startCamera()
            }
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider : ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                    .build()
                    .also{ it.setSurfaceProvider(binding.viewFinder.surfaceProvider)}
           binding.flashAutoBtn.setOnClickListener {
               ImageCapture.Builder().setFlashMode(FLASH_MODE_AUTO)
           }

            imgCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try{
                    cameraProvider.unbindAll()
                 val cam= cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imgCapture)
                        val camControl=cam.cameraControl
                binding.flashOnBtn
                    .setOnClickListener {
                    camControl.enableTorch(true)
                }
                binding.flashOffBtn.setOnClickListener {
                    camControl.enableTorch(false)
                }
                }
            catch (exc: Exception){
                    Log.e(TAG, "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(requireContext()))
    }

    fun randomNumberGeneratorForTest(): Int = kotlin.random.Random.nextInt()

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
            Toast.makeText(requireContext(), "Click some images to proceed", Toast.LENGTH_SHORT).show()
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
            Log.w(TAG, "Error while creating Pdf: ${e}")
            Toast.makeText(requireContext(), "Failed to create PDF", Toast.LENGTH_SHORT).show()
        }


    }

    private fun captureImage() {
        var imgCapture = imgCapture ?: return
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imgCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(output : ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Log.d(TAG, msg)
                    val savedUri = output.savedUri
                    binding.ClickedImageIv.setImageURI(savedUri)
                    binding.ClickedImage.visibility=View.VISIBLE
                    savedUri?.let{
                        imgList?.add(it)
                    }
                    if(imgList?.size!=0)binding.saveBtn.visibility=View.VISIBLE

                    val capturedBitmap = savedUri?.let { getBitmapFromUri(it) }
                    capturedBitmap?.let { it->
                        bitmapList.add(it)
                    }
                }
                override fun onError(e: ImageCaptureException){
                    Log.e(TAG, "Photo capture failed: ${e.message}", e)
                }
            }
        )


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

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }
}