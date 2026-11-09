// use an integer for version numbers
version = 36

cloudstream {
    description = "TV Shows and Movies from StreamingCommunity (auto-updating domain)"
    authors = listOf("doGior", "fra06083")

    /**
    * Status int as the following:
    * 0: Down
    * 1: Ok
    * 2: Slow
    * 3: Beta only
    * */
    status = 1
    tvTypes = listOf(
        "TvSeries",
        "Movie",
        "Documentary",
        "Cartoon"
    )

    requiresResources = true
    language = "it"

    iconUrl = "https://streamingcommunityz.nexus/apple-touch-icon.png?v=2"
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")
}
