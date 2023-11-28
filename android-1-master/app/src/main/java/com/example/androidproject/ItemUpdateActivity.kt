package com.example.androidproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.MainActivity.Companion.userEmail
import com.example.androidproject.databinding.ItemAddBinding
import com.example.androidproject.databinding.ItemDetailBinding
import com.example.androidproject.databinding.ItemUpdateBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ItemUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ItemUpdateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ItemUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //intent 가져오기
        var authorEmail= ""
        val intent = intent
        val item = intent.getSerializableExtra("item") as? Item
        if (item != null) {
            binding.updatePageTitleEdit.setText(item.title)
            binding.updatePagePostingEdit.setText(item.posting)
            binding.updatePagePriceEdit.setText(item.price.toString())
            binding.updateSale.isChecked = item.sale
            val category = item.category
            var temp = R.id.updateCategory1//없음
            when(category){
                "가구"-> temp = R.id.updateCategory2
                "가전"-> temp = R.id.updateCategory3
                "의류"-> temp = R.id.updateCategory4
                "도서"-> temp = R.id.updateCategory5
                "식품"-> temp = R.id.updateCategory6
            }
            binding.updateCategoryGroup.check(temp)
        }

        //초기 판매중 상태 텍스트
        binding.updateSale.text = if (binding.updateSale.isChecked) "판매중" else "판매됨"
        //판매중 상태 스위치 눌릴 시 텍스트 변경
        binding.updateSale.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked)
                binding.updateSale.text="판매중"
            else
                binding.updateSale.text="판매됨"
        }

        binding.updatePageBackButton.setOnClickListener { finish() }

        binding.updateButton.setOnClickListener {
            if(binding.updatePageTitleEdit.text.isEmpty())
                Toast.makeText(this, "제목이 설정되지 않았습니다.", Toast.LENGTH_SHORT).show()
            else if(binding.updatePagePriceEdit.text.isEmpty())
                Toast.makeText(this, "가격이 설정되지 않았습니다.", Toast.LENGTH_SHORT).show()
            else if(binding.updatePagePostingEdit.text.isEmpty())
                Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            else {
                if (item != null) updateItemToFirestore(item)
                finish()
            }
        }
    }

    private fun updateItemToFirestore(item: Item){//파이어스토어에 아이템 수정
        val db: FirebaseFirestore = Firebase.firestore
        val itemCollectionRef = db.collection("item")

        val selectedCategoryId = binding.updateCategoryGroup.checkedRadioButtonId
        val selectedCategoryButton: RadioButton = findViewById(selectedCategoryId)
        val updateData = hashMapOf(
            "title" to binding.updatePageTitleEdit.text.toString(),
            "price" to binding.updatePagePriceEdit.text.toString(),
            "posting" to binding.updatePagePostingEdit.text.toString(),
            "category" to selectedCategoryButton.text.toString(),
            "sale" to binding.updateSale.isChecked
        )

        itemCollectionRef.document(item.itemId).update(updateData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "데이터가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}













