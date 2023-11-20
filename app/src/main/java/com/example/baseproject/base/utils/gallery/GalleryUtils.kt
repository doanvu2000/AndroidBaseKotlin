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
    /**
     * required permission: READ_EXTERNAL_STORAGE to read data
     * declare in tag application of Manifest.xml:  android:requestLegacyExternalStorage="true"
     * */
    fun getAlbumModels(activity: Activity, onComplete: (List<AlbumModel>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            flowOnIO(folderListFromImages(activity)).execute(this) {
                onComplete.invoke(it)
            }
        }
    }

    fun getAllPhotoGallery(activity: Activity, onComplete: (List<ImageModel>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            flowOnIO(getAllPhotos(activity)).execute(this) {
                onComplete.invoke(it)
            }
        }
    }

    private val EXTERNAL_COVERS_URI = Uri.parse("content://media/external/audio/albumart")

    private fun Long.toCoverUri() = ContentUris.withAppendedId(EXTERNAL_COVERS_URI, this)

    private fun isImage(mimeType: String?): Boolean {
        return mimeType?.startsWith("image") ?: false
    }

    private fun getImageCollectionUri(): Uri {
        return if (isSdk29()) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    //get all photo in gallery
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
        } catch (e2: Exception) {
            e2.printStackTrace()
            return arrayListOf()
        }
        return arrayList
    }

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
                if (managedQuery.getLong(columnIndex3) > 2000) {
                    arrayList.add(
                        ImageModel(
                            title = title,
                            albumName = bucketName,
                            photoUri = data,
                            duration = managedQuery.getLong(columnIndex3)
                        )
                    )
                }
            }
        } catch (e2: Exception) {
            e2.printStackTrace()
            return arrayListOf()
        }
        return arrayList
    }

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

    @Suppress("InlinedApi")
    private const val AUDIO_COLUMN_ALBUM_ARTIST = MediaStore.Audio.AudioColumns.ALBUM_ARTIST

    val projection: Array<String>
        get() = arrayOf(
            // These columns are guaranteed to work on all versions of android
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

    fun getMusicFiles(contentResolver: ContentResolver): java.util.ArrayList<MusicData> {
        val musicDataArrayList: java.util.ArrayList<MusicData> = arrayListOf()
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
                val musicData = MusicData()
                musicData.trackId = trackId.let { cursor.getLong(it) }
                musicData.trackTitle = trackTitle.let { cursor.getString(it) }
                musicData.trackdata = path
                musicData.trackDuration = trackDuration.let { cursor.getLong(it) }
                musicData.trackDisplayName = trackDisplayName.let { cursor.getString(it) }
                musicData.thumb = cursor.getLong(storeId).toCoverUri()
                musicData.artist = cursor.getString(artistIndex)
                musicDataArrayList.add(musicData)
            }
        }
        cursor.close()
        return musicDataArrayList
    }

    private fun isAudioFile(path: String): Boolean {
        return if (TextUtils.isEmpty(path)) {
            false
        } else path.endsWith(".mp3")
    }

    fun File.getMediaDuration(context: Context): Long {
        if (!exists()) return 0
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.parse(absolutePath))
        val duration = retriever.extractMetadata(METADATA_KEY_DURATION)
        retriever.release()
        return duration?.toLongOrNull() ?: 0
    }
}

fun <T> flowOnIO(value: T) = flow {
    emit(value)
}.flowOn(Dispatchers.IO)

fun <T> Flow<T>.execute(
    scope: CoroutineScope,
    errors: ((err: String) -> Unit)? = null,
    success: (T) -> Unit
) {
    onStart {
//        isLoading.value = true
    }.onEach {
        it?.let { data ->
            withContext(Dispatchers.Main) {
                success.invoke(data)
//                isLoading.value = false
            }
        } ?: kotlin.run {
//            isLoading.value = false
        }
    }.catch {
//        isLoading.value = false
        Throwable(it).also { throwable ->
//            handlerError(throwable, errors)
            throwable.printStackTrace()
        }
    }.launchIn(scope)
}