package com.fragment.myapplication;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.fragment.myapplication.dao.*;
import com.fragment.myapplication.database.AppDatabase;
import com.fragment.myapplication.model.DownloadFile;
import com.fragment.myapplication.model.FileModel;
import com.fragment.myapplication.network.Downloader;
import com.fragment.myapplication.recycler.FileAdapter;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText txtUrl;
    private Button btnDownload;
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private FileAdapter adapter;
    private List<DownloadFile> files;
    private int REQUEST_MANAGE_EXTERNAL_STORAGE = 1238123;
    private AppDatabase database;
    private DownloadFileDao fileDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Download Manager");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()){
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
            }
        }

        initViews();
        initObject();
        updateUI();
    }

    private void updateUI() {
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void initObject() {
        recyclerView.setAdapter(adapter);
        database = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "FileDownloader"
        ).build();
        adapter = new FileAdapter();
        fileDao = database.fileDao();
        files = new ArrayList<>();
        adapter.setFiles(files);

        recyclerView.setAdapter(adapter);
        FetchData fetchData = new FetchData();
        fetchData.execute();
    }

    private class FetchData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            fileDao.getAll().forEach(file -> {
                DownloadFile downloadFile = new DownloadFile();
                downloadFile.setIcon(file.getIcon());
                downloadFile.setName(file.getName());
                downloadFile.setProgress(file.getProgress());
                downloadFile.setSize(file.getSize());
                downloadFile.setId(UUID.randomUUID().toString());

                files.add(downloadFile);
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            adapter.notifyDataSetChanged();
            updateUI();
            super.onPostExecute(unused);
        }
    }

    private void initViews() {
        txtUrl = findViewById(R.id.txtUrl);
        btnDownload = findViewById(R.id.btnDownload);
        emptyView = findViewById(R.id.emptyView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));

        btnDownload.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    initViews();
                    initObject();
                    updateUI();
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        String downloadUrl = txtUrl.getText().toString().trim();
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setDownloadUrl(downloadUrl);

        adapter.add(downloadFile);
        updateUI();

        Downloader.fetchInfo(downloadFile, new Downloader.Callback() {
            @Override
            public void onFileInfoChanged(DownloadFile file) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });

                File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                Downloader.download(file, parent, new Downloader.Callback() {
                    @Override
                    public void onFileInfoChanged(DownloadFile file) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onComplete(DownloadFile file) {
                        file.setProgress(DownloadFile.STATUS_COMPLETE);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                        FileModel model = new FileModel();
                        model.setName(file.getName());
                        model.setIcon(file.getIcon());
                        model.setProgress(file.getProgress());
                        model.setSize(file.getSize());
                        fileDao.insert(model);
                    }

                    @Override
                    public void onError(Throwable t) {
                        file.setProgress(DownloadFile.STATUS_FAIL);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });

                        FileModel model = new FileModel();
                        model.setName(file.getName());
                        model.setIcon(file.getIcon());
                        model.setProgress(file.getProgress());
                        model.setSize(file.getSize());
                        fileDao.insert(model);
                    }
                });
            }

            @Override
            public void onComplete(DownloadFile file) {
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadFile.setProgress(DownloadFile.STATUS_FAIL);
                        adapter.notifyDataSetChanged();
                    }
                });

                FileModel model = new FileModel();
                model.setName(downloadFile.getName());
                model.setIcon(downloadFile.getIcon());
                model.setProgress(downloadFile.getProgress());
                model.setSize(downloadFile.getSize());
                fileDao.insert(model);
            }
        });
    }
}