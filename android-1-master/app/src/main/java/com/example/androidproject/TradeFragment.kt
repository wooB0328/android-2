package com.example.androidproject

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.MultiAutoCompleteTextView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidproject.databinding.TradelistLayoutBinding
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TradeFragment : Fragment(), ItemAdapter.OnItemClickListener {
    private lateinit var binding: TradelistLayoutBinding
    private lateinit var itemList: ArrayList<Item> //모든 판매글 리스트
    private lateinit var filterItemList: ArrayList<Item> //필터링 된 판매글 리스트
    private val checkedCategoryOptions = booleanArrayOf(true, true, true, true, true, true) //카테고리 필터 결과
    private var checkedSaleOption = "모두" //판매 보기 필터 결과
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TradelistLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //옵션 버튼 누르면 다이얼 로그 창 띄우기
        binding.optionButton.setOnClickListener {
            showOptionsDialog()
        }

        //아이템 리스트 초기화
        itemList = ArrayList()
        filterItemList = ArrayList()

        //리사이클러뷰에 필터링 된 아이템 리스트 연결
        binding.itemRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.itemRecyclerView.setHasFixedSize(true)
        val itemAdapter = ItemAdapter(filterItemList, this)
        binding.itemRecyclerView.adapter = itemAdapter

        //아이템 리스트 파이어스토어에서 가져오기
        loadItemFromFirestore()

        //글쓰기 버튼 누르면 글쓰기 페이지로 이동
        binding.addItem.setOnClickListener {
            binding.itemRecyclerView.smoothScrollToPosition(0)//스크롤 맨위로 올림
            val intent = Intent(requireContext(), ItemAddActivity::class.java)
            // 액티비티 시작
            startActivity(intent)
        }

    }
    override fun onItemClick(position: Int) { //판매 목록 클릭시 판매 정보 상세보기 페이지로 이동
        val intent = Intent(requireContext(), ItemDetailActivity::class.java)
        intent.putExtra("itemId", itemList[position].itemId)
        // 액티비티 시작
        startActivity(intent)
    }
    private fun loadItemFromFirestore(){//파이어 스토어에서 아이템 가져오기ㅣ
        val db: FirebaseFirestore = Firebase.firestore
        val itemCollectionRef = db.collection("item")

        //데이터 추가되면 리스트에 반영함
        itemCollectionRef.addSnapshotListener { snapshot, error ->
            if (snapshot != null) {
                for (doc in snapshot.documentChanges) {
                    val newItem = createItem(doc.document)
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {//데이터 추가 시 아이템리스트에 저장하고 필터링
                            itemList.add(0, newItem)
                            filtering(newItem)
                        }
                        DocumentChange.Type.MODIFIED ->{//데이터 수정 시 아이템 수정하고 아이템리스트 다시 필터링
                            val index = findItemIndex(newItem.itemId)
                            if(index>=0) {
                                itemList[index] = newItem
                                resetFilter()
                            }
                        }
                        DocumentChange.Type.REMOVED ->{//데이터 삭제 시 아이템 삭제하고 아이템리스트 다시 필터링
                            val index = findItemIndex(newItem.itemId)
                            if (index >= 0) {
                                itemList.removeAt(index)
                                resetFilter()
                            }
                        }
                    }
                }
                binding.itemRecyclerView.adapter?.notifyDataSetChanged() //리사이클러뷰에 변경 알림
            }
        }
    }

    private fun filtering(item:Item){ //아이템을 필터링해서 옵션값에 해당하면 filterItemList에 저장하는 함수
        var categoryFilterResult=false
        when(item.category){//item의 category가 무엇이지 확인한 후, checkedCategoryOptions의 해당 카테고리의 값이 true인지 비교
            "없음"-> categoryFilterResult = checkedCategoryOptions[0]
            "가구"-> categoryFilterResult = checkedCategoryOptions[1]
            "가전"-> categoryFilterResult = checkedCategoryOptions[2]
            "의류"-> categoryFilterResult = checkedCategoryOptions[3]
            "도서"-> categoryFilterResult = checkedCategoryOptions[4]
            "식품"-> categoryFilterResult = checkedCategoryOptions[5]
        }
        var saleFilterResult=false
        when(checkedSaleOption){//현재 옵션값이 무엇인지 확인하고 item의 sale이 해당하는지 확인
            "모두"-> saleFilterResult = true
            "판매중"-> saleFilterResult = item.sale
            "판매됨"-> saleFilterResult = !item.sale
        }
        if(categoryFilterResult&&saleFilterResult)//카테고리와 판매 필터 결과 둘 다 true면 filterItemList에 추가
            filterItemList.add(0, item)
    }

    private fun resetFilter(){//필터링된 아이템리스트를 초기화 후 다시 필터링해서 추가하는 함수
        filterItemList.clear()
        for(item in itemList.reversed()) //reversed는 원본 리스트의 순서를 변경시키지 않고 역순으로 만듬
            filtering(item)
    }
    private fun createItem(doc: DocumentSnapshot): Item{//스냅샷으로 아이템 객체 생성
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

    private fun findItemIndex(itemId: String): Int { //아이템id로 인덱스 찾기
        for (index in itemList.indices) {
            if (itemList[index].itemId.equals(itemId)) {
                return index
            }
        }
        return -1
    }

    private fun showOptionsDialog() { //옵션창 다이얼로그
        val view = layoutInflater.inflate(R.layout.option_dialog_layout, null)

        //체크박스
        val newCheckedOptions = checkedCategoryOptions.copyOf() //체크상태 깊은 복사 사용
        val checkBoxIds = arrayOf(R.id.checkBox1, R.id.checkBox2, R.id.checkBox3, R.id.checkBox4, R.id.checkBox5, R.id.checkBox6)
        //체크박스에 초기상태 변경 및 클릭리스너 부여
        checkBoxIds.forEachIndexed{index, checkBoxId->
            val tempCheckBox = view.findViewById<CheckBox>(checkBoxId)
            tempCheckBox.isChecked = newCheckedOptions[index]
            tempCheckBox.setOnClickListener {
                newCheckedOptions[index] = !newCheckedOptions[index]
            }
        }

        //라디오 버튼
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        var newCheckedSaleOption = checkedSaleOption
        //라디오 버튼 초기 상태 변경 및 클릭리스너 부여
        when(newCheckedSaleOption){
            "모두" -> radioGroup.check(R.id.radioButton1)
            "판매중" -> radioGroup.check(R.id.radioButton2)
            "판매됨" -> radioGroup.check(R.id.radioButton3)
        }
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val tempButton = view.findViewById<RadioButton>(checkedId)
            newCheckedSaleOption = tempButton.text.toString()
        }

        //다이얼로그 버튼 생성
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
            .setPositiveButton("확인") { _, _ -> //확인 누를 시 변경된 옵션 반영
                for (i in newCheckedOptions.indices) {
                    checkedCategoryOptions[i] = newCheckedOptions[i]
                }
                checkedSaleOption = newCheckedSaleOption
                resetFilter()
                binding.itemRecyclerView.adapter?.notifyDataSetChanged()
            }
            .setNegativeButton("취소", null)
        builder.create().show()
    }
}