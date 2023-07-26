package com.example.boda.api.route;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Feature {
    @SerializedName("type")
    private String type;

    @SerializedName("geometry")
    private Geometry geometry;

    @SerializedName("properties")
    private PropertyRoute propertyRoute;
}