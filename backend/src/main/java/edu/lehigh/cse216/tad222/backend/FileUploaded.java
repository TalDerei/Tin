package edu.lehigh.cse216.tad222.backend;

public class FileUploaded{

    private String fileid;
    private int messageid;
    private String mime;
    private String url;
    private String fname;
    private long size;

    public FileUploaded(String fileid, int messageid, String mime,  String url, String fname, long size){
        this.fileid = fileid;
        this.messageid = messageid;
        this.mime = mime;
        this.url = url;
        this.fname = fname;
        this.size = size;
    }

    public String getFileid(){
        return this.fileid;
    }

    public int getMessageid(){
        return this.messageid;
    }

    public long getFilesize(){
        return this.size;
    }

    public String getUrl(){
        return this.url;
    }
}