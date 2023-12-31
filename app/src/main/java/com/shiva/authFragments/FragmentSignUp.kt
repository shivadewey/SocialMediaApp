package com.shiva.authFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.shiva.MainActivity
import com.shiva.data.User
import com.shiva.socialmediaapp.databinding.ActivityAuthenticationFragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore

class FragmentSignUp : Fragment() {

    private val binding by lazy {
        ActivityAuthenticationFragmentSignUpBinding.inflate(layoutInflater)
    }

    private lateinit var mFirestore: FirebaseFirestore

    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        binding.btnSignUp.setOnClickListener {
            if (checkFields()) {
                auth.createUserWithEmailAndPassword(binding.tietEmail.text.toString(), binding.tietPassword.text.toString())
                    .addOnSuccessListener { result ->
                        Toast.makeText(requireContext(), "Sign Up successful!", Toast.LENGTH_SHORT).show()

                            // add this user in fire-store
                            mFirestore
                                .collection("Users")
                                .document(result.user?.uid ?: "docTypeUser")
                                .set(
                                    User(fullName = binding.tietName.text.toString(),
                                    email = binding.tietEmail.text.toString()
                                ))
                                .addOnSuccessListener {
                                    // starting home intent
                                    activity?.startActivity(Intent(requireActivity(), MainActivity::class.java))
                                    activity?.finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                                }
                        }
                    .addOnFailureListener {
                        if (it is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(
                                requireContext(),
                                "Oops! Invalid Email!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(requireContext(), "SignUp failed!", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }


        return binding.root
    }

    private fun checkFields() : Boolean {
        // name check
        if (binding.tietName.text.toString().isEmpty()) {
            binding.tilName.error = "This field can't be empty"
        } else {
            binding.tilName.isErrorEnabled = false
        }

        // Email check
        if (binding.tietEmail.text.toString().isEmpty()) {
            binding.tilEmail.error = "This field can't be empty"
        } else {
            binding.tilEmail.isErrorEnabled = false
        }

        // Password cross-check
        if (binding.tietPassword.text.toString() != binding.tietRePassword.text.toString()) {
            binding.tilPassword.error = "Passwords don't match"
            binding.tilRePassword.error = "Passwords don't match"
        } else {
            binding.tilPassword.isErrorEnabled = false
            binding.tilRePassword.isErrorEnabled = false
        }
        if (binding.tietPassword.text.toString().length < 8) {
            binding.tilPassword.error = "Password length should be greater than 8 characters"
        }
        else {
            binding.tilPassword.isErrorEnabled = false
        }

        return binding.tietEmail.text.toString().isNotEmpty() &&
                binding.tietName.text.toString().isNotEmpty() &&
                binding.tietPassword.text.toString().isNotEmpty() &&
                binding.tietRePassword.text.toString().isNotEmpty()
    }
}