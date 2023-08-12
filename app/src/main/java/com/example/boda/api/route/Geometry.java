package com.example.boda.api.route;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Geometry {
    @SerializedName("type")
    private String  type;

    @JsonAdapter(CoordinateAdapter.class)
    @SerializedName("coordinates")
    private ArrayList<ArrayList<Double>> coordinates;

    private static class CoordinateAdapter implements JsonDeserializer<ArrayList<ArrayList<Double>>> {

        @Override
        public ArrayList<ArrayList<Double>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ArrayList<ArrayList<Double>> res = new ArrayList<>();
//            Log.d("deserialize", String.valueOf(json));

            if (String.valueOf(json).charAt(1) == '[') {
//                Log.d("checkRes", "valueOf 1 is [");
                res = context.deserialize(json, typeOfT);
            } else {
//                Log.d("checkRes", "else case");
                res.add(context.deserialize(json, ArrayList.class));
            }
//            Log.d("checkResult", res.toString());
            return res;
        }
    }
}
