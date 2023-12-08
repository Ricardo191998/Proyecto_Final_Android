package com.example.revision_app.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.revision_app.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val fragmentManager: FragmentManager = supportFragmentManager

        val fragment: Fragment = LoginFragment()

        val transaction: FragmentTransaction = fragmentManager.beginTransaction()

        transaction.replace(R.id.fragment_login, fragment)

        transaction.commit()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_login)
        if (fragment is RegisterFragment || fragment is UserListFragment) {
            val fragmentManager: FragmentManager = supportFragmentManager

            val transaction: FragmentTransaction = fragmentManager.beginTransaction()

            transaction.replace(R.id.fragment_login, LoginFragment())

            transaction.commit()
        } else {

        }
    }


}