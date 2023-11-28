package com.example.androidproject

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getSystemService
import com.example.androidproject.databinding.ItemDetailBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ItemDetailActivity : AppCompatActivity() {
    private lateinit var binding: ItemDetailBinding
    private val userEmail: String?
        get() = MainActivity.userEmail?.toString()
    private var item: Item? = null
    private var snapshotListener: ListenerRegistration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //intent 가져오기
        val intent = intent
        val itemId = intent.getSerializableExtra("itemId") as? String
        if (itemId != null) {
            loadItemFromFirestore(itemId) //itemId로 파이어베이스에서 실시간으로 데이터 가져오기, intent로 item이 아닌 itemId를 받는 이유는 실시간으로 데이터 변경을 적용 시키기 위해
        }

        //뒤로가기 버튼
        binding.detailBackButton.setOnClickListener {
            finish()//현재 액티비티 종료
        }
        //채팅하기 버튼
        binding.detailMessageButton.setOnClickListener {
            //여기에 채팅 다이알로그 입력
            showDialog(item?.userEmail.toString(), userEmail.toString())
            println("아이디는#######################" + userEmail.toString())
        }

        //수정 버튼 누르면 update페이지로 이동
        binding.detailUpdateButton.setOnClickListener {
            val updateIntent = Intent(this@ItemDetailActivity, ItemUpdateActivity::class.java)
            // Intent에 아이템 정보 추가
            updateIntent.putExtra("item", item)
            startActivity(updateIntent)
        }
        //삭제 버튼
        binding.detailDeleteButton.setOnClickListener {
            if(itemId!=null) deleteItemToFirestore(itemId)
        }

    }
    private fun loadItemFromFirestore(itemId:String) { //아이템id로 파이어스토어에서 데이터 가져옴
        val db: FirebaseFirestore = Firebase.firestore
        val itemDocumentRef = db.collection("item").document(itemId)
        snapshotListener = itemDocumentRef.addSnapshotListener { snapshot, error ->
            if (snapshot != null) {
                item = createItem(snapshot)
                binding.detailTitle.setText(item?.title)
                binding.detailPosting.setText(item?.posting)
                binding.detailPrice.setText("가격: ${item?.price}원")
                binding.detailUserName.setText("작성자: ${item?.userName}")
                binding.detailUserEmail.setText("이메일: ${item?.userEmail}")
                if(item?.sale == true) binding.detailSale.setText("판매중")
                else binding.detailSale.setText("판매됨")
                binding.detailUpdateButton.isEnabled =
                    userEmail?.equals(item?.userEmail) == true //작성자 이메일과 로그인한 이메일 비교해 수정,삭제,메세지 버튼 활성화 관리
                binding.detailDeleteButton.isEnabled =
                    userEmail?.equals(item?.userEmail) == true
                binding.detailMessageButton.isEnabled =
                    userEmail?.equals(item?.userEmail) == false
            }
        }
    }

    private fun deleteItemToFirestore(itemId: String){//아이템 삭제
        val db: FirebaseFirestore = Firebase.firestore
        val itemDocumentRef = db.collection("item").document(itemId)
        snapshotListener?.remove()
        itemDocumentRef.delete().addOnSuccessListener {
            Toast.makeText(this, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun createItem(doc: DocumentSnapshot): Item{//아이템 객체 생성
        val id = doc.id
        val title = doc["title"].toString()
        val email = doc["userEmail"].toString()
        val name = doc["userName"].toString()
        val posting = doc["posting"].toString()
        val price = doc["price"].toString().toInt()
        val sale = doc["sale"].toString().toBoolean()
        val category = doc["category"].toString()
        return Item(id, title, name, email, posting, price, sale, category)
    }

    //다이알로그 표시하는 함수
    private fun showDialog(email: String, userEmail : String) {
        // 다이얼로그 레이아웃을 설정합니다.
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_layout, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editText)

        // 다이얼로그를 생성합니다.
        val builder = AlertDialog.Builder(this)
        builder.setTitle("알림")  // 다이얼로그 제목 설정
        builder.setView(dialogLayout)  // 다이얼로그에 레이아웃 설정

        // "보내기" 버튼을 눌렀을 때의 동작을 정의합니다.
        builder.setPositiveButton("보내기") { dialog, which ->
            // 여기에 메시지를 보내는 코드를 추가합니다.
            val message = editText.text.toString()
            if (message.isNotEmpty()) {
                println("보낸 메시지: $message")
                addToFirestore(email, userEmail, message)
                // 여기에서 실제로 메시지를 보내는 동작을 추가할 수 있습니다.
                // 예를 들어, 네트워크 요청 등을 사용하여 메시지를 보낼 수 있습니다.
            } else {
                Toast.makeText(this, "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
            //키보드 떠있을 때 보내기 누르면 오류나는데, 그거 잡아주는 코드
        }

        // "닫기" 버튼을 눌렀을 때의 동작을 정의합니다.
        builder.setNegativeButton("닫기") { dialog, which ->
            // 다이얼로그를 닫습니다.
            dialog.dismiss()
        }

        // 다이얼로그를 화면에 표시합니다.
        val dialog = builder.create()
        dialog.show()
    }


    //메시지 파이어베이스에 저장하는 함수  0으로 시작하면 받은거, 1로 시작하면 보낸거.
    private fun addToFirestore(email: String, userEmail : String, message: String) {
        // Firestore에 접근하기 위한 인스턴스를 가져옵니다.
        val db = FirebaseFirestore.getInstance()

        // user 컬렉션에 접근합니다.
        val userCollection = db.collection("user")

        // 이메일을 사용하여 해당 사용자의 문서에 접근합니다.
        val userDocument = userCollection.document(email)
        val userDocument2 = userCollection.document(userEmail)


        // 판매자 데이터베이스 수정. 0으로 시작하면 받은거
        userDocument.collection("message").document(userEmail).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // document가 이미 존재할 경우
                    val existingMessages = documentSnapshot.get("note") as? ArrayList<String>

                    if (existingMessages != null) {
                        // 기존 메시지가 있는 경우 기존 배열에 새로운 메시지를 추가합니다.
                        existingMessages.add("0$message")

                        // 기존 배열을 업데이트합니다.
                        userDocument.collection("message").document(userEmail)
                            .update("note", existingMessages)
                            .addOnSuccessListener {
                                Toast.makeText(this, "메시지가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "메시지 업데이트 실패: $e", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // 기존 배열이 없는 경우 새로운 배열을 생성하고 메시지를 추가합니다.
                        val newMessages = arrayListOf("0$message")

                        // 새로운 배열을 저장합니다.
                        userDocument.collection("message").document(userEmail)
                            .set(mapOf("note" to newMessages))
                            .addOnSuccessListener {
                                Toast.makeText(this, "메시지가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "메시지 저장 실패: $e", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // document가 존재하지 않는 경우
                    val newMessages = arrayListOf("0$message")

                    // 새로운 배열을 저장합니다.
                    userDocument.collection("message").document(userEmail)
                        .set(mapOf("note" to newMessages))
                        .addOnSuccessListener {
                            Toast.makeText(this, "메시지가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "메시지 저장 실패: $e", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "메시지 저장 실패: $e", Toast.LENGTH_SHORT).show()
            }

        //구매자 데이터베이스 수정
        userDocument2.collection("message").document(email).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // document가 이미 존재할 경우
                    val existingMessages = documentSnapshot.get("note") as? ArrayList<String>

                    if (existingMessages != null) {
                        // 기존 메시지가 있는 경우 기존 배열에 새로운 메시지를 추가합니다.
                        existingMessages.add("1$message")

                        // 기존 배열을 업데이트합니다.
                        userDocument2.collection("message").document(email)
                            .update("note", existingMessages)
                            .addOnSuccessListener {
                                Toast.makeText(this, "메시지가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "메시지 업데이트 실패: $e", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // 기존 배열이 없는 경우 새로운 배열을 생성하고 메시지를 추가합니다.
                        val newMessages = arrayListOf("1$message")

                        // 새로운 배열을 저장합니다.
                        userDocument2.collection("message").document(email)
                            .set(mapOf("note" to newMessages))
                            .addOnSuccessListener {
                                Toast.makeText(this, "메시지가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "메시지 저장 실패: $e", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // document가 존재하지 않는 경우
                    val newMessages = arrayListOf("1$message")

                    // 새로운 배열을 저장합니다.
                    userDocument2.collection("message").document(email)
                        .set(mapOf("note" to newMessages))
                        .addOnSuccessListener {
                            Toast.makeText(this, "메시지가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "메시지 저장 실패: $e", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "메시지 저장 실패: $e", Toast.LENGTH_SHORT).show()
            }



    }

}