package com.example.androidproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ChatMessageActivity : AppCompatActivity() {
    private lateinit var adapter: ChatMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_message_layout)

        //fetchDataFromFirestore()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // 어댑터 초기화
        adapter = ChatMessageAdapter(emptyList()) // 초기에는 빈 데이터로 생성
        recyclerView.adapter = adapter

        fetchDataFromFirestore()

        val editTextMessage = findViewById<EditText>(R.id.editTextMessage)
        val buttonSendMessage = findViewById<Button>(R.id.buttonSendMessage)
        val buttonback = findViewById<Button>(R.id.buttonback)

        buttonSendMessage.setOnClickListener {
            val newMessage = editTextMessage.text.toString().trim()
            if (newMessage.isNotEmpty()) {
                sendMessage(newMessage)
                editTextMessage.text.clear()
            }
        }
        buttonback.setOnClickListener{
            finish()
        }
    }

    private fun fetchDataFromFirestore() {
        val targetemail = intent.getStringExtra("leftMessage").toString()
        val db = FirebaseFirestore.getInstance()
        val userDocument = MainActivity.userEmail.toString()
        //val userDocument = "aaa@a.com" // 사용자 이메일
        val messageDocument = targetemail // 메시지를 가져올 대상 이메일

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = db.collection("user")
                    .document(userDocument)
                    .collection("message")
                    .document(messageDocument)
                    .get()
                    .await()

                // "note" 필드의 값을 가져오기
                val noteArray = querySnapshot.get("note") as? List<String>

                if (noteArray != null) {
                    // note 필드가 String Array인 경우
                    val messageList = noteArray.map { processMessage(it) }

                        runOnUiThread {
                            // UI 작업은 runOnUiThread 내에서 수행
                            adapter.setData(messageList)
                        }


//                    // RecyclerView 초기화
//                    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
//                    val layoutManager = LinearLayoutManager(this@ChatMessageActivity)
//
//                    // 어댑터 설정
//                    val adapter = ChatMessageAdapter(messageList)
//                    adapter.notifyDataSetChanged()
//
//                    recyclerView.adapter=adapter
//                    recyclerView.layoutManager=LinearLayoutManager(this@ChatMessageActivity, LinearLayoutManager.VERTICAL, false)

                }

            } catch (e: Exception) {
                // 에러 처리
                e.printStackTrace()
            }
        }
    }


    //메시지 보내는 함수
    private fun sendMessage(message: String) {
        val targetemail = intent.getStringExtra("leftMessage").toString()
        val db = FirebaseFirestore.getInstance()
        val userDocument = MainActivity.userEmail.toString()
        val messageDocument = targetemail

        GlobalScope.launch(Dispatchers.Main) {
            try {
                //내 DB에 쓰기
                val writeMe = db.collection("user")
                    .document(userDocument)
                    .collection("message")
                    .document(messageDocument)
                    .get()
                    .await()

                var noteArrayMe = writeMe.get("note") as? MutableList<String>

                // 새 메시지 추가
                if (noteArrayMe != null) {
                    noteArrayMe.add("1$message") // "1"로 시작하는 경우는 나의 메시지
                } else {
                    noteArrayMe = mutableListOf("1$message")
                }

                // 업데이트된 메시지를 Firestore에 저장
                db.collection("user")
                    .document(userDocument)
                    .collection("message")
                    .document(messageDocument)
                    .update("note", noteArrayMe)
                    .await()

                //상대방 DB에 쓰기
                val writeyou = db.collection("user")
                    .document(messageDocument)
                    .collection("message")
                    .document(userDocument)
                    .get()
                    .await()

                var noteArrayyou = writeyou.get("note") as? MutableList<String>

                // 새 메시지 추가
                if (noteArrayyou != null) {
                    noteArrayyou.add("0$message") // "0"로 시작하는 경우는 상대방의 메시지
                } else {
                    noteArrayyou = mutableListOf("0$message")
                }

                // 업데이트된 메시지를 Firestore에 저장
                db.collection("user")
                    .document(messageDocument)
                    .collection("message")
                    .document(userDocument)
                    .update("note", noteArrayyou)
                    .await()

                // 메시지를 화면에 표시하기 위해 RecyclerView 갱신
                fetchDataFromFirestore()

            } catch (e: Exception) {
                // 에러 처리
                e.printStackTrace()
            }
        }
    }


}
private fun processMessage(message: String): ChatMessage {
    // "0"로 시작하면 "상대방 : " 추가, "1"로 시작하면 "나 : " 추가
    val processedMessage = if (message.startsWith("0")) {
        "상대방 : ${message.substring(1)}"
    } else if (message.startsWith("1")) {
        "나 : ${message.substring(1)}"
    } else {
        message
    }

    return ChatMessage(processedMessage)
}
