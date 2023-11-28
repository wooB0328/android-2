package com.example.androidproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginbuuton = findViewById<Button>(R.id.buttonlogin)
        loginbuuton.setOnClickListener {
            login()
        }

        val CreateIdbutton = findViewById<Button>(R.id.buttoncreateid)
        CreateIdbutton.setOnClickListener {
            val intent = Intent(this, CreateId_Activity::class.java)
            startActivity(intent)

        }
    }


    private fun login(){
        val edittextEmail = findViewById<EditText>(R.id.editTextEmail).text.toString()
        val edittextPassword = findViewById<EditText>(R.id.editTextPassword).text.toString()
        val retry = findViewById<TextView>(R.id.retry)

        if (edittextEmail.isNotEmpty() && edittextPassword.isNotEmpty()) {
            Firebase.auth.signInWithEmailAndPassword(edittextEmail, edittextPassword)
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("email", edittextEmail)
                        startActivity(intent)
                    } else {
                        retry.text = "이메일이나 비밀번호를 잘못 입력했습니다."
                    }
                }
        } else {
            retry.text = "이메일과 비밀번호를 입력해주세요."
        }
    }
}