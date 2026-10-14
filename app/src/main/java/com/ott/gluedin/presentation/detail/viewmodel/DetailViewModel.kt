package com.ott.gluedin.presentation.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ott.gluedin.domain.entity.ContentItem
import com.ott.gluedin.domain.usecase.GetContentDetailUseCase
import com.ott.gluedin.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getContentDetailUseCase: GetContentDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ContentItem>>(UiState.Loading)
    val uiState: StateFlow<UiState<ContentItem>> = _uiState.asStateFlow()

    fun loadContentDetail(contentId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getContentDetailUseCase(contentId)
                .onSuccess { item ->
                    _uiState.value = UiState.Success(item)
                }
                .onFailure { throwable ->
                    _uiState.value = UiState.Error(
                        message = throwable.localizedMessage ?: "Failed to load content details"
                    )
                }
        }
    }
}
