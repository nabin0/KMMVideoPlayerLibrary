package com.github.nabin0.kmmvideoplayer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform