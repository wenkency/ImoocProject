package cn.carhouse.audio.bean;

import java.io.Serializable;

/**
 * 1.歌曲实体
 * 2.引入greendao以后扩展了许多
 */
public class AudioBean implements Serializable {
    private String id;
    // 地址
    private String url;

    // 歌名
    private String name;

    // 作者
    private String author;

    // 所属专辑
    private String album;

    private String albumInfo;

    // 专辑封面
    private String albumPic;

    // 时长
    private String totalTime;


    public AudioBean(String id, String url, String name, String author,
                     String album, String albumInfo, String albumPic, String totalTime) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.author = author;
        this.album = album;
        this.albumInfo = albumInfo;
        this.albumPic = albumPic;
        this.totalTime = totalTime;
    }

    public AudioBean() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String mUrl) {
        this.url = mUrl;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumPic() {
        return this.albumPic;
    }

    public void setAlbumPic(String albumPic) {
        this.albumPic = albumPic;
    }

    public String getAlbumInfo() {
        return this.albumInfo;
    }

    public void setAlbumInfo(String albumInfo) {
        this.albumInfo = albumInfo;
    }

    public String getTotalTime() {
        return this.totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }


    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof AudioBean)) {
            return false;
        }
        return ((AudioBean) other).id.equals(this.id);
    }

    @Override
    public String toString() {
        return "AudioBean{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
