package ar.enzo.trenmitre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val repo: TrainRepository = SofseTrainRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TrenApp(repo) }
    }
}

private val Accents = listOf(Color(0xFF34D27B), Color(0xFF3FA9F5), Color(0xFFF5A623), Color(0xFFE05CC8))
private fun accent(i: Int) = Accents[i % Accents.size]

@Composable
fun TrenApp(repo: TrainRepository) {
    val ctx = LocalContext.current
    var station by remember { mutableStateOf(loadStation(ctx)) }
    var favStations by remember { mutableStateOf(loadFavStations(ctx)) }
    var favRamales by remember { mutableStateOf(loadFavRamales(ctx)) }
    // Navegación simple: null = arribos; picking = elegir ramal; ramal != null = elegir estación.
    var picking by remember { mutableStateOf(false) }
    var ramal by remember { mutableStateOf<Ramal?>(null) }

    when {
        ramal != null -> StationPicker(
            ramal = ramal!!,
            favs = favStations,
            onToggleFav = { favStations = toggle(favStations, it).also { s -> saveFavStations(ctx, s) } },
            onBack = { ramal = null },
            onPick = { picked ->
                saveStation(ctx, picked); station = picked; ramal = null; picking = false
            },
        )
        picking -> {
            val ramales = remember { loadRamales(ctx) }
            RamalPicker(
                ramales = ramales,
                favs = favRamales,
                onToggleFav = { favRamales = toggle(favRamales, it).also { s -> saveFavRamales(ctx, s) } },
                onBack = { picking = false },
                onPick = { ramal = it },
            )
        }
        else -> ArrivalsScreen(repo, station, onOpenSettings = { picking = true })
    }
}

@Composable
private fun ArrivalsScreen(repo: TrainRepository, station: Station, onOpenSettings: () -> Unit) {
    var dirs by remember { mutableStateOf<List<Direction>>(emptyList()) }
    var idx by remember { mutableIntStateOf(0) }
    var firstLoad by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) { delay(60_000); tick++ }
    }
    LaunchedEffect(station, tick) {
        refreshing = true
        dirs = repo.directions(station.id)
        if (idx >= dirs.size) idx = 0
        firstLoad = false
        refreshing = false
    }

    val color by animateColorAsState(accent(idx), label = "accent")

    MaterialTheme {
        Scaffold(timeText = { TimeText() }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.22f), Color.Black)))
                    .clickable { tick++ },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val towards = dirs.getOrNull(idx)?.towards
                    Pill("⇄  ${towards ?: "—"}", color) {
                        if (dirs.size > 1) idx = (idx + 1) % dirs.size
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.caption1,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable(onClick = onOpenSettings),
                    )
                    Spacer(Modifier.height(6.dp))

                    when {
                        firstLoad -> CircularProgressIndicator(indicatorColor = color)
                        dirs.isEmpty() -> Text(
                            "Sin trenes",
                            style = MaterialTheme.typography.title3,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                        else -> Countdown(dirs[idx].arrivals, color)
                    }
                }

                if (refreshing && !firstLoad) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp).size(14.dp),
                        strokeWidth = 2.dp,
                        indicatorColor = color,
                    )
                }
            }
        }
    }
}

private val labels = listOf("próximo", "siguiente", "3º")

@Composable
private fun Countdown(arrivals: List<Arrival>, color: Color) {
    if (arrivals.isEmpty()) {
        Text("Sin trenes", style = MaterialTheme.typography.title3, color = Color.White.copy(alpha = 0.6f))
        return
    }
    val pager = rememberPagerState { arrivals.size }
    LaunchedEffect(arrivals) { pager.scrollToPage(0) }

    HorizontalPager(state = pager) { page ->
        val mins = arrivals[page].minutes
        val pulse by animateFloatAsState(if (mins <= 2) 1f else 0.85f, label = "pulse")
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = labels.getOrElse(page) { "${page + 1}º" },
                style = MaterialTheme.typography.caption2,
                color = Color.White.copy(alpha = 0.6f),
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$mins", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = pulse))
                Spacer(Modifier.width(4.dp))
                Text(
                    "min",
                    style = MaterialTheme.typography.title3,
                    color = color,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
        }
    }

    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(arrivals.size) { i ->
            val on = i == pager.currentPage
            Box(
                Modifier
                    .size(if (on) 7.dp else 5.dp)
                    .clip(CircleShape)
                    .background(if (on) color else Color.White.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
private fun Pill(text: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 5.dp),
    ) {
        Text(text, style = MaterialTheme.typography.caption1, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun RamalPicker(
    ramales: List<Ramal>, favs: Set<Int>,
    onToggleFav: (Int) -> Unit, onBack: () -> Unit, onPick: (Ramal) -> Unit,
) {
    val ordered = ramales.sortedBy { it.id !in favs }
    PickerList("Elegí ramal", onBack) {
        items(ordered) { r ->
            FavChip(r.name, r.id in favs, maxLines = 2, onClick = { onPick(r) }, onLongClick = { onToggleFav(r.id) })
        }
    }
}

@Composable
private fun StationPicker(
    ramal: Ramal, favs: Set<Int>,
    onToggleFav: (Int) -> Unit, onBack: () -> Unit, onPick: (Station) -> Unit,
) {
    val ordered = ramal.stations.sortedBy { it.id !in favs }
    PickerList("Elegí estación", onBack) {
        items(ordered) { s ->
            FavChip(s.name, s.id in favs, maxLines = 1, onClick = { onPick(s) }, onLongClick = { onToggleFav(s.id) })
        }
    }
}

/** Chip de ancho completo. Tap = seleccionar; mantener apretado = fav on/off. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun FavChip(
    text: String, fav: Boolean, maxLines: Int,
    onClick: () -> Unit, onLongClick: () -> Unit,
) {
    val bg = if (fav) MaterialTheme.colors.primary.copy(alpha = 0.30f)
    else Color.White.copy(alpha = 0.08f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(bg)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = if (fav) "★ $text" else text,
            maxLines = maxLines,
            style = MaterialTheme.typography.button,
            color = Color.White,
        )
    }
}

@Composable
private fun PickerList(
    title: String,
    onBack: () -> Unit,
    content: androidx.wear.compose.foundation.lazy.ScalingLazyListScope.() -> Unit,
) {
    MaterialTheme {
        Scaffold(timeText = { TimeText() }) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Chip(
                        onClick = onBack,
                        label = { Text("‹ $title") },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                content()
            }
        }
    }
}
