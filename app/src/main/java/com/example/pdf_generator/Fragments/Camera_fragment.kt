package com.example.pdf_generator.Fragments
import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pdf_generator.adapters.clickedImagePreviewAdapter
import com.example.pdf_generator.databinding.FragmentCameraBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Camera_fragment : Fragment()
{
    private var _binding:FragmentCameraBinding?=null
    private val binding get()=_binding!!

    private lateinit var bitmapList:ArrayList<Bitmap>
    private lateinit var imgList:ArrayList<Uri>
    private var imgCapture:ImageCapture?=null
    private val REQUEST_CODE = 20

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
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
        bitmapList=ArrayList<Bitmap>()
        imgList= ArrayList()
        if (checkallPermission())
    {
        startCamera()
    }
    else {
        requestPermission()
    }

        binding.captureBtn.setOnClickListener{

        captureImage()

        }




    }

    private fun setclickedImageinRV() {
        val adapter=clickedImagePreviewAdapter(imgList)
        binding.clickedImgRecyclerView.layoutManager= LinearLayoutManager(requireContext(),
            RecyclerView.HORIZONTAL,false)
        binding.clickedImgRecyclerView.adapter=adapter
    }

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
        if (requestCode == REQUEST_CODE)
        {
            if (checkallPermission())
            {
                startCamera()
            }
            else
            {
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermission() {
        val activityResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions())
            { permissions ->
                // Handle Permission granted/rejected
                var permissionGranted = true
                permissions.entries.forEach {
                    if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                        permissionGranted = false
                }
                if (!permissionGranted) {
                    Toast.makeText(requireContext(),
                        "Permission denied",
                        Toast.LENGTH_SHORT).show()
                } else {
                    startCamera()
                }
            }
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private
    fun startCamera()
    {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(
            {

                val cameraProvider : ProcessCameraProvider = cameraProviderFuture.get()


                val preview = Preview.Builder()
                    .build()
                    .also{
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)}

                imgCapture = ImageCapture.Builder().build()


                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try
                {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to Camera_fragment
                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imgCapture)
                }
                catch (exc
                       : Exception)
                {
                    Log.e(TAG, "Use case binding failed", exc)
                }
            },
            ContextCompat.getMainExecutor(requireContext()))
    }
    private
    fun captureImage()
    {
        val imgCapture = imgCapture ?: return
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
                /** Called when an image has been successfully saved.  */
        override fun onImageSaved(output : ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
//                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    Log.d(TAG, msg)
                    val savedUri = output.savedUri
                    savedUri?.let{
                        imgList.add(it)
                    }
                    setclickedImageinRV()
                    val capturedBitmap = savedUri?.let { getBitmapFromUri(it) }


                    capturedBitmap?.let { it->
                        bitmapList.add(it)

                    }
        }

            override fun onError(e: ImageCaptureException){
                Log.e(TAG, "Photo capture failed: ${e.message}", e)
            }})
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