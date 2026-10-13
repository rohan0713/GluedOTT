package com.ott.gluedin.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ott.gluedin.R
import com.ott.gluedin.databinding.ItemContentCardBinding
import com.ott.gluedin.domain.entity.ContentItem

class ContentCardAdapter(
    private val onItemClick: (ContentItem) -> Unit
) : ListAdapter<ContentItem, ContentCardAdapter.ContentCardViewHolder>(ContentDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentCardViewHolder {
        val binding = ItemContentCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContentCardViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ContentCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ContentCardViewHolder(
        private val binding: ItemContentCardBinding,
        private val onItemClick: (ContentItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContentItem) {
            binding.tvTitle.text = item.title
            val subtitle = buildString {
                if (item.duration.isNotBlank()) append(item.duration)
                if (item.genres.isNotEmpty()) {
                    if (isNotEmpty()) append(" • ")
                    append(item.genres.first())
                }
            }
            binding.tvSubtitle.text = subtitle
            binding.tvResolutionBadge.text = item.resolution

            binding.ivThumbnail.load(item.thumbnailUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_rounded_card)
                error(R.drawable.bg_rounded_card)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    object ContentDiffCallback : DiffUtil.ItemCallback<ContentItem>() {
        override fun areItemsTheSame(oldItem: ContentItem, newItem: ContentItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ContentItem, newItem: ContentItem): Boolean {
            return oldItem == newItem
        }
    }
}
