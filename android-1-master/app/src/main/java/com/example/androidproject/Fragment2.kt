package com.example.androidproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Fragment2 : Fragment() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment2_layout, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ChatListAdapter(emptyList()) { clickedItem ->
            //아이템 클릭 이벤트
            println(clickedItem.leftMessage)
            val intent = Intent(requireContext(), ChatMessageActivity::class.java)
            intent.putExtra("leftMessage", clickedItem.leftMessage)
            requireContext().startActivity(intent)
        }
        // Firestore에서 데이터 가져오기
        recyclerView.adapter = adapter
        fetchDataFromFirestore()
    }
    override fun onResume() {
        super.onResume()
        fetchDataFromFirestore()
    }

    //2번 프레그먼트에 파이어베이스 아이템 나열하는 코드
    private fun fetchDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val userEmail = MainActivity.userEmail.toString() // 사용자 이메일 가져오기

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = db.collection("user")
                    .document(userEmail)
                    .collection("message")
                    .get()
                    .await()

                val chatList = mutableListOf<ChatList>()

                for (document in querySnapshot) {
                    val leftMessage = document.id
                    val rightMessage = "이 사용자와 채팅하기" // 여기에서 고정된 메시지를 사용하거나 Firestore에서 가져올 필요가 있습니다.
                    chatList.add(ChatList(leftMessage, rightMessage))
                }

                // 데이터가 준비되면 RecyclerView에 설정
                adapter.setChatList(chatList)
            } catch (e: Exception) {
                // 에러 처리
                e.printStackTrace()
            }
        }
    }
}

fun refreshFragment(fragment: Fragment, fragmentManager: FragmentManager) {
    var ft: FragmentTransaction = fragmentManager.beginTransaction()
    ft.detach(fragment).attach(fragment).commit()
}
