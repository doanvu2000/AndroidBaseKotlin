package com.example.baseproject.base.utils.gallery

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.net.toUri
import com.example.baseproject.base.utils.extension.isSdk29
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object GalleryUtils {

    //region Constants

    private val EXTERNAL_COVERS_URI = "content://media/external/audio/albumart".toUri()

    @Suppress("InlinedApi")
    private const val AUDIO_COLUMN_ALBUM_ARTIST = MediaStore.Audio.AudioColumns.ALBUM_ARTIST

    private val projection: Array<String>
        get() = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.DATE_ADDED,
            MediaStore.Audio.AudioColumns.DATE_MODIFIED,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.SIZE,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.MIME_TYPE,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.YEAR,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.DATA,
            AUDIO_COLUMN_ALBUM_ARTIST
        )

    //endregion

    //region Gallery Albums

    /**
     * Get all albums from device gallery
     * Required permission: READ_EXTERNAL_STORAGE
     * Declare in Manifest.xml: android:requestLegacyExternalStorage="true"
     * @param activity Activity context
     * @param onComplete callback with list of albums
     */
    fun getAlbumModels(activity: Activity, onComplete: (List<AlbumModel>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            flowOnIO(folderListFromImages(activity)).execute(this) {
                onComplete.invoke(it)
            }
        }
    }

    /**
     * Get all photos from device gallery
     * @param activity Activity context
     * @param onComplete callback with list of images
     */
    fun getAllPhotoGallery(activity: Activity, onComplete: (List<ImageModel>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            flowOnIO(getAllPhotos(activity)).execute(this) {
                onComplete.invoke(it)
            }
        }
    }

    /**
     * Create folder list from all images grouped by album
     * @param context Activity context
     * @return list of albums with images
     */
    fun folderListFromImages(context: Activity): List<AlbumModel> {
        val allImages = getAllPhotos(context)
        val albumMap: MutableMap<String, AlbumModel> = LinkedHashMap()

        for (image in allImages) {
            val bucketName = image.albumName
            var folder = albumMap[bucketName]
            if (folder == null && bucketName != null) {
                folder = AlbumModel(coverUri = image.photoUri, name = bucketName)
                albumMap[bucketName] = folder
            }
            folder?.albumPhotos?.add(image)
        }

        val results = arrayListOf<AlbumModel>()
        if (allImages.isNotEmpty()) {
            val album = AlbumModel(
                name = "All Photo",
                albumPhotos = allImages,
                coverUri = allImages.firstOrNull()?.photoUri
            )
            results.add(album)
        }
        results.addAll(albumMap.values)
        return results
    }

    //endregion

    //region Images

    /**
     * Get collection URI for images based on Android version
     */
    private fun getImageCollectionUri(): Uri {
        return if (isSdk29()) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }

    /**
     * Check if MIME type is image
     */
    private fun isImage(mimeType: String?): Boolean {
        return mimeType?.startsWith("image") ?: false
    }

    /**
     * Get all photos from device gallery
     * @param activity Activity context
     * @return list of all images
     */
    fun getAllPhotos(activity: Activity): ArrayList<ImageModel> {
        val strArr = arrayOf("title", "_data", "_id", "bucket_display_name")
        val arrayList: ArrayList<ImageModel> = ArrayList()

        try {
            val managedQuery = activity.managedQuery(
                getImageCollectionUri(),
                strArr,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )

            for (i in 0 until managedQuery.count) {
                managedQuery.moveToPosition(i)
                val columnBucketName = managedQuery.getColumnIndex("bucket_display_name")
                val columnTitle = managedQuery.getColumnIndex("title")
                val columnData = managedQuery.getColumnIndex("_data")

                val title = managedQuery.getString(columnTitle)
                val data = managedQuery.getString(columnData)
                val bucketName = managedQuery.getString(columnBucketName)

                arrayList.add(ImageModel(title = title, albumName = bucketName, photoUri = data))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return arrayListOf()
        }
        return arrayList
    }

    //endregion

    //region Videos

    /**
     * Get all videos from device gallery
     * @param activity Activity context
     * @return list of video files with duration > 2 seconds
     */
    fun getMediaVideos(activity: Activity): ArrayList<ImageModel> {
        val strArr =
            arrayOf("title", MediaStore.Video.Media.DATA, "_id", "duration", "bucket_display_name")
        val arrayList: ArrayList<ImageModel> = ArrayList()

        try {
            val managedQuery = activity.managedQuery(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                strArr,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )

            for (i in 0 until managedQuery.count) {
                managedQuery.moveToPosition(i)
                val columnIndex = managedQuery.getColumnIndex("title")
                val columnIndex2 = managedQuery.getColumnIndex(MediaStore.Video.Media.DATA)
                val columnIndex3 = managedQuery.getColumnIndex("duration")
                val columnBucketName = managedQuery.getColumnIndex("bucket_display_name")

                val bucketName = managedQuery.getString(columnBucketName)
                val title = managedQuery.getString(columnIndex)
                val data = managedQuery.getString(columnIndex2)
                val duration = managedQuery.getLong(columnIndex3)

                // Only include videos longer than 2 seconds
                if (duration > 2000) {
                    arrayList.add(
                        ImageModel(
                            title = title,
                            albumName = bucketName,
                            photoUri = data,
                            duration = duration
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return arrayListOf()
        }
        return arrayList
    }

    /**
     * Create folder list from all videos grouped by album
     * @param context Activity context
     * @return list of video albums
     */
    fun folderListFromVideos(context: Activity): List<AlbumModel> {
        val allImages = getMediaVideos(context)
        val albumMap: MutableMap<String, AlbumModel> = LinkedHashMap()

        for (image in allImages) {
            val bucketName = image.albumName
            var folder = albumMap[bucketName]
            if (folder == null && bucketName != null) {
                folder = AlbumModel(coverUri = image.photoUri, name = bucketName)
                albumMap[bucketName] = folder
            }
            folder?.albumPhotos?.add(image)
        }

        val results = arrayListOf<AlbumModel>()
        if (allImages.isNotEmpty()) {
            val album = AlbumModel(
                name = "All Videos",
                albumPhotos = allImages,
                coverUri = allImages.firstOrNull()?.photoUri
            )
            results.add(album)
        }
        results.addAll(albumMap.values)
        return results
    }

    //endregion

    //region Audio/Music

    /**
     * Convert album ID to cover URI
     */
    private fun Long.toCoverUri() = ContentUris.withAppendedId(EXTERNAL_COVERS_URI, this)

    /**
     * Check if file is audio file
     */
    private fun isAudioFile(path: String): Boolean {
        return if (TextUtils.isEmpty(path)) {
            false
        } else {
            path.endsWith(".mp3")
        }
    }

    /**
     * Get all music files from device
     * @param contentResolver ContentResolver instance
     * @return list of music data
     */
    fun getMusicFiles(contentResolver: ContentResolver): ArrayList<MusicData> {
        val musicDataArrayList: ArrayList<MusicData> = arrayListOf()
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "is_music != 0",
            null,
            "title ASC"
        ) ?: return musicDataArrayList

        val storeId = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val trackId = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
        val trackTitle = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
        val trackDisplayName =
            cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
        val trackData = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA)
        val trackDuration = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
        val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)

        while (cursor.moveToNext()) {
            val path = trackData.let { cursor.getString(it) } ?: ""
            if (isAudioFile(path)) {
                val musicData = MusicData().apply {
                    this.trackId = cursor.getLong(trackId)
                    this.trackTitle = cursor.getString(trackTitle)
                    this.trackdata = path
                    this.trackDuration = cursor.getLong(trackDuration)
                    this.trackDisplayName = cursor.getString(trackDisplayName)
                    this.thumb = cursor.getLong(storeId).toCoverUri()
                    this.artist = cursor.getString(artistIndex)
                }
                musicDataArrayList.add(musicData)
            }
        }
        cursor.close()
        return musicDataArrayList
    }

    //endregion

    //region Media Duration

    /**
     * Get media duration from file
     * @param context Context instance
     * @return duration in milliseconds
     */
    fun File.getMediaDuration(context: Context): Long {
        if (!exists()) return 0
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(absolutePath))
            val duration = retriever.extractMetadata(METADATA_KEY_DURATION)
            duration?.toLongOrNull() ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        } finally {
            retriever.release()
        }
    }

    //endregion
}

//region Flow Utilities

/**
 * Create a Flow on IO dispatcher
 */
fun <T> flowOnIO(value: T) = flow {
    emit(value)
}.flowOn(Dispatchers.IO)

/**
 * Execute Flow with error handling and UI thread switching
 * @param scope CoroutineScope for execution
 * @param errors optional error callback
 * @param success success callback with result
 */
fun <T> Flow<T>.execute(
    scope: CoroutineScope,
    errors: ((err: String) -> Unit)? = null,
    success: (T) -> Unit
) {
    onStart {
        // Loading state can be handled here
    }.onEach { data ->
        data?.let {
            withContext(Dispatchers.Main) {
                success.invoke(it)
            }
        }
    }.catch { throwable ->
        Throwable(throwable).also { error ->
            error.printStackTrace()
            errors?.invoke(error.message ?: "Unknown error")
        }
    }.launchIn(scope)
}

//endregion
