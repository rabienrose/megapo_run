package org.yczbj.ycvideoplayerlib.model;


public class Media {
    private int id;
    private String videoName;
    private String videoImage;
    private String unlock;
    private String position;
    private String percentage;
    private String num;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVideoName() {
        return videoName;
    }

    public String getBareName() {
        String[] vec = videoName.split("/");
        String filename = vec[vec.length-1];
        vec = filename.split("\\.");
        return vec[0];
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoImage() {
        return videoImage;
    }

    public void setVideoImage(String videoImage) {
        this.videoImage = videoImage;
    }

    public String getUnlock() {
        return unlock;
    }

    public void setUnlock(String unlock) {
        this.unlock = unlock;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }
    public Media(){}

    public Media(int id, String videoName, String videoImage, String unlock, String position, String percentage, String num) {
        this.id = id;
        this.videoName = videoName;
        this.videoImage = videoImage;
        this.unlock = unlock;
        this.position = position;
        this.percentage = percentage;
        this.num = num;
    }
}
