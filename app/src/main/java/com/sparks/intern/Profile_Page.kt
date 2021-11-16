package com.sparks.intern

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class Profile_Page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile__page)

        val firebaseAuth = FirebaseAuth.getInstance()

        FirebaseApp.initializeApp(this)

        val prof_img      = findViewById(R.id.profile_pic) as ImageView
        val textViewname = findViewById(R.id.Username) as TextView
        val textViewemail = findViewById(R.id.Email) as TextView
        val log_out_btn = findViewById(R.id.log_out) as TextView

        for(user in firebaseAuth.currentUser!!.providerData){

            if (user.providerId == "facebook.com") {
                val currentUser : String? = Firebase.auth.currentUser?.uid

                val fb_ref = FirebaseDatabase.getInstance().getReference("Users").child("Facebook").child(currentUser.toString())

                fb_ref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var fb_name = snapshot.child("fb_name").getValue()
                            var fb_email = snapshot.child("fb_email").getValue()
                            var fb_prof_img = snapshot.child("fb_pic").getValue().toString()


                            Picasso.with(applicationContext).load(fb_prof_img).into(prof_img)
                            textViewname.text = "$fb_name"
                            textViewemail.text = fb_email.toString()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
            else if(user.providerId == "google.com"){

                val currentUser : String? = Firebase.auth.currentUser?.uid

                val g_ref = FirebaseDatabase.getInstance().getReference("Users").child("Google").child(currentUser.toString())

                g_ref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var g_name = snapshot.child("g_name").getValue()
                            var g_email = snapshot.child("g_email").getValue()
                            var g_surname = snapshot.child("g_surname").getValue()
                            var g_prof_img = snapshot.child("g_pic").getValue().toString()


                            Picasso.with(this@Profile_Page).load(g_prof_img).into(prof_img)
                            textViewname.text = "$g_name" + " " +  "$g_surname"
                            textViewemail.text = g_email.toString()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }

        log_out_btn.setOnClickListener {

            Firebase.auth.signOut()

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(this,"Logged out successfully!", Toast.LENGTH_LONG).show()

        }
    }
}
