package com.example.revision_app.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.revision_app.HomeActivity
import com.example.revision_app.application.RevisionApp
import com.example.revision_app.databinding.FragmentUserListBinding
import com.example.revision_app.retrofit.login.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: UserItemAdapter
    private lateinit var mLinearLayout: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        getUserList()
        return root
    }

    private fun setupRecyclerView() {
        mAdapter = UserItemAdapter(mutableListOf()){ user ->
            launchHome(User(user.uid, user.email, user.name, user.user_name, user.image_url))
        }

        mLinearLayout = LinearLayoutManager(requireActivity())
        binding.rvUsers.apply {
            setHasFixedSize(true)
            layoutManager = mLinearLayout
            adapter = mAdapter
        }
    }

    private fun getUserList() {
        var repository = (RevisionApp.appContext  as RevisionApp).repositoryUserLocal

        CoroutineScope(Dispatchers.IO).launch {
            var localUsers = repository.getUsers()
            mAdapter.setUser(localUsers)
        }
    }


    private fun launchHome(user: User) {
        val intent = Intent(context, HomeActivity::class.java).apply {
            putExtra("EXTRA_USER", user)
        }
        startActivity(intent)
    }

}