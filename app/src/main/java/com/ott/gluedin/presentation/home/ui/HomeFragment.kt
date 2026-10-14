package com.ott.gluedin.presentation.home.ui

import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.ott.gluedin.R
import com.ott.gluedin.databinding.FragmentHomeBinding
import com.ott.gluedin.domain.entity.ContentItem
import com.ott.gluedin.domain.entity.HomeFeed
import com.ott.gluedin.presentation.common.UiState
import com.ott.gluedin.presentation.home.adapter.CategoryRailAdapter
import com.ott.gluedin.presentation.home.viewmodel.HomeViewModel
import com.ott.gluedin.presentation.player.PlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    interface NavigationListener {
        fun onNavigateToDetail(contentId: String)
    }

    private var navigationListener: NavigationListener? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var railAdapter: CategoryRailAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationListener) {
            navigationListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        railAdapter = CategoryRailAdapter(viewModel.scrollStates) { item ->
            railAdapter.saveAllScrollStates(binding.rvCategoryRails)
            viewModel.verticalScrollY = binding.nsvHomeContent.scrollY
            navigationListener?.onNavigateToDetail(item.id)
        }
        binding.rvCategoryRails.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = railAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupListeners() {
        binding.btnHomeRetry.setOnClickListener {
            viewModel.loadHomeFeed()
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

    private fun renderState(state: UiState<HomeFeed>) {
        when (state) {
            is UiState.Loading -> {
                binding.pbHomeLoading.isVisible = true
                binding.nsvHomeContent.isVisible = false
                binding.layoutHomeError.isVisible = false
                binding.tvHomeEmpty.isVisible = false
            }
            is UiState.Success -> {
                binding.pbHomeLoading.isVisible = false
                binding.nsvHomeContent.isVisible = true
                binding.layoutHomeError.isVisible = false
                binding.tvHomeEmpty.isVisible = false

                bindHeroBanner(state.data.heroBanner)
                railAdapter.submitList(state.data.rails) {
                    if (viewModel.verticalScrollY > 0) {
                        _binding?.nsvHomeContent?.post {
                            _binding?.nsvHomeContent?.scrollY = viewModel.verticalScrollY
                        }
                    }
                }
            }
            is UiState.Empty -> {
                binding.pbHomeLoading.isVisible = false
                binding.nsvHomeContent.isVisible = false
                binding.layoutHomeError.isVisible = false
                binding.tvHomeEmpty.isVisible = true
            }
            is UiState.Error -> {
                binding.pbHomeLoading.isVisible = false
                binding.nsvHomeContent.isVisible = false
                binding.layoutHomeError.isVisible = true
                binding.tvHomeEmpty.isVisible = false
                binding.tvErrorMessage.text = state.message
            }
        }
    }

    private fun bindHeroBanner(hero: ContentItem) {
        binding.tvHeroTitle.text = hero.title
        binding.tvHeroDescription.text = hero.shortDescription
        binding.tvHeroResolution.text = hero.resolution
        binding.tvHeroRating.text = hero.maturityRating
        binding.tvHeroDuration.text = hero.duration
        binding.tvHeroGenres.text = hero.genres.joinToString(" • ")

        binding.ivHeroBackdrop.load(hero.bannerUrl) {
            crossfade(true)
            placeholder(R.color.ott_surface)
            error(R.color.ott_surface)
        }

        binding.btnHeroPlay.setOnClickListener {
            val intent = PlayerActivity.createIntent(
                context = requireContext(),
                videoUrl = hero.videoUrl,
                streamType = hero.streamType,
                title = hero.title
            )
            startActivity(intent)
        }

        binding.btnHeroDetails.setOnClickListener {
            navigationListener?.onNavigateToDetail(hero.id)
        }
    }

    override fun onDestroyView() {
        if (::railAdapter.isInitialized && _binding != null) {
            railAdapter.saveAllScrollStates(binding.rvCategoryRails)
            viewModel.verticalScrollY = binding.nsvHomeContent.scrollY
        }
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        navigationListener = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
