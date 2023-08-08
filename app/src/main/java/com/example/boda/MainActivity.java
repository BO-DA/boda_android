package com.example.boda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.boda.api.route.RequestInfo;
import com.example.boda.api.route.ResponseInfo;
import com.example.boda.api.RetrofitAPI;
import com.example.boda.api.search.Document;
import com.example.boda.api.search.SearchResponseInfo;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {

    private MapView mapView;
    private ViewGroup mapViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 키 해시 얻기
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("키해시", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // 권한ID를 가져오기
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        int permission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int permission3 = ContextCompat.checkSelfPermission(this,
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
            return;
        }

        // 지도를 띄우자
        // java code
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mapView.setShowCurrentLocationMarker(true);
        mapView.setCurrentLocationRadius(20);

        // 현재 위치 시스템으로부터 가져오기
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location nowLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Double nowLatitude = nowLocation.getLatitude(); Log.d("Position.Latitude", String.valueOf(nowLatitude));
        Double nowLongitude = nowLocation.getLongitude();   Log.d("Position.Longitude", String.valueOf(nowLongitude));

        // API 통신
        final SearchResponseInfo[] searchData = {null};
        final ResponseInfo[] data = {null};

        // todo: TTS 구현
        // todo: TTS 말하기 ("원하시는 목적지를 말씀해주세요.")
        // todo: STT 수행
        String placeName = "던킨도너츠"; // todo: 장소 STT 받아서 전달하기
        searchPlace(searchData, data, nowLongitude, nowLatitude, placeName);

        Toast toast = Toast.makeText(this.getApplicationContext(), "현 위치에서 " + placeName + "까지의 경로를 안내합니다.", Toast.LENGTH_SHORT);
        toast.show();


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
                nowLongitude,
                nowLatitude,
                searchLongitude,
                searchLatitude,
                "WGS84GEO",
                "WGS84GEO",
                "현재 위치",
                "도착지");
        retrofitAPI.walkingDirection(requestInfo).enqueue(new Callback<ResponseInfo>() {
            @Override
            public void onResponse(@NonNull Call<ResponseInfo> call, @NonNull Response<ResponseInfo> response) {
                data[0] = response.body();
                assert data[0] != null;
                Log.d("RouteResponse", data[0].toString());
            }

            @Override
            public void onFailure(@NonNull Call<ResponseInfo> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    // 권한 체크 이후 로직
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        // READ_PHONE_STATE의 권한 체크 결과를 불러온다
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == 1000) {
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            // 권한 체크에 동의를 하지 않으면 안드로이드 종료
            if (!check_result) {
                finish();
            }
        }
    }
}