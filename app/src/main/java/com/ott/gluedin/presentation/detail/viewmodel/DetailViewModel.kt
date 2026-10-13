package com.ott.gluedin.presentation.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ott.gluedin.domain.entity.ContentItem
import com.ott.gluedin.domain.usecase.GetContentDetailUseCase
import com.ott.gluedin.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
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

    class Factory(
        private val getContentDetailUseCase: GetContentDetailUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                return DetailViewModel(getContentDetailUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
