package it.dogior.hadEnough

import android.util.Log
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.newExtractorLink
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject

class VixSrcExtractor : ExtractorApi() {
    override val mainUrl = "vixsrc.to"
    override val name = "VixSrc"
    override val requiresReferer = false
    private val TAG = "VixSrcExtractor"
    private var referer: String? = null

    private val userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/133.0"

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        this.referer = referer ?: "https://vixsrc.to/"
        Log.d(TAG, "REFERER: $referer  URL: $url")

        val embedUrl = resolveEmbedUrl(url)
        val playlistUrl = getPlaylistLink(embedUrl)
        Log.w(TAG, "FINAL URL: $playlistUrl")

        callback.invoke(
            newExtractorLink(
                source = "VixSrc",
                name = "Streaming Community - VixSrc",
                url = playlistUrl,
                type = ExtractorLinkType.M3U8
            ) {
                this.referer = this@VixSrcExtractor.referer
                this.headers = mapOf("User-Agent" to userAgent)
            }
        )
    }

    /**
     * VixSrc non espone più il player direttamente nella pagina /movie/... o /tv/...
     * ma tramite le API JSON /api/movie/{tmdbId} e /api/tv/{tmdbId}/{season}/{episode}.
     */
    private suspend fun resolveEmbedUrl(url: String): String {
        if (url.contains("/embed/")) return url

        val apiUrl = runCatching {
            val segments = url.toHttpUrl().pathSegments.filter { it.isNotBlank() }
            when {
                segments.firstOrNull() == "movie" && segments.size >= 2 ->
                    "https://vixsrc.to/api/movie/${segments[1]}"

                segments.firstOrNull() == "tv" && segments.size >= 4 ->
                    "https://vixsrc.to/api/tv/${segments[1]}/${segments[2]}/${segments[3]}"

                else -> null
            }
        }.getOrNull() ?: return url

        return runCatching {
            val response = app.get(
                apiUrl,
                headers = mapOf(
                    "Accept" to "application/json",
                    "Referer" to "https://vixsrc.to/",
                    "User-Agent" to userAgent
                )
            )
            val src = JSONObject(response.text).optString("src")
            if (src.isBlank()) url else "https://vixsrc.to$src"
        }.getOrDefault(url)
    }

    private suspend fun getPlaylistLink(url: String): String {
        Log.d(TAG, "Item url: $url")

        val script = getScript(url)
        val masterPlaylist = script.getJSONObject("masterPlaylist")
        val masterPlaylistParams = masterPlaylist.getJSONObject("params")
        val token = masterPlaylistParams.getString("token")
        val expires = masterPlaylistParams.getString("expires")
        val playlistUrl = masterPlaylist.getString("url")

        val params = "token=$token&expires=$expires"
        var masterPlaylistUrl = if ("?" in playlistUrl) {
            "$playlistUrl&$params"
        } else {
            "$playlistUrl?$params"
        }

        if (script.optBoolean("canPlayFHD", false)) {
            masterPlaylistUrl += "&h=1"
        }

        Log.d(TAG, "Master Playlist URL: $masterPlaylistUrl")
        return masterPlaylistUrl
    }

    private suspend fun getScript(url: String): JSONObject {
        Log.d(TAG, "Item url: $url")

        val headers = mapOf(
            "Accept" to "*/*",
            "Connection" to "keep-alive",
            "Referer" to (referer ?: "https://vixsrc.to/"),
            "User-Agent" to userAgent
        )

        val resp = app.get(url, headers = headers, interceptor = CloudflareKiller()).document
        val script = resp.select("script")
            .firstOrNull { it.data().contains("masterPlaylist") }
            ?.data()
            ?.replace("\n", "\t")
            ?: throw IllegalStateException("VixSrc: masterPlaylist non trovato")

        val scriptJson = getSanitisedScript(script)
        Log.d(TAG, "Script Json: $scriptJson")
        return JSONObject(scriptJson)
    }

    private fun getSanitisedScript(script: String): String {
        val assignmentRegex = Regex("""window\.(\w+)\s*=""")
        val parts = assignmentRegex.split(script).drop(1)
        val keys = assignmentRegex.findAll(script).map { it.groupValues[1] }.toList()

        val jsonObjects = keys.zip(parts).map { (key, value) ->
            val cleaned = value
                .replace(";", "")
                .replace(Regex("""(\{|\[|,)\s*(\w+)\s*:"""), "$1 \"$2\":")
                .replace(Regex(""",(\s*[}\]])"""), "$1")
                .trim()

            "\"$key\": $cleaned"
        }

        return "{\n${jsonObjects.joinToString(",\n")}\n}".replace("'", "\"")
    }
}
