package ar.enzo.trenmitre

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** Minutos hasta que llega el tren. */
data class Arrival(val minutes: Int)

/** Un destino ("tren") disponible en la estación, con sus próximos arribos. */
data class Direction(val towards: String, val arrivals: List<Arrival>)

interface TrainRepository {
    suspend fun directions(stationId: Int): List<Direction>
}

/**
 * Arribos en vivo del endpoint público de SOFSE. Sin credenciales:
 * GET /v1/arribos/estacion/{id} devuelve JSON. Se agrupa por destino terminal
 * (última estación del servicio), que separa naturalmente ramal y sentido.
 * Parseo en parseDirections() para poder probarlo sin red.
 */
class SofseTrainRepository(
    private val client: OkHttpClient = OkHttpClient(),
) : TrainRepository {

    override suspend fun directions(stationId: Int): List<Direction> = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("https://api-servicios.sofse.gob.ar/v1/arribos/estacion/$stationId")
            .header("x-api-origen", "wear")
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string()
            if (!resp.isSuccessful || body == null) emptyList() else parseDirections(body)
        }
    }

    companion object {
        fun parseDirections(json: String): List<Direction> {
            val results = JSONObject(json).optJSONArray("results") ?: return emptyList()
            val byDest = linkedMapOf<String, MutableList<Int>>()
            for (i in 0 until results.length()) {
                val r = results.getJSONObject(i)
                val serv = r.getJSONObject("servicio")
                if (!serv.isNull("cancelacion")) continue
                val mins = r.getJSONObject("arribo").getInt("segundos") / 60
                if (mins < 0) continue
                byDest.getOrPut(terminus(serv)) { mutableListOf() }.add(mins)
            }
            return byDest.map { (dest, mins) -> Direction(dest, mins.sorted().take(3).map(::Arrival)) }
                .sortedBy { it.arrivals.firstOrNull()?.minutes ?: Int.MAX_VALUE }
        }

        /** Destino real del servicio: última estación; si no, cabecera final del ramal. */
        private fun terminus(serv: JSONObject): String {
            val est = serv.optJSONArray("estaciones")
            if (est != null && est.length() > 0) {
                est.getJSONObject(est.length() - 1).optString("nombre").takeIf { it.isNotEmpty() }
                    ?.let { return it }
            }
            return serv.optJSONObject("ramal")?.optJSONObject("cabeceraFinal")
                ?.optString("nombre").orEmpty().ifEmpty { "—" }
        }
    }
}
