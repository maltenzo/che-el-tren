package ar.enzo.trenmitre

import android.content.Context
import org.json.JSONArray

/** Estación: id de la API SOFSE (= stop_id del GTFS) + nombre. */
data class Station(val id: Int, val name: String)

// Favoritos por defecto (primer arranque); el usuario los cambia con long-press.
private val DEFAULT_FAV_STATIONS = setOf(150, 271)        // Urquiza, Miguelete
private val DEFAULT_FAV_RAMALES = setOf(9)                // Mitre / Retiro - J. L. Suárez

fun loadFavStations(ctx: Context) = loadIntSet(ctx, "fav_stations", DEFAULT_FAV_STATIONS)
fun loadFavRamales(ctx: Context) = loadIntSet(ctx, "fav_ramales", DEFAULT_FAV_RAMALES)
fun saveFavStations(ctx: Context, s: Set<Int>) = saveIntSet(ctx, "fav_stations", s)
fun saveFavRamales(ctx: Context, s: Set<Int>) = saveIntSet(ctx, "fav_ramales", s)

/** Agrega o quita el id; devuelve el set nuevo (para disparar recomposición). */
fun toggle(set: Set<Int>, id: Int): Set<Int> =
    if (id in set) set - id else set + id

private fun loadIntSet(ctx: Context, key: String, default: Set<Int>): Set<Int> {
    val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val raw = p.getStringSet(key, null) ?: return default
    return raw.mapNotNull { it.toIntOrNull() }.toSet()
}

private fun saveIntSet(ctx: Context, key: String, s: Set<Int>) {
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putStringSet(key, s.map { it.toString() }.toSet()).apply()
}

/** Ramal con sus estaciones ordenadas (precalculado del GTFS en assets/lines.json). */
data class Ramal(val id: Int, val name: String, val stations: List<Station>)

fun loadRamales(ctx: Context): List<Ramal> {
    val json = ctx.assets.open("lines.json").bufferedReader().use { it.readText() }
    val arr = JSONArray(json)
    return (0 until arr.length()).map { i ->
        val r = arr.getJSONObject(i)
        val sts = r.getJSONArray("stations")
        Ramal(
            id = r.getInt("id"),
            name = r.getString("name"),
            stations = (0 until sts.length()).map { j ->
                val s = sts.getJSONObject(j)
                Station(s.getInt("id"), s.getString("name"))
            },
        )
    }
}

private const val PREFS = "cfg"

/** Estación elegida; por defecto Gral. Urquiza (Mitre / J.L. Suárez). */
fun loadStation(ctx: Context): Station =
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).run {
        Station(getInt("id", 150), getString("name", "Gral. Urquiza")!!)
    }

fun saveStation(ctx: Context, s: Station) {
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putInt("id", s.id).putString("name", s.name).apply()
}
