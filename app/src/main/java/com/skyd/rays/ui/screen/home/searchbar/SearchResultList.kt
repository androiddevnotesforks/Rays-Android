package com.skyd.rays.ui.screen.home.searchbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skyd.rays.R
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.plus
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RaysFloatingActionButton
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysOutlinedCard
import com.skyd.rays.ui.local.LocalSearchResultReverse
import com.skyd.rays.ui.local.LocalSearchResultSort
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.home.HomeIntent
import com.skyd.rays.ui.screen.home.HomeViewModel
import com.skyd.rays.util.sendStickerByUuid
import com.skyd.rays.util.sendStickersByUuids
import kotlinx.coroutines.launch

@Composable
fun SearchResultList(
    state: LazyStaggeredGridState,
    dataList: List<StickerWithTags>,
    onItemClickListener: ((data: StickerWithTags, selected: Boolean) -> Unit)? = null,
    multiSelect: Boolean,
    onMultiSelectChanged: (Boolean) -> Unit,
    selectedStickers: List<StickerWithTags>,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val searchResultSort = LocalSearchResultSort.current
    val searchResultReverse = LocalSearchResultReverse.current
    val scope = rememberCoroutineScope()
    var fabHeight by remember { mutableStateOf(0.dp) }

    LaunchedEffect(searchResultSort) {
        viewModel.sendUiIntent(HomeIntent.SortStickerWithTagsList(dataList))
    }
    LaunchedEffect(searchResultReverse) {
        viewModel.sendUiIntent(HomeIntent.ReverseStickerWithTagsList(dataList))
    }

    Column {
        SearchResultConfigBar(
            stickersCount = dataList.size,
            multiSelect = multiSelect,
            onMultiSelectChanged = onMultiSelectChanged,
        )
        if (dataList.isEmpty()) {
            AnimatedPlaceholder(
                resId = R.raw.lottie_genshin_impact_klee_2,
                tip = stringResource(id = R.string.home_screen_no_search_result_tip)
            )
        } else {
            Scaffold(
                floatingActionButton = {
                    RaysFloatingActionButton(
                        onClick = { scope.launch { state.animateScrollToItem(0) } },
                        onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                        contentDescription = stringResource(R.string.home_screen_search_result_list_to_top),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null
                        )
                    }
                },
                contentWindowInsets = WindowInsets(0.dp),
            ) { paddingValues ->
                val windowSizeClass = LocalWindowSizeClass.current
                LazyVerticalStaggeredGrid(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    contentPadding = paddingValues + PaddingValues(bottom = fabHeight) +
                            PaddingValues(horizontal = 16.dp) +
                            PaddingValues(bottom = 16.dp),
                    columns = StaggeredGridCells.Fixed(
                        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) 2 else 4
                    ),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = dataList, key = { it.sticker.uuid }) {
                        SearchResultItem(
                            data = it,
                            selectable = multiSelect,
                            selected = selectedStickers.contains(it),
                            onClickListener = onItemClickListener
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultConfigBar(
    stickersCount: Int,
    multiSelect: Boolean,
    onMultiSelectChanged: (Boolean) -> Unit
) {
    val searchResultReverse = LocalSearchResultReverse.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expandMenu by rememberSaveable { mutableStateOf(false) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            Badge {
                AnimatedContent(
                    targetState = stickersCount,
                    label = "badgeAnimatedContent",
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                slideInVertically(initialOffsetY = { it }))
                            .togetherWith(
                                fadeOut(animationSpec = tween(90)) +
                                        slideOutVertically(targetOffsetY = { -it })
                            )
                    }
                ) { targetCount ->
                    Text(text = targetCount.toString())
                }
            }
        }

        item {
            Box {
                FilterChip(
                    selected = false,
                    onClick = { expandMenu = !expandMenu },
                    label = { Text(text = stringResource(R.string.search_result_sort)) },
                    trailingIcon = {
                        Icon(
                            imageVector = if (expandMenu) Icons.Default.ArrowDropUp
                            else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                        )
                    }
                )
                SearchResultSortMenu(
                    expanded = expandMenu,
                    onDismissRequest = { expandMenu = false })
            }
        }

        item {
            FilterChip(
                selected = searchResultReverse,
                onClick = {
                    SearchResultReversePreference.put(
                        context = context,
                        scope = scope,
                        value = !searchResultReverse
                    )
                },
                label = { Text(text = stringResource(R.string.search_result_reverse)) },
                leadingIcon = {
                    if (searchResultReverse) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                }
            )
        }

        item {
            FilterChip(
                selected = multiSelect,
                onClick = { onMultiSelectChanged(!multiSelect) },
                label = { Text(text = stringResource(R.string.search_result_multi_select)) },
                leadingIcon = {
                    if (multiSelect) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                }
            )
        }
    }
}

@Composable
internal fun MultiSelectActionBar(
    selectedStickers: List<StickerWithTags>,
    onDeleteClick: () -> Unit,
    onExportClick: () -> Unit,
) {
    val context = LocalContext.current
    val windowSizeClass = LocalWindowSizeClass.current
    val items = remember {
        arrayOf<@Composable () -> Unit>(
            @Composable {
                RaysIconButton(
                    onClick = {
                        context.sendStickersByUuids(
                            uuids = selectedStickers.map { it.sticker.uuid },
                            onSuccess = { selectedStickers.forEach { it.sticker.shareCount++ } }
                        )
                    },
                    enabled = selectedStickers.isNotEmpty(),
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(id = R.string.home_screen_send_sticker)
                )
            },
            @Composable {
                RaysIconButton(
                    onClick = onExportClick,
                    enabled = selectedStickers.isNotEmpty(),
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(id = R.string.home_screen_export)
                )
            },
            @Composable {
                RaysIconButton(
                    onClick = onDeleteClick,
                    enabled = selectedStickers.isNotEmpty(),
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.home_screen_delete)
                )
            }
        )
    }
    if (windowSizeClass.isCompact) {
        Row {
            items.forEachIndexed { _, function -> function() }
        }
    } else {
        Column {
            items.forEachIndexed { _, function -> function() }
        }
    }
}

@Composable
fun SearchResultItem(
    modifier: Modifier = Modifier,
    data: StickerWithTags,
    selectable: Boolean = false,
    selected: Boolean = false,
    onClickListener: ((data: StickerWithTags, selected: Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    RaysOutlinedCard(
        modifier = modifier.fillMaxWidth(),
        onLongClick = {
            context.sendStickerByUuid(
                uuid = data.sticker.uuid,
                onSuccess = { data.sticker.shareCount++ }
            )
        },
        onClick = {
            onClickListener?.invoke(data, !selected)
        }
    ) {
        Box {
            RaysImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop,
                uuid = data.sticker.uuid
            )
            if (selectable && selected) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                        .padding(3.dp),
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null
                )
            }
        }
        if (data.sticker.title.isNotBlank()) {
            Text(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 10.dp)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                text = data.sticker.title,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}
