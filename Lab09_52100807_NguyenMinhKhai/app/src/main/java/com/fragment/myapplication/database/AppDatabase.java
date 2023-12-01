package com.fragment.myapplication.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.fragment.myapplication.dao.DownloadFileDao;
import com.fragment.myapplication.model.DownloadFile;
import com.fragment.myapplication.model.FileModel;

@Database(entities = {FileModel.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DownloadFileDao fileDao();

}
