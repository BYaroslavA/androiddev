package com.example.myapplication123

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication123.databinding.ItemFilterBinding

class FilterAdapter(
    private val filters: List<FilterType>,
    private var selectedFilter: FilterType,
    private val onFilterSelected: (FilterType) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private var thumbnailBitmap: Bitmap? = null

    fun setThumbnail(bitmap: Bitmap?) {
        this.thumbnailBitmap = bitmap
        notifyDataSetChanged()
    }

    fun setSelected(filter: FilterType) {
        val oldPos = filters.indexOf(selectedFilter)
        val newPos = filters.indexOf(filter)
        selectedFilter = filter
        if (oldPos != -1) notifyItemChanged(oldPos)
        if (newPos != -1) notifyItemChanged(newPos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = filters[position]
        holder.bind(filter, filter == selectedFilter)
    }

    override fun getItemCount(): Int = filters.size

    inner class FilterViewHolder(private val binding: ItemFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(filter: FilterType, isSelected: Boolean) {
            binding.tvFilterName.text = filter.displayName

            // Set thumbnail with filter effect
            thumbnailBitmap?.let { bmp ->
                binding.ivFilterThumbnail.setImageBitmap(bmp)
                val matrix = ImageProcessor.getFilterColorMatrix(filter)
                binding.ivFilterThumbnail.colorFilter = ColorMatrixColorFilter(matrix)
            } ?: run {
                binding.ivFilterThumbnail.setImageDrawable(null)
            }

            // Selection state styling
            if (isSelected) {
                binding.cardFilter.setBackgroundResource(R.drawable.bg_filter_thumb_selected)
                binding.tvFilterName.setTextColor(Color.parseColor("#00E5FF"))
            } else {
                binding.cardFilter.setBackgroundResource(R.drawable.bg_filter_thumb_normal)
                binding.tvFilterName.setTextColor(Color.parseColor("#E2E8F0"))
            }

            binding.root.setOnClickListener {
                if (filter != selectedFilter) {
                    setSelected(filter)
                    onFilterSelected(filter)
                }
            }
        }
    }
}
