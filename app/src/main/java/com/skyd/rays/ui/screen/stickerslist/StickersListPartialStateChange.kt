package com.skyd.rays.ui.screen.stickerslist

import com.skyd.rays.model.bean.StickerWithTags

internal sealed interface StickersListPartialStateChange {
    fun reduce(oldState: StickersListState): StickersListState

    data object LoadingDialog : StickersListPartialStateChange {
        override fun reduce(oldState: StickersListState): StickersListState =
            oldState.copy(loadingDialog = true)
    }

    sealed interface StickersList : StickersListPartialStateChange {
        override fun reduce(oldState: StickersListState): StickersListState {
            return when (this) {
                is Success -> oldState.copy(
                    listState = ListState.Success(stickerWithTagsList),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    listState = oldState.listState.apply { loading = true }
                )
            }
        }

        data object Loading : StickersList
        data class Success(val stickerWithTagsList: List<StickerWithTags>) : StickersList
    }

    sealed interface AddClickCount : StickersListPartialStateChange {
        data object Success : AddClickCount {
            override fun reduce(oldState: StickersListState): StickersListState = oldState
        }
    }
}
