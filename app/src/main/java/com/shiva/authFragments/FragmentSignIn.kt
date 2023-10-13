package com.shiva.authFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.shiva.MainActivity
import com.shiva.socialmediaapp.databinding.ActivityAuthenticationFragmentSignInBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class FragmentSignIn : Fragment() {

    private val binding by lazy {
        ActivityAuthenticationFragmentSignInBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        Log.d("withoutSignIn", auth.currentUser?.uid.toString())

        binding.btnSignIn.setOnClickListener {
            // fetching email and pass from UI
            val email = binding.tietEmail.text.toString()
            val password = binding.tietPassword.text.toString()

            // email field null check
            if (email.isNullOrBlank()) {
                binding.tilEmail.error = "This field can't be empty"
            }
            else {
                binding.tilEmail.isErrorEnabled = false
            }

            // password field null check
            if (password.isNullOrBlank()) {
                binding.tilPassword.error = "This field can't be empty"
            }
            else {
                binding.tilPassword.isErrorEnabled = false
            }

            // auth
            if (checkFields(email, password)) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // intent
                            activity?.startActivity(Intent(activity, MainActivity::class.java))
//                            startActivity(intent)
                            activity?.finish()
                        } else {
                            Log.d("auth", "error : ${task.exception}")
                            when(task.exception) {
                                is FirebaseAuthInvalidCredentialsException -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Oops! Invalid Email!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                is FirebaseException -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Looks like you are not registered or password is incorrect! GOTO Sign Up page!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Authentication ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
            }

        }
        return view
    }

    private fun checkFields(email: String, password: String) : Boolean {
        return email.isNotBlank() && password.isNotBlank()
    }
}