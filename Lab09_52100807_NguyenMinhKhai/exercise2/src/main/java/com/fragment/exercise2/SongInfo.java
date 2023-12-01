package com.fragment.exercise2;

public class SongInfo {
    public String name;
    public String url;

    public int rawId;

    public SongInfo(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public SongInfo(String name, String url, int rawId) {
        this.name = name;
        this.url = url;
        this.rawId = rawId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRawId() {
        return rawId;
    }

    public void setRawId(int rawId) {
        this.rawId = rawId;
    }
}
