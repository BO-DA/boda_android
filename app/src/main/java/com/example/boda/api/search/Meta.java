package com.example.boda.api.search;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meta {
    @SerializedName("total_count")
    private int total_count;

    @SerializedName("pageable_count")
    private int pageable_count;

    @SerializedName("is_end")
    private boolean is_end;

    @SerializedName("same_name")
    private SameName    sameName;
}
