package com.shiva

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.shiva.fragments.FragmentHome
import com.shiva.fragments.FragmentPost
import com.shiva.fragments.FragmentProfile
import com.shiva.fragments.FragmentSearch
import com.shiva.socialmediaapp.R
import com.shiva.socialmediaapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        // bottom nav switching frags
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.botNavHome -> setFragContainer(FragmentHome())
                R.id.botNavSearch -> setFragContainer(FragmentSearch())
                R.id.botNavPost -> setFragContainer(FragmentPost())
                R.id.botNavProfile -> setFragContainer(FragmentProfile())
                else -> setFragContainer(FragmentHome())
            }
            true
        }

        // chat intent
        binding.appToolbar.ivMessageIcon.setOnClickListener {
            Intent(this, ChatActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.appToolbar.appIcon.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Create the dialog
                val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
                alertDialogBuilder.setTitle("Logout")
                alertDialogBuilder.setMessage("Do you want really want to Logout?")
                alertDialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
                    // Code to execute when the "OK" button is clicked
                    dialog.dismiss()
                    auth.signOut()
                    startActivity(Intent(this@MainActivity, AuthenticationActivity::class.java))
                    finish()
                })
                alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                    // Code to execute when the "Cancel" button is clicked
                    dialog.dismiss()
                })

                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        })
    }

    private fun setFragContainer(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.container.id, fragment)
            .commit()
    }
}