package com.ott.gluedin.presentation.detail.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.ott.gluedin.R
import com.ott.gluedin.databinding.FragmentDetailBinding
import com.ott.gluedin.domain.entity.ContentItem
import com.ott.gluedin.presentation.common.UiState
import com.ott.gluedin.presentation.detail.viewmodel.DetailViewModel
import com.ott.gluedin.presentation.player.PlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val contentId: String by lazy {
        arguments?.getString(ARG_CONTENT_ID).orEmpty()
    }

    private val viewModel: DetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeUiState()

        if (savedInstanceState == null) {
            viewModel.loadContentDetail(contentId)
        }
    }

    private fun setupListeners() {
        binding.ibDetailBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnDetailRetry.setOnClickListener {
            viewModel.loadContentDetail(contentId)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: UiState<ContentItem>) {
        when (state) {
            is UiState.Loading -> {
                binding.pbDetailLoading.isVisible = true
                binding.nsvDetailContent.isVisible = false
                binding.layoutDetailError.isVisible = false
            }
            is UiState.Success -> {
                binding.pbDetailLoading.isVisible = false
                binding.nsvDetailContent.isVisible = true
                binding.layoutDetailError.isVisible = false
                bindContent(state.data)
            }
            is UiState.Empty -> {
                binding.pbDetailLoading.isVisible = false
                binding.nsvDetailContent.isVisible = false
                binding.layoutDetailError.isVisible = true
                binding.tvDetailErrorMessage.text = getString(R.string.empty_content)
            }
            is UiState.Error -> {
                binding.pbDetailLoading.isVisible = false
                binding.nsvDetailContent.isVisible = false
                binding.layoutDetailError.isVisible = true
                binding.tvDetailErrorMessage.text = state.message
            }
        }
    }

    private fun bindContent(item: ContentItem) {
        binding.tvDetailTitle.text = item.title
        binding.tvDetailResolution.text = item.resolution
        binding.tvDetailRating.text = item.maturityRating
        binding.tvDetailYear.text = item.releaseYear.toString()
        binding.tvDetailDuration.text = item.duration
        binding.tvDetailStreamType.text = item.streamType
        binding.tvDetailDescription.text = item.fullDescription
        binding.tvDetailGenres.text = item.genres.joinToString(" • ")

        binding.ivDetailBackdrop.load(item.bannerUrl) {
            crossfade(true)
            placeholder(R.color.ott_surface)
            error(R.color.ott_surface)
        }

        binding.btnDetailPlay.setOnClickListener {
            val intent = PlayerActivity.createIntent(
                context = requireContext(),
                videoUrl = item.videoUrl,
                streamType = item.streamType,
                title = item.title
            )
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CONTENT_ID = "arg_content_id"

        fun newInstance(contentId: String): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT_ID, contentId)
                }
            }
        }
    }
}
