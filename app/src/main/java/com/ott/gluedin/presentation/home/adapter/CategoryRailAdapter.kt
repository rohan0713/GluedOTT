package com.ott.gluedin.presentation.home.adapter

import android.os.Parcelable
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
    private val scrollStates: MutableMap<String, Parcelable> = mutableMapOf(),
    private val onItemClick: (ContentItem) -> Unit
) : ListAdapter<CategoryRail, CategoryRailAdapter.CategoryRailViewHolder>(RailDiffCallback) {

    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryRailViewHolder {
        val binding = ItemCategoryRailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryRailViewHolder(binding, sharedViewPool, scrollStates, onItemClick)
    }

    override fun onBindViewHolder(holder: CategoryRailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: CategoryRailViewHolder) {
        holder.saveScrollState()
        super.onViewRecycled(holder)
    }

    fun saveAllScrollStates(parentRecyclerView: RecyclerView) {
        for (i in 0 until parentRecyclerView.childCount) {
            val child = parentRecyclerView.getChildAt(i)
            val holder = parentRecyclerView.getChildViewHolder(child) as? CategoryRailViewHolder
            holder?.saveScrollState()
        }
    }

    class CategoryRailViewHolder(
        private val binding: ItemCategoryRailBinding,
        private val sharedPool: RecyclerView.RecycledViewPool,
        private val scrollStates: MutableMap<String, Parcelable>,
        private val onItemClick: (ContentItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val cardAdapter = ContentCardAdapter(onItemClick)
        private var currentRailId: String? = null

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
            saveScrollState()
            currentRailId = rail.railId
            binding.tvRailTitle.text = rail.title
            cardAdapter.submitList(rail.items) {
                val savedState = scrollStates[rail.railId]
                if (savedState != null) {
                    binding.rvRailContent.layoutManager?.onRestoreInstanceState(savedState)
                } else {
                    binding.rvRailContent.scrollToPosition(0)
                }
            }
        }

        fun saveScrollState() {
            currentRailId?.let { id ->
                binding.rvRailContent.layoutManager?.onSaveInstanceState()?.let { state ->
                    scrollStates[id] = state
                }
            }
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
