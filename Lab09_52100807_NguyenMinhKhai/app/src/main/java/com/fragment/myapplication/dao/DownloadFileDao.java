package com.fragment.myapplication.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.fragment.myapplication.model.FileModel;

import java.util.List;
@Dao
public interface DownloadFileDao {
    @Query("SELECT * FROM File")
    List<FileModel> getAll();

    @Insert
    void insert(FileModel file);
}
