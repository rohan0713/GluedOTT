package com.ott.gluedin.presentation.home.viewmodel

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ott.gluedin.domain.entity.HomeFeed
import com.ott.gluedin.domain.usecase.GetHomeFeedUseCase
import com.ott.gluedin.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeFeedUseCase: GetHomeFeedUseCase
) : ViewModel() {

    val scrollStates: MutableMap<String, Parcelable> = mutableMapOf()
    var verticalScrollY: Int = 0

    private val _uiState = MutableStateFlow<UiState<HomeFeed>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeFeed>> = _uiState.asStateFlow()

    init {
        loadHomeFeed()
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getHomeFeedUseCase()
                .onSuccess { feed ->
                    if (feed.rails.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(feed)
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = UiState.Error(
                        message = throwable.localizedMessage ?: "Failed to load OTT content"
                    )
                }
        }
    }
}