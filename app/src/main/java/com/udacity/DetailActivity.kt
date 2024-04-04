package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        if(intent?.extras != null){
            binding.detailContent.fileNameTXT.text = intent.getStringExtra("fileName")
            binding.detailContent.statusTxt.text = intent.getStringExtra("downloadStatus")
        }

        binding.fab.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }


    }
}
