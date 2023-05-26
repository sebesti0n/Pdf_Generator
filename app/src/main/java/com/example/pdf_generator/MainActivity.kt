package com.example.pdf_generator


import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.pdf_generator.Fragments.Camera_fragment
import com.example.pdf_generator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.cameraLaunchBtn.setOnClickListener{
            binding.cameraLaunchBtn.visibility=View.INVISIBLE
            val transaction=supportFragmentManager.beginTransaction()
            transaction.replace(R.id.camera_fragment_container,Camera_fragment())
            transaction.commit()
//            val i= Intent(this,CameraActivity::class.java)
//            startActivity(i)
//            finish()
        }

    }
}