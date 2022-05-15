package com.example.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.FirestoreGrpc
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val fireStoreRef by lazy {
        FirebaseFirestore.getInstance()
    }

    private val phone : String? by lazy {
        prefUser("phone")
     }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fireStoreRef.collection("users").document(phone!!).get().addOnSuccessListener {
            var image = it.get("profileImage").toString()
            var name = it.get("name").toString()
            var bio = it.get("bio").toString()

            Glide.with(this).load(image).into(profileImage)
            nameText.text = name
            phoneText.text = phone
            bioText.text = bio
        }

        .addOnFailureListener {
            Toast.makeText(this , it.message , Toast.LENGTH_LONG).show()
        }
    }


    fun prefUser(key : String): String? {
        val pref = this.getSharedPreferences("mypref", AppCompatActivity.MODE_PRIVATE)
        return pref.getString("phone" , "012345678")
    }
}
