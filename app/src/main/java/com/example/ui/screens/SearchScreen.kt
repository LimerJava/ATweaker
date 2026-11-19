package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.SynonymSearchEngine
import com.example.model.TweakItem
import com.example.model.TweakOption
import com.example.ui.components.TweakCard
import com.example.ui.strings.Strings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    query: String,
    results: List<Pair<TweakItem, List<String>>>,
    tweakValues: Map<String, String>,
    isRu: Boolean,
    strings: Strings,
    bottomPadding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onToggleTweak: (TweakItem, Boolean) -> Unit,
    onSelectOption: (TweakItem, TweakOption) -> Unit,
    onRunAction: (TweakItem) -> Unit,
) {
    val chips = if (isRu) {
        SynonymSearchEngine.suggestedSynonymChipsRu
    } else {
        SynonymSearchEngine.suggestedSynonymChipsEn
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 6.dp,
            bottom = bottomPadding.calculateBottomPadding() + 92.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .testTag("synonym_search_input"),
                placeholder = { Text(strings.searchPlaceholder) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
            )
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = strings.searchSynonymsHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        chips.forEach { chip ->
                            AssistChip(
                                onClick = { onQueryChange(chip) },
                                label = { Text(chip) },
                                modifier = Modifier.testTag("synonym_chip_$chip"),
                            )
                        }
                    }
                }
            }
        }

        if (query.isBlank()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TravelExplore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = strings.searchEmptyTitle,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else if (results.isEmpty()) {
            item {
                Text(
                    text = if (isRu) "Ничего не найдено по запросу «$query»" else "No tweaks matched '$query'",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp),
                )
            }
        } else {
            items(results, key = { it.first.id }) { (tweak, matchedSynonyms) ->
                TweakCard(
                    tweak = tweak,
                    currentValue = tweakValues[tweak.id],
                    isRu = isRu,
                    strings = strings,
                    matchedSynonyms = matchedSynonyms,
                    onToggle = { enable -> onToggleTweak(tweak, enable) },
                    onSelectOption = { opt -> onSelectOption(tweak, opt) },
                    onRunAction = { onRunAction(tweak) },
                    modifier = Modifier.widthIn(max = 680.dp),
                )
            }
        }
    }
}
