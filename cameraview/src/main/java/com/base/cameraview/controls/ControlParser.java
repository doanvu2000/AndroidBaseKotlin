package com.base.cameraview.controls;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.NonNull;

import com.base.cameraview.R;

/**
 * Parses controls from XML attributes.
 */
public class ControlParser {

    private int preview;
    private int facing;
    private int flash;
    private int grid;
    private int whiteBalance;
    private int mode;
    private int hdr;
    private int audio;
    private int videoCodec;
    private int audioCodec;
    private int engine;
    private int pictureFormat;

    public ControlParser(@NonNull Context context, @NonNull TypedArray array) {
        preview = array.getInteger(R.styleable.CameraView_cameraPreview, Preview.DEFAULT.getValue());
        facing = array.getInteger(R.styleable.CameraView_cameraFacing,
                Facing.defaultFacing(context).getValue());
        flash = array.getInteger(R.styleable.CameraView_cameraFlash, Flash.DEFAULT.getValue());
        grid = array.getInteger(R.styleable.CameraView_cameraGrid, Grid.DEFAULT.getValue());
        whiteBalance = array.getInteger(R.styleable.CameraView_cameraWhiteBalance,
                WhiteBalance.DEFAULT.getValue());
        mode = array.getInteger(R.styleable.CameraView_cameraMode, Mode.DEFAULT.getValue());
        hdr = array.getInteger(R.styleable.CameraView_cameraHdr, Hdr.DEFAULT.getValue());
        audio = array.getInteger(R.styleable.CameraView_cameraAudio, Audio.DEFAULT.getValue());
        videoCodec = array.getInteger(R.styleable.CameraView_cameraVideoCodec,
                VideoCodec.DEFAULT.getValue());
        audioCodec = array.getInteger(R.styleable.CameraView_cameraAudioCodec,
                AudioCodec.DEFAULT.getValue());
        engine = array.getInteger(R.styleable.CameraView_cameraEngine, Engine.DEFAULT.getValue());
        pictureFormat = array.getInteger(R.styleable.CameraView_cameraPictureFormat,
                PictureFormat.DEFAULT.getValue());
    }

    @NonNull
    public Preview getPreview() {
        return Preview.fromValue(preview);
    }

    @NonNull
    public Facing getFacing() {
        //noinspection ConstantConditions
        return Facing.fromValue(facing);
    }

    @NonNull
    public Flash getFlash() {
        return Flash.fromValue(flash);
    }

    @NonNull
    public Grid getGrid() {
        return Grid.fromValue(grid);
    }

    @NonNull
    public Mode getMode() {
        return Mode.fromValue(mode);
    }

    @NonNull
    public WhiteBalance getWhiteBalance() {
        return WhiteBalance.fromValue(whiteBalance);
    }

    @NonNull
    public Hdr getHdr() {
        return Hdr.fromValue(hdr);
    }

    @NonNull
    public Audio getAudio() {
        return Audio.fromValue(audio);
    }

    @NonNull
    public AudioCodec getAudioCodec() {
        return AudioCodec.fromValue(audioCodec);
    }

    @NonNull
    public VideoCodec getVideoCodec() {
        return VideoCodec.fromValue(videoCodec);
    }

    @NonNull
    public Engine getEngine() {
        return Engine.fromValue(engine);
    }

    @NonNull
    public PictureFormat getPictureFormat() {
        return PictureFormat.fromValue(pictureFormat);
    }
}
