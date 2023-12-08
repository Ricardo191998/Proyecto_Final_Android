package com.example.revision_app.revisions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.revision_app.R
import com.example.revision_app.retrofit.revision.RevisionDto
import com.squareup.picasso.Picasso

class RevisionAdapter(private var revisions: MutableList<RevisionDto>, private val onRevisionClicked: (RevisionDto) -> Unit) :
    RecyclerView.Adapter<RevisionAdapter.RevisionViewHolder>() {
    private lateinit var mContext: Context
    var onItemSelected : ((RevisionDto) -> Unit)? = null

    fun setRevision(revisions: List<RevisionDto>) {
        this.revisions = revisions as MutableList<RevisionDto>
        notifyDataSetChanged()
    }

    class RevisionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val revisionImage: ImageView = itemView.findViewById(R.id.imv_goal_revision)
        val revisionName: TextView = itemView.findViewById(R.id.tv_revision_title)
        val revisionDesc: TextView = itemView.findViewById(R.id.tv_revision_desc)
        val revisionStatus: TextView = itemView.findViewById(R.id.tv_revision_status)
        val root: ConstraintLayout = itemView.findViewById(R.id.ly_revision)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RevisionViewHolder {
        mContext = parent.context
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_revision_view, parent, false)
        return RevisionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RevisionViewHolder, position: Int) {
        val currentRevision = revisions[position]
        // holder.pokemonImage.setImageResource()
        holder.revisionName.text = currentRevision.goal_id?.name
        holder.revisionDesc.text = currentRevision.goal_id?.description
        holder.revisionStatus.text = "${currentRevision.real_progress} - ${currentRevision.kpi}"
        Picasso.get()
            .load(currentRevision.goal_id?.image_url)
            .error(R.drawable.ic_people)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.revisionImage)
        holder.root.setOnClickListener {
            onRevisionClicked(currentRevision)
        }
    }

    override fun getItemCount(): Int {
        return revisions.size
    }
}