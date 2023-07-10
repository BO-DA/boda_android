package com.example.boda.route;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Geometry {
    @SerializedName("type")
    private String  type;

    @SerializedName("coordinates")
    private List<List<Integer>> coordinates;
}
