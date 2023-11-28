package com.example.androidproject

import java.io.Serializable

class Item(val itemId:String, val title: String, val userName: String, val userEmail: String, val posting:String, val price: Int, val sale:Boolean, val category:String) :
    Serializable {
}