package com.example.revision_app.login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.revision_app.R
import com.example.revision_app.room.user.data.model.UserEntity
import com.squareup.picasso.Picasso

class UserItemAdapter(private var users: MutableList<UserEntity>, private val onUserCliked: (UserEntity) -> Unit) :
    RecyclerView.Adapter<UserItemAdapter.UserItemViewHolder>(){

    private lateinit var mContext: Context

    fun setUser(users: List<UserEntity>) {
        this.users = users as MutableList<UserEntity>
        notifyDataSetChanged()
    }

    class UserItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.imv_goal_revision)
        val userName: TextView = itemView.findViewById(R.id.tv_user_name)
        val userEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val root: ConstraintLayout = itemView.findViewById(R.id.ly_users)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        mContext = parent.context
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return UserItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val currentUser = users[position]
        // holder.pokemonImage.setImageResource()
        holder.userName.text = currentUser.name
        holder.userEmail.text = currentUser.email
        Picasso.get()
            .load(currentUser.image_url)
            .error(R.drawable.ic_people)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.userImage)
        holder.root.setOnClickListener {
            onUserCliked(currentUser)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

}