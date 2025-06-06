package com.example.fileupload.model;

public class FileMetadata {

    private String filename;
    private String s3Url;
    private String uploadTime;
    private String s3key;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getS3Url() {
        return s3Url;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getS3Key() {
        return s3key;
    }
    public void setS3key(String s3key) {
        this.s3key = s3key;
    }

}
