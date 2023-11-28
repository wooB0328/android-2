package com.example.androidproject

import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.databinding.ItemAddBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ItemAddActivity : AppCompatActivity(){
    private val userEmail: String?
        get() = MainActivity.userEmail?.toString()
    private lateinit var binding: ItemAddBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ItemAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addPageBackButton.setOnClickListener {
            finish()//현재 액티비티 종료
        }
        binding.addButton.setOnClickListener {
            if(binding.addPageTitleEdit.text.isEmpty())
                Toast.makeText(this, "제목이 설정되지 않았습니다.", Toast.LENGTH_SHORT).show()
            else if(binding.addPagePriceEdit.text.isEmpty())
                Toast.makeText(this, "가격이 설정되지 않았습니다.", Toast.LENGTH_SHORT).show()
            else if(binding.addPagePostingEdit.text.isEmpty())
                Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            else {
                addItemToFirestore()
                finish()//현재 액티비티 종료
            }
        }
    }
    private fun addItemToFirestore() {
        val db:FirebaseFirestore = Firebase.firestore

        //유저 이메일로 유저 네임을 알아낸다.
        var userName = ""
        val userCollectionRef = db.collection("user")
        userCollectionRef.document(userEmail.toString()).get()
            .addOnSuccessListener {
                userName = it["name"].toString();
                // 선택된 카테고리의 텍스트 가져오기
                val selectedCategoryId = binding.addCategoryGroup.checkedRadioButtonId
                val selectedCategoryButton: RadioButton = findViewById(selectedCategoryId)

                //아이템 정보를 파이어스토어에 저장한다.
                val itemCollectionRef = db.collection("item")
                //editText는 Editable 타입을 반환하기 때문에 toString이 필요함
                val item = hashMapOf(
                    "title" to binding.addPageTitleEdit.text.toString(),
                    "price" to binding.addPagePriceEdit.text.toString(),
                    "posting" to binding.addPagePostingEdit.text.toString(),
                    "userEmail" to userEmail,
                    "userName" to userName,
                    "category" to selectedCategoryButton.text.toString(),
                    "sale" to true,
                    )
                //아이템 추가 요청
                itemCollectionRef.add(item)
                    .addOnSuccessListener {
                        Toast.makeText(this, "데이터가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "데이터 저장 실패: $e", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "유저정보 불러오기 실패: $e", Toast.LENGTH_SHORT).show()
            }
    }
}



















