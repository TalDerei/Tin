package edu.lehigh.cse216.tad222.backend;

public class FileUploaded{

    private String fileid;
    private int messageid;
    private long filesize;
    private String url;

    public FileUploaded(String fileid, int messageid, long filesize, String url){
        this.fileid = fileid;
        this.messageid = messageid;
        this.filesize = filesize;
        this.url = url;
    }

    public String getFileid(){
        return this.fileid;
    }

    public int getMessageid(){
        return this.messageid;
    }

    public long getFilesize(){
        return this.filesize;
    }

    public String getUrl(){
        return this.url;
    }
}