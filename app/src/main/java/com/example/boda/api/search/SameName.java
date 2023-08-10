package com.example.boda.api.search;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SameName {
    @SerializedName("region")
    private String[]    region;

    @SerializedName("keyword")
    private String  keyword;

    @SerializedName("selected_region")
    private String  selected_region;
}
