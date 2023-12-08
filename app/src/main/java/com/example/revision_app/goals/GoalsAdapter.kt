package com.example.revision_app.goals

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.revision_app.retrofit.goal.Goal
import com.example.revision_app.R
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

class GoalsAdapter(private var goals: MutableList<Goal>, private val onGoalClicked: (Goal) -> Unit) :
    RecyclerView.Adapter<GoalsAdapter.GoalsViewHolder>() {
    private lateinit var mContext: Context
    var onItemSelected : ((Goal) -> Unit)? = null

    fun setGoals(goals: List<Goal>) {
        this.goals = goals as MutableList<Goal>
        notifyDataSetChanged()
    }

    class GoalsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val goalImage: ImageView = itemView.findViewById(R.id.imgPhoto)
        val goalName: TextView = itemView.findViewById(R.id.tvName)
        val root: MaterialCardView = itemView.findViewById(R.id.rootGoal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalsViewHolder {
        mContext = parent.context
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.goal_item, parent, false)
        return GoalsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GoalsViewHolder, position: Int) {
        val currentGoal = goals[position]
        // holder.pokemonImage.setImageResource()
        holder.goalName.text = currentGoal.name
        Picasso.get()
            .load(currentGoal.image_url)
            .error(R.drawable.ic_people)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.goalImage)
        holder.root.setOnClickListener {
            onGoalClicked(currentGoal)
        }
    }

    override fun getItemCount(): Int {
        return goals.size
    }
}