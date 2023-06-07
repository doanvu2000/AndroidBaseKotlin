package com.example.baseproject.base.utils.gallery

import android.net.Uri

data class MusicData(
    var trackId: Long? = null,
    var trackTitle: String? = null,
    var trackdata: String? = null,
    var trackDuration: Long? = null,
    var trackDisplayName: String? = null,
    var thumb: Uri? = null,
    var artist: String? = null
) {
    override fun toString(): String {
        return "{\"trackId\":$trackId,\n" +
                "\"trackTitle\": \"$trackTitle\",\n" +
                "\"trackdata\": \"$trackdata\",\n" +
                "\"trackDuration\":$trackDuration,\n" +
                "\"trackDuration\":$trackDuration,\n" +
                "\"trackDisplayName\": \"$trackDisplayName\",\n" +
                "\"thumb\": \"$thumb\",\n" +
                "\"artist\": \"$artist\"\n}"
    }
}
