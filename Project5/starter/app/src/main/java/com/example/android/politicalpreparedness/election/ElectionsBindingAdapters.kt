package com.example.android.politicalpreparedness.election

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.model.ElectionModel

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<ElectionModel>?) {
    val adapter = recyclerView.adapter as ElectionListAdapter
    adapter.submitList(data)
}