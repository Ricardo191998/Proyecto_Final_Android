package com.example.revision_app.goals

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.revision_app.HomeActivity
import com.example.revision_app.R
import com.example.revision_app.application.RevisionApp
import com.example.revision_app.databinding.ActivityGoalsBinding
import com.example.revision_app.edit_goals.EditGoalsFragment
import com.example.revision_app.edit_goals.EditGoalsViewModel
import com.example.revision_app.retrofit.goal.Goal
import com.example.revision_app.retrofit.login.User
import com.example.revision_app.room.goal.GoalRepository
import com.example.revision_app.utils.NetworkUtil
import com.example.revision_app.workers.goals.GoalsWorker

class GoalsActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityGoalsBinding
    private lateinit var mGridLayout: GridLayoutManager
    private lateinit var mAdapter: GoalsAdapter

    private lateinit var goalsViewModel: GoalsViewModel
    private lateinit var mEditGoalsViewModel: EditGoalsViewModel

    private lateinit var repositoryGoalLocal: GoalRepository

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        repositoryGoalLocal = (application as RevisionApp).repositoryGoalLocal

        setupRecyclerView()
        setupAction()
    }

    private fun setupRecyclerView() {
        mAdapter = GoalsAdapter(mutableListOf()){ goal ->
            launchEditFragment(goal)
        }

        mGridLayout = GridLayoutManager(this, 2)
        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }

    override fun onResume() {
        super.onResume()

        setViewModel()
    }

    private fun setViewModel () {

        mEditGoalsViewModel = ViewModelProvider(this)[EditGoalsViewModel::class.java]
        mEditGoalsViewModel.getShowFab().observe(this) { isVisible ->
            if (isVisible) mBinding.fab.show() else mBinding.fab.hide()
        }

        mEditGoalsViewModel.getResult().observe(this){result ->
            if (result is String) {

                if(mEditGoalsViewModel.getIsOffline() == false) {
                    callGetGoals()
                } else {
                    callGetLocalGoals()
                }

            }
        }

        if(NetworkUtil.hasInternetContection(this)){
            callGetGoals()
            mEditGoalsViewModel.setIsOffline(false)
        } else {
            callGetLocalGoals()
            mEditGoalsViewModel.setIsOffline(true)
        }
    }

    fun callGetLocalGoals(){
        var userTemp = intent?.getSerializableExtra(  "EXTRA_USER") as? User

        if (userTemp != null) {
            user = userTemp
            goalsViewModel = ViewModelProvider(this)[GoalsViewModel::class.java]

            goalsViewModel.setRepository(repositoryGoalLocal)

            goalsViewModel.goals.observe(this, Observer { goals ->
                mAdapter.setGoals(goals)
            })

            goalsViewModel.loadGoalsForUserLocal(user.uid)

        }
    }

    fun callGetGoals() {
        var userTemp  = intent?.getSerializableExtra(  "EXTRA_USER") as? User
        val sharedPreferences = this.getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
        val localUpdates = sharedPreferences.getBoolean("localUpdates", false)

        if (userTemp != null) {
            user = userTemp
            val token = sharedPreferences.getString("token", "string")

            goalsViewModel = ViewModelProvider(this)[GoalsViewModel::class.java]

            goalsViewModel.goals.observe(this, Observer { goals ->
                mAdapter.setGoals(goals)
            })

            if (token != null) {
                goalsViewModel.loadGoalsForUser(user.uid, token)

                if(localUpdates) {
                    showAlert(user, token)
                } else {
                    goalWorker(user, token, false)
                }

            }
        }
    }

    private fun goalWorker(user: User, token: String, syncWithLocal: Boolean) {
        val sharedPreferences = this.getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)

        val data = workDataOf(
            "user_id" to user.uid,
            "token" to token,
            "syncWithLocal" to syncWithLocal
        )

        val workRequest1 = OneTimeWorkRequestBuilder<GoalsWorker>()
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

        workManager.beginUniqueWork("workGoals", ExistingWorkPolicy.KEEP, workRequest1)
            .enqueue()

        if(syncWithLocal) {
            workManager.getWorkInfoByIdLiveData(workRequest1.id)
                .observe(this){ work1info ->
                    if(work1info!=null){
                        if(work1info.state.isFinished){
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("localUpdates", false)
                            editor.apply()
                            this.callGetGoals()
                        }
                    }
            }
        }
    }

    fun setupAction () {
        mBinding.fab.setOnClickListener {
            launchEditFragment()
        }
    }

    private fun launchEditFragment(goal: Goal = Goal("", "", "", "","", "", "", "", "")) {
        mEditGoalsViewModel.setShowFab(false)
        mEditGoalsViewModel.setGoalSelected(goal)
        mEditGoalsViewModel.setUserId(user.uid)
        mEditGoalsViewModel.setRepository(repositoryGoalLocal)

        var goalLocal = goalsViewModel.getGoalLocalById(goal._id)

        if(goalLocal != null) {
            mEditGoalsViewModel.setLocalGoalSelected(goalLocal)
        }

        val fragmentManager: FragmentManager = supportFragmentManager

        val fragment: Fragment = EditGoalsFragment()

        val transaction: FragmentTransaction = fragmentManager.beginTransaction()

        transaction.replace(R.id.containerMain, fragment)
        transaction.addToBackStack(null)

        transaction.commit()

    }

    override fun onBackPressed() {
        val fragmentManager: FragmentManager = supportFragmentManager

        val fragment = fragmentManager.findFragmentById(R.id.containerMain)
        if (fragment is EditGoalsFragment) {
            mEditGoalsViewModel.setShowFab(true)
            fragmentManager.popBackStack()
        } else {
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("EXTRA_USER", user)
            }
            startActivity(intent)
        }
    }

    private fun showAlert(user: User, token: String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Importante!!")
        builder.setMessage("Tienes cambios en local. ¿Deseas Guardarlos o Descartalos?")

        builder.setPositiveButton("Guardar") { dialog: DialogInterface, _: Int ->
            goalWorker(user, token, true)
            dialog.dismiss()
        }

        builder.setNegativeButton("Descartar") { dialog: DialogInterface, _: Int ->
            goalWorker(user, token, false)
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

}