package com.sparks.intern

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.facebook.*
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity()

{
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    val Req_Code: Int = 123
    var callbackManager = CallbackManager.Factory.create();
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = getInstance()

        FirebaseApp.initializeApp(this)

        fb_login_button.setPermissions("email", "public_profile")
        fb_login_button.setOnClickListener {
            Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
            fbsignIn()
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        google_sign_in_button.setOnClickListener {
            signInGoogle()
        }
    }
    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }
    private fun fbsignIn() {
        fb_login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginresult: LoginResult) {

                handleFacebookAccessToken(loginresult.accessToken)

            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {

            }

        })
    }
    private fun handleFacebookAccessToken(accessToken: AccessToken) {

        val ref  = FirebaseDatabase.getInstance().getReference("Users").child("Facebook")

        val credential  = FacebookAuthProvider.getCredential(accessToken.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener{ task->

                val request = GraphRequest.newMeRequest(
                    accessToken, GraphRequest.GraphJSONObjectCallback
                    { `object`, response ->

                        val fbName = `object`.getString("name")

                        val fbEmail = `object`.getString("email")

                        var fbProfilePicURL = `object`.getJSONObject("picture").getJSONObject("data").getString("url")

                        val fbUpload = FB_data(fbName.toString(), fbEmail.toString(), fbProfilePicURL.toString())

                        val currentUser : String? = Firebase.auth.currentUser?.uid

                        try {
                            ref.child(currentUser.toString()).setValue(fbUpload).addOnCompleteListener {

                                Toast.makeText(this, "data saved successfully", Toast.LENGTH_LONG).show()

                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()

                        }

                        val baos = ByteArrayOutputStream()

                        val image = baos.toByteArray()

                        val storageRef = FirebaseStorage.getInstance("gs://sparks-task5.appspot.com/").reference.child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")


                        val upload = storageRef.putBytes(image)

                        upload.addOnCompleteListener { uploadTask ->
                            if (uploadTask.isSuccessful) {
                                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                                    urlTask.result?.let {
                                        fbProfilePicURL = it.toString()

                                    }

                                }
                            } else {
                                Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show()
                            }
                        }


                    })
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email,picture.type(large)")
                request.parameters = parameters
                request.executeAsync()

                val intent = Intent(this, Profile_Page::class.java)
                startActivity(intent)

            }
                .addOnFailureListener { e->
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }
    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                Google_auth(account)

            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    private fun Google_auth(account: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val googleFirstName = account?.givenName ?: ""

                val googleLastName = account?.familyName ?: ""

                val googleEmail = account?.email ?: ""

                var googleProfilePicURL = account?.photoUrl.toString()

                val ref  = FirebaseDatabase.getInstance().getReference("Users").child("Google")

                val currentUser : String? = Firebase.auth.currentUser?.uid

                val Google_Upload= Google_data(googleFirstName, googleLastName, googleEmail, googleProfilePicURL)

                try {
                    ref.child(currentUser.toString()).setValue(Google_Upload).addOnCompleteListener {

                        Toast.makeText(this, "data saved successfully", Toast.LENGTH_LONG).show()

                    }
                }
                catch (e: Exception)
                {
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()

                }

                val baos = ByteArrayOutputStream()

                val image = baos.toByteArray()

                val storageRef = FirebaseStorage.getInstance().reference.child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")


                val upload = storageRef.putBytes(image)

                upload.addOnCompleteListener{ uploadTask->
                    if (uploadTask.isSuccessful)
                    {
                        storageRef.downloadUrl.addOnCompleteListener{ urlTask->
                            urlTask.result?.let {
                                googleProfilePicURL = it.toString()

                            }

                        }
                    }
                    else{
                        Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show()
                    }
                }


                val intent = Intent(this, Profile_Page::class.java)
                startActivity(intent)

            }

        }
    }
}