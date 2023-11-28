package com.example.androidproject

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


class CreateId_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_id)

        val datebutton = findViewById<Button>(R.id.dateButton)
        val createbutton = findViewById<Button>(R.id.buttonCreate)
        datebutton.setOnClickListener {
            showDatePickerDialog()
        }

        createbutton.setOnClickListener {
            CreateUser()
        }


        val backbutton = findViewById<Button>(R.id.buttonback) //뒤로가기 버튼
        backbutton.setOnClickListener {
            onBackPressed()
        }

    }
    private fun isEmailValid(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return email.matches(emailRegex.toRegex())
    }

    private fun CreateUser() {
        val email = findViewById<EditText>(R.id.editTextCreateEmail).text.toString()
        val password = findViewById<EditText>(R.id.editTextCreatePassword).text.toString()
        val password2 = findViewById<EditText>(R.id.editTextCreatePassword2).text.toString()
        val name = findViewById<EditText>(R.id.editTextName).text.toString()
        val birthday = findViewById<EditText>(R.id.editTextDate).text.toString()
        val explain = findViewById<TextView>(R.id.explain)

        if (email.isEmpty()) {
            explain.text = "이메일을 입력하세요"
        } else if (password.isEmpty()) {
            explain.text = "비밀번호를 입력하세요"
        }  else if (!isEmailValid(email)) {
            explain.text = "이메일 형식으로 입력해 주세요"
        } else if (password2.isEmpty()) {
            explain.text = "비밀번호 확인을 입력하세요"
        } else if (password.length < 6) {
            explain.text = "비밀번호를 6자 이상 입력하세요."
        } else if (name.isEmpty()) {
            explain.text = "이름을 입력하세요"
        } else if (birthday.isEmpty()) {
            explain.text = "생일을 입력하세요"
        } else if (email.isNotEmpty() && password.isNotEmpty() && password2.isNotEmpty()) {
            if (password == password2) {
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) { // 성공했을 때
                            val db: FirebaseFirestore = Firebase.firestore
                            val userDocumentRef = db.collection("user").document(email)

                            // 유저 문서를 생성하고 필요한 컬렉션을 추가합니다.
                            val userMap = hashMapOf(
                                "email" to email,
                                "password" to password,
                                "name" to name,
                                "birthday" to birthday
                            )
                            userDocumentRef.set(userMap)
                                .addOnSuccessListener {
                                    // 유저 문서 생성 성공 시 메시지 컬렉션을 추가합니다.
                                    val messageCollectionRef = userDocumentRef.collection("message")
                                    // 동적으로 생성된 문서의 이름을 유저 이메일로 지정
                                    val userMessageDocumentRef = messageCollectionRef.document(email)
                                    val initialMessage = hashMapOf(
                                        "note" to arrayListOf<String>() // 초기 메시지는 빈 배열
                                    )
                                    userMessageDocumentRef.set(initialMessage)
                                        .addOnSuccessListener {
                                            explain.text = "회원가입이 완료되었습니다."
                                        }
                                        .addOnFailureListener { e ->
                                            explain.text = "메시지 컬렉션 추가에 실패했습니다. $e"
                                        }
                                }
                                .addOnFailureListener { e ->
                                    explain.text = "회원가입에 실패했습니다. $e"
                                }
                        } else { // 실패했을 때
                            if (task.exception is FirebaseAuthUserCollisionException) {
                                explain.text = "중복된 이메일입니다."
                            }
                        }
                    }
            }
        }
    }


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateDateInView(selectedDate)
            },
            year,
            month,
            day
        )

        datePicker.show()
    }

    private fun updateDateInView(selectedDate: Calendar) {
        val myFormat = "yyyy-MM-dd" // 원하는 날짜 형식
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        val formattedDate = sdf.format(selectedDate.time)

        val editDate = findViewById<EditText>(R.id.editTextDate)
        editDate.setText(formattedDate)
    }
}

