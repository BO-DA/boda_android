package com.example.boda.route;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyRoute {
    // 길 안내, 도로 및 시설물 정보
    @SerializedName("index")
    private int index;

    // 길 안내, 도로 및 시설물 정보
    @SerializedName("name")
    private String  name;

    // 길 안내, 도로 및 시설물 정보
    @SerializedName("description")
    private String  description;

    // 길 안내, 도로 및 시설물 정보
    @SerializedName("facilityType")
    private String  facilityType;

    // 길 안내, 도로 및 시설물 정보
    @SerializedName("facilityName")
    private String  facilityName;

    // 길 안내
    @SerializedName("totalDistance")
    private int totalDistance;

    // 길 안내
    @SerializedName("totalTime")
    private int totalTime;

    // 길 안내
    @SerializedName("pointIndex")
    private int pointIndex;

    // 길 안내
    @SerializedName("nearPoiName")
    private String  nearPoiName;

    // 길 안내
    @SerializedName("nearPoiX")
    private Double  nearPoiX;

    // 길 안내
    @SerializedName("nearPoiY")
    private Double  nearPoiY;

    // 길 안내
    @SerializedName("turnType")
    private int  turnType;

    // 길 안내
    @SerializedName("pointType")
    private String  pointType;

    // 도로 및 시설물 정보
    @SerializedName("lineIndex")
    private int lineIndex;

    // 도로 및 시설물 정보
    @SerializedName("distance")
    private int distance;

    // 도로 및 시설물 정보
    @SerializedName("time")
    private int time;

    // 도로 및 시설물 정보
    @SerializedName("roadType")
    private int roadType;

    // 도로 및 시설물 정보
    @SerializedName("categoryRoadType")
    private int categoryRoadType;

}
