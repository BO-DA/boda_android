package com.example.boda.api.route;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseInfo {

    @SerializedName("type")
    private String type;

    @SerializedName("features")
    private List<Feature> features;
}
