package io.github.wykopmobilny.domain.navigation

interface ClipboardService {

    suspend fun copy(text: String)
}
