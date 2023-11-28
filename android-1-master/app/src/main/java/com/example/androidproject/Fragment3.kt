package com.example.androidproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class Fragment3 : Fragment() {
    companion object {
        private const val ARG_USER_EMAIL = "user_email"

        fun newInstance(userEmail: String?): Fragment3 {
            val fragment = Fragment3()
            val args = Bundle()
            args.putString(ARG_USER_EMAIL, userEmail)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment3_layout, container, false)

        val textemail = view.findViewById<TextView>(R.id.textViewEmail)
        val textname = view.findViewById<TextView>(R.id.textViewName)
        val textBirth = view.findViewById<TextView>(R.id.textViewBirth)

        val userEmail = arguments?.getString(ARG_USER_EMAIL)

        textemail.text = userEmail
        val db = FirebaseFirestore.getInstance()
        val userCollection = db.collection("user")

        // userEmail이 일치하는 document 쿼리
        userCollection.whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // document에서 name과 birthday 가져와서 TextView에 설정
                    val name = document.getString("name")
                    println(name)
                    val birthday = document.getString("birthday")
                    println(birthday)

                    textname.text = name
                    textBirth.text = birthday
                }
            }
            .addOnFailureListener { exception ->
                println("오류###################################33")
            }
        return view
    }
}