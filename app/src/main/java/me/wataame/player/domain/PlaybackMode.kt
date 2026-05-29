package me.wataame.player.domain

enum class PlaybackMode(val label: String) {
    NORMAL("通常"),
    ONE_LOOP("1曲ループ"),
    FOLDER_LOOP("フォルダループ"),
    FOLDER_RANDOM("フォルダランダム"),
    ALL_RANDOM("全曲ランダム");

    fun next(): PlaybackMode = entries[(ordinal + 1) % entries.size]
}

data class ABRepeatState(
    val pointA: Long? = null,
    val pointB: Long? = null,
) {
    val isEnabled: Boolean get() = pointA != null && pointB != null && pointB > pointA
}
