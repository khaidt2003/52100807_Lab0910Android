package com.fragment.myapplication.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fragment.myapplication.R;

import java.util.Random;

@Entity(tableName = "File")
public class FileModel {
    public static final int STATUS_COMPLETE = 101;
    public static final int STATUS_WAITING = -1;
    public static final int STATUS_FAIL = -2;

    private static final int[] ICONS = {
            R.drawable.icon_archive,
            R.drawable.icon_image,
            R.drawable.icon_movie,
            R.drawable.icon_music,
            R.drawable.icon_office,
            R.drawable.icon_text,
            R.drawable.icon_other
    };

    private static final Random random = new Random();

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "size")
    private long size;
    @ColumnInfo(name = "progress")
    private int progress; // 101: success, -1: failed
    @ColumnInfo(name = "icon")
    private int icon;

    public FileModel() {
    }

    public FileModel(int id, String name, long size, int progress, int icon) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.progress = progress;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
