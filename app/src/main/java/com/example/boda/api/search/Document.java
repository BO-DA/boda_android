package com.example.boda.api.search;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Document {
    @SerializedName("id")
    private String  id;

    @SerializedName("place_name")
    private String  place_name;

    @SerializedName("category_name")
    private String  category_name;

    @SerializedName("category_group_code")
    private String  category_group_code;

    @SerializedName("category_group_name")
    private String  category_group_name;

    @SerializedName("phone")
    private String  phone;

    @SerializedName("address_name")
    private String  address_name;

    @SerializedName("road_address_name")
    private String  road_address_name;

    @SerializedName("x")
    private String  x;

    @SerializedName("y")
    private String  y;

    @SerializedName("place_url")
    private String  place_url;

    @SerializedName("distance")
    private String  distance;
}
