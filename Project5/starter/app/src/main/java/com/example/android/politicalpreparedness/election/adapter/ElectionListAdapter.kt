package com.example.android.politicalpreparedness.election.adapter

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.databinding.ViewholderElectionBinding
import com.example.android.politicalpreparedness.election.model.ElectionModel
import com.example.android.politicalpreparedness.network.models.ElectionEntity
import java.util.*

class ElectionListAdapter(private val clickListener: ElectionListener)
    : ListAdapter<ElectionModel, ElectionViewHolder>(ElectionDiffCallback()) {

    // TODO: Bind ViewHolder
    override fun onBindViewHolder(holder: ElectionViewHolder, position: Int) {
        val electionModel = getItem(position) as ElectionModel
        holder.bind(electionModel, clickListener)
    }

    // TODO: Add companion object to inflate ViewHolder (from)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionViewHolder {
        return ElectionViewHolder.from(parent)
    }
}

// TODO: Create ElectionViewHolder
class ElectionViewHolder private constructor(val binding: ViewholderElectionBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(
        electionModel: ElectionModel,
        clickListener: ElectionListener
    ) {
        binding.election = electionModel
        val electionDate: String = ( SimpleDateFormat("E MMM dd hh:mm:ss z yyyy", Locale.US)).format(electionModel.electionDay.time)
        binding.time = electionDate
        binding.clickListener = clickListener
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): ElectionViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ViewholderElectionBinding
                .inflate(layoutInflater, parent, false)
            return ElectionViewHolder(binding)
        }
    }
}

// TODO: Create ElectionDiffCallback
class ElectionDiffCallback :
    DiffUtil.ItemCallback<ElectionModel>() {
    override fun areItemsTheSame(oldItem: ElectionModel, newItem: ElectionModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ElectionModel, newItem: ElectionModel): Boolean {
        return oldItem == newItem
    }
}

// TODO: Create ElectionListener
class ElectionListener(val clickListener: (electionModel: ElectionModel) -> Unit) {
    fun onClick(electionModel: ElectionModel) = clickListener(electionModel)
}