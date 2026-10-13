package com.ott.gluedin.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ott.gluedin.databinding.ItemCategoryRailBinding
import com.ott.gluedin.domain.entity.CategoryRail
import com.ott.gluedin.domain.entity.ContentItem

class CategoryRailAdapter(
    private val onItemClick: (ContentItem) -> Unit
) : ListAdapter<CategoryRail, CategoryRailAdapter.CategoryRailViewHolder>(RailDiffCallback) {

    // Shared RecycledViewPool across all category rails for 60fps smooth scrolling
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryRailViewHolder {
        val binding = ItemCategoryRailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryRailViewHolder(binding, sharedViewPool, onItemClick)
    }

    override fun onBindViewHolder(holder: CategoryRailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryRailViewHolder(
        private val binding: ItemCategoryRailBinding,
        private val sharedPool: RecyclerView.RecycledViewPool,
        private val onItemClick: (ContentItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val cardAdapter = ContentCardAdapter(onItemClick)

        init {
            val layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                initialPrefetchItemCount = 4
            }
            binding.rvRailContent.layoutManager = layoutManager
            binding.rvRailContent.setRecycledViewPool(sharedPool)
            binding.rvRailContent.adapter = cardAdapter
            binding.rvRailContent.setHasFixedSize(true)
        }

        fun bind(rail: CategoryRail) {
            binding.tvRailTitle.text = rail.title
            cardAdapter.submitList(rail.items)
        }
    }

    object RailDiffCallback : DiffUtil.ItemCallback<CategoryRail>() {
        override fun areItemsTheSame(oldItem: CategoryRail, newItem: CategoryRail): Boolean {
            return oldItem.railId == newItem.railId
        }

        override fun areContentsTheSame(oldItem: CategoryRail, newItem: CategoryRail): Boolean {
            return oldItem == newItem
        }
    }
}
