package com.example.androidproject

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide.init

//리사이클러뷰에 데이터를 제공하는 어답터 정의
class ItemAdapter(val itemList:ArrayList<Item>, val listener: OnItemClickListener) : RecyclerView.Adapter<ItemAdapter.CustomViewHolder>() {
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false) //item_list의 정보 가져와서 뷰에 저장
        return CustomViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ItemAdapter.CustomViewHolder, position: Int) {
        holder.name.text = itemList.get(position).title
        holder.price.text = itemList.get(position).price.toString()
        holder.category.text = itemList.get(position).category
        holder.posting.text = itemList.get(position).posting
        holder.userName.text = itemList.get(position).userName
        // sale 값이 false면 TextView의 글자 색상 회색
        if (itemList[position].sale) {
            holder.name.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.price.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.category.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.posting.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.sale.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.userName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.sale.text = "판매중"
        } else {
            holder.name.setTextColor(Color.parseColor("#868e96"))
            holder.price.setTextColor(Color.parseColor("#868e96"))
            holder.category.setTextColor(Color.parseColor("#868e96"))
            holder.posting.setTextColor(Color.parseColor("#868e96"))
            holder.sale.setTextColor(Color.parseColor("#868e96"))
            holder.userName.setTextColor(Color.parseColor("#868e96"))
            holder.sale.text="판매됨"
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    class CustomViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.item_title)
        val price = itemView.findViewById<TextView>(R.id.item_price)
        val category = itemView.findViewById<TextView>(R.id.item_category)
        val posting = itemView.findViewById<TextView>(R.id.item_posting)
        val userName = itemView.findViewById<TextView>(R.id.item_user_name)
        val sale = itemView.findViewById<TextView>(R.id.item_sale)
        init {
            itemView.setOnClickListener {//클릭된 아이템의 위치를 알려줌
                listener.onItemClick(adapterPosition)
            }
        }

    }
}