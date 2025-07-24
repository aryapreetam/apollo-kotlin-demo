package com.apollokotlindemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform