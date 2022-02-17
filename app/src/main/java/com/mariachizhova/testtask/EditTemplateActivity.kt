package com.mariachizhova.testtask

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.mariachizhova.testtask.databinding.ActivityEditTemplateBinding
import android.os.Bundle
import android.widget.Toast


class EditTemplateActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityEditTemplateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityEditTemplateBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.submit.setOnClickListener {
            val inputTemplate = mBinding.inputTemplate.text.toString()
            Toast.makeText(this, "Input template: $inputTemplate", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("inputTemplate", inputTemplate)
            startActivity(intent)
        }
    }
}