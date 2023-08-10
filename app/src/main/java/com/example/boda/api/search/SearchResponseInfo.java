package com.example.boda.api.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchResponseInfo {
    @Expose
    @SerializedName("meta")
    private Meta meta;

    @Expose
    @SerializedName("documents")
    private Document[] documents;
}
