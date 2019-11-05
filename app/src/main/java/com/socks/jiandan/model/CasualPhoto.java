package com.socks.jiandan.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CasualPhoto {
    public int result;
    public String message;
    public Data data;

    public static class Data {
        public String tid;
        public String author;
        @SerializedName("follower")
        public int fans;
        @SerializedName("threadcount")
        public int productionCount;
        @SerializedName("replies")
        public int replyCount;
        @SerializedName("boolrecommend")
        public int liked;
        @SerializedName("recommend_add")
        public int likeCount;
        public String message;
        @SerializedName("authorid")
        public String authorId;
        @SerializedName("groupid")
        public String groupId;
        @SerializedName("grouptitle")
        public String groupTitle;
        @SerializedName("avatarurl")
        public String avatarUrl;
        public List<ImageText> images;
    }

    public static class ImageText {
        public String url;
        @SerializedName("description")
        public String desc;
    }
}
