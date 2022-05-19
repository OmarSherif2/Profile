package com.example.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firestore.v1.FirestoreGrpc
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.internal.Intrinsics

class MainActivity : AppCompatActivity() {

    private lateinit var selectedImgUri : Uri

    private val fireStoreRef by lazy {
        FirebaseFirestore.getInstance()
    }

    private val phone : String by lazy {
       prefUser()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        checkStoragePermission()


        fireStoreRef.collection("users").document(phone).get().addOnSuccessListener {

            val image = it.get("profileImage").toString()
            val name = it.get("name").toString()
            val bio = it.get("bio").toString()


            nameText.setText(name)
            phoneText.text = phone
            bioText.setText(bio)
            Glide.with(this).load(image).into(profileImage)
        }

        .addOnFailureListener {
            Toast.makeText(this , it.message , Toast.LENGTH_LONG).show()
        }



        nameText.setOnEditorActionListener(object : TextView.OnEditorActionListener{
            override fun onEditorAction(view: TextView?, actionId: Int, event: KeyEvent?): Boolean {

                if (actionId == EditorInfo.IME_ACTION_DONE){

                    if (nameText.text.isNotEmpty()){

                        updateName(nameText.text.toString())
                        return true

                    } else {
                        Toast.makeText(this@MainActivity , "Please Enter Valid Name" , Toast.LENGTH_SHORT).show()
                    }

                }

                return true
            }

        })

        var currentName : String = ""

        enableDisableEditName.setOnClickListener{

            currentName = nameText.text.toString()

            enableDisableEditName.isVisible = false
            cancelEditName.isVisible = true
            nameText.isEnabled = true
            nameText.requestFocus()
        }

        cancelEditName.setOnClickListener {
            nameText.setText(currentName)
            nameText.isEnabled = false
            enableDisableEditName.isVisible = true
            cancelEditName.isVisible = false

        }

        profileImage.setOnClickListener {
            pickImg()

        }


        bioText.setOnEditorActionListener(object : TextView.OnEditorActionListener{
            override fun onEditorAction(view: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE){

                    if (bioText.text.isNotEmpty()){

                        updateBio(bioText.text.toString())
                        return true

                    } else {
                        Toast.makeText(this@MainActivity , "Please Enter Valid Name" , Toast.LENGTH_SHORT).show()
                    }

                }

                return true
            }

        })


        var currentBio : String = ""

        enableDisableEditBio.setOnClickListener{
            currentBio = bioText.text.toString()

            enableDisableEditBio.isVisible = false
            cancelEditBio.isVisible = true
            bioText.isEnabled = true
            bioText.requestFocus()
        }

        cancelEditBio.setOnClickListener {
            bioText.setText(currentBio)
            bioText.isEnabled = false
            enableDisableEditBio.isVisible = true
            cancelEditBio.isVisible = false

        }




    }

    private fun updateBio(bio: String) {

        fireStoreRef.collection("users").document(phone).update("bio" , bio).addOnSuccessListener {
            bioText.isEnabled = false
            enableDisableEditBio.isVisible = true
            cancelEditBio.isVisible = false
            Toast.makeText(this , "Bio is successfully updated" , Toast.LENGTH_LONG).show()
        }

            .addOnFailureListener {
                Toast.makeText(this , it.message , Toast.LENGTH_LONG).show()
            }

    }


    private fun prefUser() : String {
        val pref = this.getSharedPreferences("mypref", MODE_PRIVATE)
        return pref.getString("phone" , "012345678")!!
    }

    private fun updateName(name : String) {
        fireStoreRef.collection("users").document(phone).update("name" , name).addOnSuccessListener {
            nameText.isEnabled = false
            enableDisableEditName.isVisible = true
            cancelEditName.isVisible = false
            Toast.makeText(this , "Name is successfully updated" , Toast.LENGTH_LONG).show()
        }

        .addOnFailureListener {
            Toast.makeText(this , it.message , Toast.LENGTH_LONG).show()
        }
    }



    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == -1
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                101
            )
        } else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_LONG).show()
        }
    }

    //Bra 3nk
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Intrinsics.checkNotNullParameter(permissions, "permissions")
        Intrinsics.checkNotNullParameter(grantResults, "grantResults")
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == 0) {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_LONG).show()
            } else {
                onBackPressed()
            }
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null){
            selectedImgUri = data.data!!
            sendToFireBase()
        }
    }

    fun pickImg(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent , 3)
    }



    private fun sendToFireBase() {

        val storageRef = FirebaseStorage.getInstance().reference
        val riverRef = storageRef.child("imgProfile/$phone")


        riverRef.putFile(selectedImgUri)

            .addOnSuccessListener {

                riverRef.downloadUrl.addOnSuccessListener {

                    fireStoreRef.collection("users").document(phone).update("profileImage" , it).addOnSuccessListener {
                        Glide.with(this).load(selectedImgUri).into(profileImage)
                        Toast.makeText(this,"Image Successfully Updated", Toast.LENGTH_LONG).show()
                    }
                        .addOnFailureListener {
                            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        }
                }

                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }

            }

            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }

    }
}
