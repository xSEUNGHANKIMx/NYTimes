package com.example.nytimes;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Article implements Parcelable {
    private static final String NYT_URL = "http://www.nytimes.com/";
    String mUrl, mHeadline, mMultimedia;

    public Article(Parcel in) {
        readFromParcel(in);
    }

    public String getWebUrl() {
        return mUrl;
    }

    public String getHeadline() {
        return mHeadline;
    }

    public String getThumbnail() {
        return mMultimedia;
    }

    public Article(String webUrl, String headline, String media) {
        this.mUrl = webUrl;
        this.mHeadline = headline;
        this.mMultimedia = media;
    }

    public Article(JSONObject jsonObject) {
        try {
            this.mUrl = jsonObject.getString("web_url");
            this.mHeadline = jsonObject.getJSONObject("headline").getString("main");
            JSONArray multimedia = jsonObject.getJSONArray("multimedia");
            if (multimedia.length() > 0) {
                this.mMultimedia = NYT_URL + multimedia.getJSONObject(0).getString("url");
            } else {
                this.mMultimedia = "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Article> fromJsonArray(JSONArray jsonArray) {
        ArrayList<Article> results = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++ ) {
            try {
                results.add(new Article(jsonArray.getJSONObject(i)));
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUrl);
        dest.writeString(this.mHeadline);
        dest.writeString(this.mMultimedia);
    }


    public void readFromParcel(Parcel in) {
        this.mUrl = in.readString();
        this.mHeadline = in.readString();
        this.mMultimedia = in.readString();
    }

}
