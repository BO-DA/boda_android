package com.example.boda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.boda.api.route.Feature;
import com.example.boda.api.route.RequestInfo;
import com.example.boda.api.route.ResponseInfo;
import com.example.boda.api.RetrofitAPI;
import com.example.boda.api.search.Document;
import com.example.boda.api.search.SearchResponseInfo;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainFragment extends Fragment
        implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {
    private MapView mapView;
    private ViewGroup mapViewContainer;
    SearchResponseInfo[] searchData = {null};
    ResponseInfo[] data = {null};

    ArrayList<ArrayList<ArrayList<Double>>> routes = new ArrayList<>();
    private int checkIndex = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_stream, container, false);
        super.onCreate(savedInstanceState);

        // 키 해시 얻기
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("키해시", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // 권한ID를 가져오기
        int permission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.INTERNET);

        int permission2 = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        int permission3 = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        // 권한이 열려 있는지 확인
        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            // 마쉬멜로우 이상 버전부터 권한을 물어본다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 권한 체크(READ_PHONE_STATE의 requestCode 를 1000으로 세팅
                requestPermissions(
                        new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1000);
            }
            return view;
        }

        mapView = new MapView(getActivity());
        mapViewContainer = (ViewGroup) getActivity().findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mapView.setShowCurrentLocationMarker(true);

        return view;
    }

    // Kakao map API로 장소 이름을 통해 검색하여, 최상단 장소의 위도, 경도를 가지고 옴
    private void searchPlace(SearchResponseInfo[] searchData, ResponseInfo[] data, Double nowLongitude, Double nowLatitude, String placeName) {
        Retrofit searchRetrofit = new Retrofit.Builder()
                .baseUrl("https://dapi.kakao.com/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI searchRetrofitAPI = searchRetrofit.create(RetrofitAPI.class);

        Map<String, String> queries = new HashMap<>();
        queries.put("query", placeName);
        queries.put("x", nowLongitude.toString());
        queries.put("y", nowLatitude.toString());
        queries.put("radius", "5000");
        searchRetrofitAPI.findPlace(queries).enqueue(new Callback<SearchResponseInfo>() {
            @Override
            public void onResponse(@NonNull Call<SearchResponseInfo> call, @NonNull Response<SearchResponseInfo> response) {
                searchData[0] = response.body();
                assert searchData[0] != null;
                Log.d("SearchResponse", searchData[0].toString());
                Document resBody = searchData[0].getDocuments()[0];

                // TTS 경로 안내 알림 + 팝업 메시지
                String ttsString = "현재 위치에서 " + resBody.getPlace_name() + "까지의 경로를 안내합니다.";
                Toast.makeText(getActivity(), ttsString, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), TtsActivity.class);
                intent.putExtra("SpeechText", ttsString);
                startActivity(intent);

                // 장소 검색이 성공한 경우에만 directionSearch를 통해 경로를 검색함
                directionSearch(data, nowLongitude, nowLatitude, Double.parseDouble(resBody.getX()), Double.parseDouble(resBody.getY()));
            }

            @Override
            public void onFailure(@NonNull Call<SearchResponseInfo> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // 출발지 (현위치)로부터 도착지 (현위치 반경 가장 가까운 장소 검색) 하여 도보 경로 반환
    private void directionSearch(ResponseInfo[] data, Double nowLongitude, Double nowLatitude, Double searchLongitude, Double searchLatitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://apis.openapi.sk.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        RequestInfo requestInfo = new RequestInfo(
                nowLongitude, nowLatitude, searchLongitude, searchLatitude,
                "WGS84GEO", "WGS84GEO", "현재 위치", "도착지");
        retrofitAPI.walkingDirection(requestInfo).enqueue(new Callback<ResponseInfo>() {
            @Override
            public void onResponse(@NonNull Call<ResponseInfo> call, @NonNull Response<ResponseInfo> response) {
                data[0] = response.body();
                assert data[0] != null;
                Log.d("RouteResponse", data[0].toString());

                for (int i = 0; i < data[0].getFeatures().size(); i++)
                    routes.add(data[0].getFeatures().get(i).getGeometry().getCoordinates());
                Log.d("checkRoutes", routes.toString());
                drawLineOnMap();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseInfo> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void drawLineOnMap() {

        MapPolyline mapPolyline = new MapPolyline();
        mapPolyline.setTag(1000);
        mapPolyline.setLineColor(Color.argb(100, 255, 0, 0));

        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).size() != 1) { // feature 가 여러 개 (경로인 경우)
                for (int j = 0; j < routes.get(i).size(); j++) { // 그리는 경로에 추가
                    mapPolyline.addPoint(MapPoint.mapPointWithGeoCoord(routes.get(i).get(j).get(1), routes.get(i).get(j).get(0)));
                }
            }
        }
        // 선 그리기
        mapView.addPolyline(mapPolyline);
        mapView.setZoomLevel(0, true);
    }

    Boolean isSearchRequested = false;
    Boolean isTalked = false;
    Boolean isWalkerArrived = false;

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.d("currentLocation", mapPointGeo.latitude + "  " + mapPointGeo.longitude);

        // API 통신
        if (!isSearchRequested) {
            String placeName = getActivity().getIntent().getStringExtra("sttResult");
            searchPlace(searchData, data, mapPointGeo.longitude, mapPointGeo.latitude, placeName);
            isSearchRequested = true;
        }

        if (data != null && data[0] != null && checkIndex < data[0].getFeatures().size() && !isWalkerArrived) {
            Feature nowIndexFeature = data[0].getFeatures().get(checkIndex);
            // 일반적 계산: 0.0001 차이는 10m 차이
            ArrayList<Double> nowIndex = nowIndexFeature.getGeometry().getCoordinates().get(0);

            if (Math.abs(nowIndex.get(0) - mapPointGeo.longitude) < 0.00015
                    && Math.abs(nowIndex.get(1) - mapPointGeo.latitude) < 0.00015) {
                // 위도와 경도 모두 현위치 20m 이내인 경우
                if (!isTalked) {
                    String ttsString = nowIndexFeature.getPropertyRoute().getDescription() + "하세요.";
                    Toast.makeText(getActivity(), ttsString, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), TtsActivity.class);
                    intent.putExtra("SpeechText", ttsString);
                    startActivity(intent);
                }
                isTalked = true;

                Log.d("RouteStatus", checkIndex + " 도착");
                while (checkIndex < data[0].getFeatures().size()
                        && data[0].getFeatures().get(checkIndex).getType() != "Point") {
                    checkIndex++;
                    isTalked = false;
                }
//                if (checkIndex == data[0].getFeatures().size()) {
//                    isWalkerArrived = true;
//                    onWalkerArrive();
//                }
                Log.d("RouteStatus", checkIndex + " / " + data[0].getFeatures().size());
            }
        }
    }

    public void onWalkerArrive() {
        // TTS 경로 안내 알림 + 팝업 메시지
        String ttsString = "목적지에 도착하여 경로 안내를 종료합니다.";
        Toast.makeText(getActivity(), ttsString, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), TtsActivity.class);
        intent.putExtra("SpeechText", ttsString);
        startActivity(intent);

//        Intent mainIntent = new Intent(getApplicationContext(), SttActivity.class);
//        startActivity(mainIntent);
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {}

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {}

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {}

    @Override
    public void onMapViewInitialized(MapView mapView) {}

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {}

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {}

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {}

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {}

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {}

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {}

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {}

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {}

}