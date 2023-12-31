package com.shiva.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.shiva.data.IndividualPost
import com.shiva.data.Post
import com.shiva.data.User
import com.shiva.socialmediaapp.R
import com.shiva.socialmediaapp.databinding.ActivityMainFragmentPostBinding
import com.shiva.utils.Const
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FragmentPost : Fragment() {

    private val binding by lazy {
        ActivityMainFragmentPostBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var mFireStore: FirebaseFirestore
    private val mFirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private lateinit var userReference: DocumentReference
    private lateinit var imageReference: StorageReference
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = binding.root

        // references from firebase
        mFireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        imageReference = mFirebaseStorage.reference.child(Const.STORAGE_POSTS)
        userReference = mFireStore.collection(Const.FS_USERS).document(auth.currentUser?.uid ?: "")


        userReference.get()
            .addOnSuccessListener { data->
                data.toObject(User::class.java)?.let {
                    currentUser = it
                }
            }
            .addOnFailureListener {

            }

        var chosenPhoto: Uri? = null
        val profileImagePicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            chosenPhoto = uri
            chosenPhoto?.let{
                binding.ivPhoto.setImageURI(chosenPhoto)
                binding.tvError.visibility = View.GONE
            }
        }

        binding.btnSelectImage.setOnClickListener {
            // launching intent for picking image from gallery
            profileImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        binding.btnPost.setOnClickListener {
            if (chosenPhoto != null) {
                lifecycleScope.launch (Dispatchers.IO) {
                    uploadImage(chosenPhoto!!)
                }
            } else {
                binding.tvError.visibility = View.VISIBLE
            }
        }

        return view
    }

    private suspend fun uploadImage(chosenPhoto: Uri) {
        lifecycleScope.launch (Dispatchers.IO) {
            val ref = imageReference.child(UUID.randomUUID().toString())
            ref.putFile(chosenPhoto).await()
            val uri = ref.downloadUrl.await()
            val post = Post(
                imageUrl = uri.toString(),
                caption = binding.etCaption.text.toString(),
                createdAt = System.currentTimeMillis()
            )
            currentUser.posts.add(post)

            // setting the post in fireStore in user's account
            userReference.set(currentUser).addOnSuccessListener {
                Toast.makeText(context, "Posted Successfully!", Toast.LENGTH_SHORT).show()
                binding.etCaption.text.clear()
                binding.ivPhoto.setImageResource(R.mipmap.default_profile_pic)
            }.await()

            // setting the post in fireStore in 'posts' collection.
            mFireStore.collection(Const.FS_POSTS).document(post.createdAt.toString())
                .set(
                    IndividualPost(post = post,
                        userRef = auth.currentUser?.uid ?: "")
                ).await()
        }
    }
}