package com.example.revision_app.tasks

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.revision_app.application.RevisionApp
import com.example.revision_app.databinding.ActivityTasksBinding
import com.example.revision_app.retrofit.login.User
import com.example.revision_app.retrofit.revision.Goal
import com.example.revision_app.retrofit.task.TaskDto
import com.example.revision_app.retrofit.task.TaskRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface OnItemClickListener {
    fun onItemLongClick(item: TaskDto, view: View)
}

class TasksActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityTasksBinding
    private lateinit var repository: TaskRepository

    private lateinit var mAdapterPending: TaskAdapter
    private lateinit var mAdapterInProgress: TaskAdapter
    private lateinit var mAdapterCompleted: TaskAdapter

    private lateinit var taskViewModel: TaskViewModel

    private var goals: List<Goal>? = null

    var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        repository = (application as RevisionApp).repositoryTask

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        taskViewModel.setRepository(repository)

        setUpRecyclerView()
        setUpService()
        setUpGoalSlider()

        mBinding.buttonWithIcon.setOnClickListener {
            setUpOpenDialog()
        }
    }

    fun setUpOpenDialog(){
        val dialog = TaskDialog(goals!!){
            if(it){
                message("Tarea creada Exitosamente")
            }else {
                message("Fallo la creación de la tarea")
            }
            setUpService()
        }
        dialog.show(supportFragmentManager, "dialog")
    }

    fun setUpGoalSlider(){
        var user = intent?.getSerializableExtra("EXTRA_USER") as User

        if (user != null) {
            taskViewModel.loadGoals(user, token)
            taskViewModel.goals.observe(this, Observer { goals ->
                Log.d("GOALS", goals.toString())
               this.goals = goals
            })
        }

    }

    fun setUpRecyclerView(){
        mAdapterPending = TaskAdapter(mutableListOf(), object : OnItemClickListener {
            override fun onItemLongClick(item: TaskDto, view: View) {
                // Iniciar el arrastre y soltar (drag and drop)
                val dragShadow = View.DragShadowBuilder(view)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(null, dragShadow, item, 0)
                } else {
                    view.startDrag(null, dragShadow, item, 0)
                }
            }
        })
        mAdapterInProgress = TaskAdapter(mutableListOf(), object : OnItemClickListener {
            override fun onItemLongClick(item: TaskDto, view: View) {
                // Iniciar el arrastre y soltar (drag and drop)
                val dragShadow = View.DragShadowBuilder(view)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(null, dragShadow, item, 0)
                } else {
                    view.startDrag(null, dragShadow, item, 0)
                }
            }
        })
        mAdapterCompleted = TaskAdapter(mutableListOf(), object : OnItemClickListener {
            override fun onItemLongClick(item: TaskDto, view: View) {
                // Iniciar el arrastre y soltar (drag and drop)
                val dragShadow = View.DragShadowBuilder(view)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(null, dragShadow, item, 0)
                } else {
                    view.startDrag(null, dragShadow, item, 0)
                }
            }
        })

        mBinding.rvPending.layoutManager = LinearLayoutManager(this)
        mBinding.rvPending.adapter = mAdapterPending

        mBinding.rvInprogress.layoutManager = LinearLayoutManager(this)
        mBinding.rvInprogress.adapter = mAdapterInProgress

        mBinding.rvCompleted.layoutManager = LinearLayoutManager(this)
        mBinding.rvCompleted.adapter = mAdapterCompleted

        mBinding.svPendind.setOnDragListener(dragListener)
        mBinding.svInprogress.setOnDragListener(dragListener)
        mBinding.svCompleted.setOnDragListener(dragListener)

    }

    fun findRecyclerViewInScrollView(scrollView: NestedScrollView): RecyclerView? {
        for (i in 0 until scrollView.childCount) {
            val childView = scrollView.getChildAt(i)
            if (childView is RecyclerView) {
                return childView
            }
        }
        return null
    }

    val dragListener = View.OnDragListener { v, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                // Inicia el arrastre
                true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                // El elemento arrastrable ha ingresado al ScrollView
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                // El elemento arrastrable ha salido del ScrollView
                true
            }
            DragEvent.ACTION_DROP -> {
                // El elemento arrastrable se ha soltado en el ScrollView
                val taskValue = event.localState

                val task = taskValue as TaskDto
                val targetRecyclerView = findRecyclerViewInScrollView(v as NestedScrollView)

                val hashMap = HashMap<String, RecyclerView>()

                // Agregar elementos al HashMap
                hashMap["pending"] = mBinding.rvPending
                hashMap["in-progress"] = mBinding.rvInprogress
                hashMap["finished"] = mBinding.rvCompleted

                val fromRecyclerView = hashMap[task.status]

                if (targetRecyclerView != null) {

                    CoroutineScope(Dispatchers.IO).launch{

                        task.status = v.tag.toString()
                        val result = taskViewModel.updateTask(task, token)

                        withContext(Dispatchers.Main){
                            if(result){
                                val adapter = targetRecyclerView.adapter as TaskAdapter
                                adapter.addItem(task)
                                message("Meta actualizada con éxito")
                            } else {
                                message("Fallo la actualización de la meta")
                            }
                        }

                    }


                }

                if(fromRecyclerView != null){
                    val adapter = fromRecyclerView.adapter as TaskAdapter
                    val indexToRemove = adapter.getItemAt(task)
                    if(indexToRemove != -1){
                        adapter.removeItem(indexToRemove)
                    }
                }

                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                // Fin del arrastre
                true
            }
            else -> false
        }
    }

    private fun message(text: String){
        Snackbar.make(mBinding.mainLayout, text, Snackbar.LENGTH_SHORT)
            .setTextColor(Color.parseColor("#FFFFFF"))
            .setBackgroundTint(Color.parseColor("#9E1734"))
            .show()
    }

    fun setUpService(){
        val sharedPreferences = this.getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", "string")!!



        taskViewModel.tasks.observe(this, Observer { tasks ->
            var pendings = mutableListOf<TaskDto>()
            var inProgress = mutableListOf<TaskDto>()
            var completed = mutableListOf<TaskDto>()
            tasks.forEach { task ->
                if(task.status == "pending"){
                    pendings.add(task)
                }else if(task.status == "in-progress"){
                    inProgress.add(task)
                } else {
                    completed.add(task)
                }
            }
            mAdapterPending.setTasks(pendings)
            mAdapterInProgress.setTasks(inProgress)
            mAdapterCompleted.setTasks(completed)
        })

        if (token != null) {
            taskViewModel.loadTask( token)
        }

    }
}

