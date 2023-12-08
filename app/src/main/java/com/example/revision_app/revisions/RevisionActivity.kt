package com.example.revision_app.revisions

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.revision_app.HomeActivity
import com.example.revision_app.R
import com.example.revision_app.application.RevisionApp
import com.example.revision_app.databinding.ActivityRevisionBinding
import com.example.revision_app.edit_goals.EditGoalsFragment
import com.example.revision_app.edit_revisions.EditRevisionsFragment
import com.example.revision_app.edit_revisions.EditRevisionsViewModel
import com.example.revision_app.retrofit.login.User
import com.example.revision_app.retrofit.revision.RevisionDto
import com.example.revision_app.retrofit.revision.RevisionRepository
import com.example.revision_app.room.goal.GoalRepository
import com.example.revision_app.room.revision.RevisionRespositoryLocal
import com.example.revision_app.utils.NetworkUtil
import com.example.revision_app.workers.revisions.RevisionsWorker

class RevisionActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityRevisionBinding
    private lateinit var revisionViewModel : RevisionViewModel

    private lateinit var mEditRevisionsViewModel: EditRevisionsViewModel
    private lateinit var mLinearLayout: LinearLayoutManager

    private lateinit var mAdapter: RevisionAdapter

    private lateinit var repository: RevisionRepository
    private lateinit var repositoryLocal: RevisionRespositoryLocal
    private lateinit var repositoryGoalLocal: GoalRepository


    private var currentSelected : String = "Actual"

    private lateinit var user : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityRevisionBinding.inflate(layoutInflater)

        repository = (application as RevisionApp).repository
        repositoryLocal = (application as RevisionApp).repositoryRevisionLocal
        repositoryGoalLocal = (application as RevisionApp).repositoryGoalLocal

        setContentView(mBinding.root)

        mBinding.fabRevision.setOnClickListener {
            launchEditFragment()
        }

        setupSpinner()
        setupRecyclerView()
        setViewModel()
    }

    private fun setupRecyclerView() {
        mAdapter = RevisionAdapter(mutableListOf()){ revision ->
            launchEditFragment(revision)
        }

        mLinearLayout = LinearLayoutManager(this)
        mBinding.rvRevisions.apply {
            setHasFixedSize(true)
            layoutManager = mLinearLayout
            adapter = mAdapter
        }
    }

    private fun setViewModel () {

        mEditRevisionsViewModel = ViewModelProvider(this)[EditRevisionsViewModel::class.java]
        mEditRevisionsViewModel.setRepository(repository)
        mEditRevisionsViewModel.setRepositoryLocal(repositoryLocal)
        mEditRevisionsViewModel.setRepositoryGoalLocal(repositoryGoalLocal)

        mEditRevisionsViewModel.getShowFab().observe(this) { isVisible ->
            if (isVisible) {
                mBinding.fabRevision.show()
                mBinding.rvRevisions.visibility = View.VISIBLE
                mBinding.rvSpinner.visibility = View.VISIBLE
            } else {
                mBinding.fabRevision.hide()
                mBinding.rvRevisions.visibility = View.GONE
                mBinding.rvSpinner.visibility = View.GONE
            }
        }

        mEditRevisionsViewModel.getResult().observe(this){result ->
            if (result is String) {
                if(NetworkUtil.hasInternetContection(this)){
                    callGetRevision()
                } else {
                    callGetLocalRevision()
                }
            }
        }

        if(NetworkUtil.hasInternetContection(this)){
            mEditRevisionsViewModel.setIsOffline(false)
        } else {
            mEditRevisionsViewModel.setIsOffline(true)
        }

    }

    fun callGetRevision() {
        user = intent?.getSerializableExtra("EXTRA_USER") as User
        val sharedPreferences = this.getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
        val localUpdates = sharedPreferences.getBoolean("localUpdatesRevision", false)

        if (user != null) {
            val token = sharedPreferences.getString("token", "string")

            revisionViewModel = ViewModelProvider(this)[RevisionViewModel::class.java]

            mBinding.etRevisionSearch.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
                override fun afterTextChanged(s: Editable?) {
                    revisionViewModel.searchRevision(mBinding.etRevisionSearch.text.toString())
                }
            })

            revisionViewModel.setRepository(repository)
            revisionViewModel.setRepositoryLocal(repositoryLocal)

            revisionViewModel.revision.observe(this, Observer { revisions ->
                mAdapter.setRevision(revisions)
            })

            if (token != null) {
                revisionViewModel.loadRevision(currentSelected == "Actual", user.uid , token)

                if(localUpdates){
                    showAlert(user, token)
                } else {
                    revisionWorker(user, token, false)
                }

            }
        }
    }

    fun callGetLocalRevision() {
        user = intent?.getSerializableExtra("EXTRA_USER") as User

        if (user != null) {

            revisionViewModel = ViewModelProvider(this)[RevisionViewModel::class.java]
            revisionViewModel.setRepositoryGoalLocal(repositoryGoalLocal)

            mBinding.etRevisionSearch.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
                override fun afterTextChanged(s: Editable?) {
                    revisionViewModel.searchRevision(mBinding.etRevisionSearch.text.toString())
                }
            })

            revisionViewModel.setRepository(repository)
            revisionViewModel.setRepositoryLocal(repositoryLocal)

            revisionViewModel.revision.observe(this, Observer { revisions ->
                mAdapter.setRevision(revisions)
            })

            revisionViewModel.goals.observe(this, Observer { revisions ->
                revisionViewModel.loadRevisionsForUserLocal(currentSelected == "Actual", user.uid)
            })

            revisionViewModel.loadGoalsForUserLocal(user.uid)
        }
    }

    private fun setupSpinner() {
        val items = arrayOf("Actual", "Todos")
        val context: Context = applicationContext

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.rvSpinner.adapter = adapter

        mBinding.rvSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                currentSelected = selectedItem

                if(NetworkUtil.hasInternetContection(context)){
                    callGetRevision()
                } else {
                    callGetLocalRevision()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })
    }

    private fun launchEditFragment(revision: RevisionDto = RevisionDto("", "", "", 0,0, false, "", "", null)) {
        mEditRevisionsViewModel.setShowFab(false)
        mEditRevisionsViewModel.setRevisionSelected(revision)
        mEditRevisionsViewModel.setUser(user)

        var revisionLocal = revisionViewModel.getRevisionLocalById(revision._id!!)

        if(revisionLocal != null) {
            mEditRevisionsViewModel.setLocalRevisionSelected(revisionLocal)
        }

        val fragmentManager: FragmentManager = supportFragmentManager

        val fragment: Fragment = EditRevisionsFragment()

        val transaction: FragmentTransaction = fragmentManager.beginTransaction()

        transaction.replace(R.id.container_revision, fragment)
        transaction.addToBackStack(null)

        transaction.commit()
    }

    private fun revisionWorker(user: User, token: String, syncWithLocal: Boolean) {
        val sharedPreferences = this.getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)

        val data = workDataOf(
            "user_id" to user.uid,
            "token" to token,
            "syncWithLocal" to syncWithLocal
        )

        val workRequest1 = OneTimeWorkRequestBuilder<RevisionsWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(
                        true
                    )
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(data)
            .build()

        val workManager = WorkManager.getInstance(this)

        workManager.beginUniqueWork("workRevisions", ExistingWorkPolicy.KEEP, workRequest1)
            .enqueue()

        if(syncWithLocal) {
            workManager.getWorkInfoByIdLiveData(workRequest1.id)
                .observe(this){ work1info ->
                    if(work1info!=null){
                        if(work1info.state.isFinished){
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("localUpdatesRevision", false)
                            editor.apply()
                            this.callGetRevision()
                        }
                    }
                }
        }
    }

    private fun showAlert(user: User, token: String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Importante!!")
        builder.setMessage("Tienes cambios en local. ¿Deseas Guardarlos o Descartalos?")

        builder.setPositiveButton("Guardar") { dialog: DialogInterface, _: Int ->
            revisionWorker(user, token, true)
            dialog.dismiss()
        }

        builder.setNegativeButton("Descartar") { dialog: DialogInterface, _: Int ->
            revisionWorker(user, token, false)
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    override fun onBackPressed() {
        val fragmentManager: FragmentManager = supportFragmentManager

        val fragment = fragmentManager.findFragmentById(R.id.container_revision)
        if (fragment is EditRevisionsFragment) {
            mEditRevisionsViewModel.setShowFab(true)
            fragmentManager.popBackStack()
        } else {
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("EXTRA_USER", user)
            }
            startActivity(intent)
        }
    }


}