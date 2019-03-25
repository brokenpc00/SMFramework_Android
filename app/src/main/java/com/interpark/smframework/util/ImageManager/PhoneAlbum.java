package com.interpark.smframework.util.ImageManager;

import java.util.ArrayList;

public class PhoneAlbum {
    private int id;
    private long bucketId;
    private int albumIndex;
    private String name;
    private String coverUri;    // 대표 이미지 file path
    private int photoCount;
    private ArrayList<PhonePhoto> albumPhotos;

    public int getId() {
        return id;
    }

    public int getAlbumIndex() {return  albumIndex;}

    public void setId(int id) {
        this.id = id;
    }

    public long getBucketId() {return bucketId;}

    public void setBucketId(long bucketId) {this.bucketId = bucketId;}

    public void setAlbumIndex(int albumIndex) {this.albumIndex=albumIndex;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(String coverUri) {
        this.coverUri = coverUri;
    }

    public int getPhotoCount() {return photoCount;}

    public void setPhotoCount(int photoCount) {this.photoCount = photoCount;}

    public ArrayList<PhonePhoto> getAlbumPhotos() {
        if (albumPhotos==null) {
            albumPhotos = new ArrayList<>();
        }
        return albumPhotos;
    }

    public void setAlbumPhotos(ArrayList<PhonePhoto> albumPhotos) {
        this.albumPhotos = albumPhotos;
    }
}
