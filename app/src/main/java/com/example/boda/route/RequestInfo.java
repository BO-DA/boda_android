package com.example.boda.route;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RequestInfo {
    @Expose
    @SerializedName("startX")
    private Double  startX;

    @Expose
    @SerializedName("startY")
    private Double  startY;

    @Expose
    @SerializedName("endX")
    private Double  endX;

    @Expose
    @SerializedName("endY")
    private Double  endY;

    @Expose
    @SerializedName("reqCoordType")
    private String  reqCoordType;

    @Expose
    @SerializedName("resCoordType")
    private String  resCoordType;

    @Expose
    @SerializedName("startName")
    private String  startName;

    @Expose
    @SerializedName("endName")
    private String  endName;
}
