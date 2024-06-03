package com.example.myapplication.ui.weather;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Context;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.util.Log;

public class api extends Thread {
    class LatXLngY
    {
        public double lat;
        public double lng;

        public double x;
        public double y;

    }
    public String rainq,wind,rain,temperature,sky,windvec;
    public static int TO_GRID = 0;
    public static int TO_GPS = 1;
    private final Context mContext;
    public api(Context context) {
        this.mContext = context;
    }
    private GpsTracker gpsTracker;

    public String func() throws InterruptedException {
        gpsTracker = new GpsTracker(mContext);
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();
        String address = getCurrentAddress(latitude, longitude);
        String endPoint =  "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/";
        String serviceKey = "FWotZoUL42BFQn3tCT%2Bs9rTJWZWIHjhwJdY9E%2Ft0uijolGyYoc28z%2FLSPfmsC8o12oIGvJtaOaFbJGbmKopbwQ%3D%3D";
        String pageNo = "1";
        String numOfRows = "10";
        String baseDate; //원하는 날짜
        String baseTime; //원하는 시간
        String nx; //위경도임.
        String ny; //위경도 정보는 api문서 볼 것

        //String nx = "63"; //위경도임.
        //String ny = "124"; //위경도 정보는 api문서 볼 것
        long rt = System.currentTimeMillis();
        Date rtdate = new Date(rt);
        SimpleDateFormat df = new SimpleDateFormat("hh00");
        SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMdd");
        baseTime = timeChange(df.format(rtdate));
        baseDate = df2.format(rtdate);
        System.out.println(baseDate+baseTime+" "+latitude+" "+longitude+" "+" "+gpsTracker.getLocation());
        LatXLngY tmp = convertGRID_GPS(TO_GRID, latitude, longitude);
        nx = Integer.toString((int)(tmp.x > 144 ? 63 : tmp.x));
        ny = Integer.toString((int)(tmp.y > 147 ? 124 : tmp.y));
        System.out.println(">>" + "x = " + nx + ", y = " + ny);
        String s = endPoint+"getVilageFcst?serviceKey="+serviceKey
                +"&pageNo=" + pageNo
                +"&numOfRows=" + numOfRows
                +"+&dataType=JSON"
                + "&base_date=" +baseDate
                +"&base_time="+baseTime
                +"&nx="+nx
                +"&ny="+ny;
        Thread t = new Thread(()  -> {
            URL url = null;
            try {
                url = new URL(s);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                conn.setRequestMethod("GET");
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            }


            BufferedReader bufferedReader;
            try {
                if(conn.getResponseCode() == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                }else{
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));//connection error :(
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while (true) {
                try {
                    if (!((line = bufferedReader.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                stringBuilder.append(line);
            }
            try {
                bufferedReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String result= stringBuilder.toString();
            conn.disconnect();
            JSONObject mainObject = null;
            JSONArray itemArray = null;
            try {
                mainObject = new JSONObject(result);
                JSONObject jsonO1 = new JSONObject(result);
                String response = jsonO1.getString("response");

                JSONObject json02 = new JSONObject(response);
                String body = json02.getString("body");

                JSONObject json03 = new JSONObject(body);
                String items = json03.getString("items");
                Log.i("items",s);
                JSONObject json04 = new JSONObject(items);
                itemArray = json04.getJSONArray("item");

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            /** JSONArray itemArray = null;
             try {
             itemArray = mainObject.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item");
             } catch (JSONException e) {
             throw new RuntimeException(e);
             } **/
            for(int i=0; i<itemArray.length(); i++){
                JSONObject item = null;
                try {
                    item = itemArray.getJSONObject(i);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String category = null;
                category = item.optString("category");
                String value = null;
                value = item.optString("fcstValue");
                System.out.println(category+"  "+value);
                if (category.equals("SKY")) {
                    if (value.equals("1")) {
                        sky = "맑음 ";
                    } else if (value.equals("2")) {
                        sky = "비 ";
                    } else if (value.equals("3")) {
                        sky = "구름많음 ";
                    } else if (value.equals("4")) {
                        sky = "흐림 ";
                    }
                }
                if (category.equals("TMP")) {
                    temperature = value + "℃ ";
                }
                if(category.equals("WSD")) {
                    wind = value + "m/s ";
                }
                if(category.equals("VEC")) {
                    windvec = (Math.floor((Double.parseDouble(value) + 22.5*0.5)/22.5)) + " ";
                }
                if(category.equals("POP")) {
                    rain = value + "% ";
                }
                if(category.equals("PCP")) {
                    rainq = (value.equals("강수없음")?"강수없음 ":value +"% ");
                }
            }
            System.out.println(rainq+" "+wind+" "+rain+" "+temperature+" "+sky+" "+windvec);

        });
        t.start();
        t.join();
        return rainq+wind+rain+temperature+sky+windvec+address;
    }
    public String timeChange(String time)
    {
        // 현재 시간에 따라 데이터 시간 설정(3시간 마다 업데이트) //
        switch(time) {

            case "0200":
            case "0300":
            case "0400":
                time = "0200";
                break;
            case "0500":
            case "0600":
            case "0700":
                time = "0500";
                break;
            case "0800":
            case "0900":
            case "1000":
                time = "0800";
                break;
            case "1100":
            case "1200":
            case "1300":
                time = "1100";
                break;
            case "1400":
            case "1500":
            case "1600":
                time = "1400";
                break;
            case "1700":
            case "1800":
            case "1900":
                time = "1700";
                break;
            case "2000":
            case "2100":
            case "2200":
                time = "2000";
                break;
            case "2300":
            case "0000":
            case "0100":
                time = "2300";

        }
        return time;
    }
    private LatXLngY convertGRID_GPS(int mode, double lat_X, double lng_Y )
    {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기1준점 Y좌표(GRID)

        //
        // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
        //


        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        LatXLngY rs = new LatXLngY();

        if (mode == TO_GRID) {
            rs.lat = lat_X;
            rs.lng = lng_Y;
            double ra = Math.tan(Math.PI * 0.25 + (lat_X) * DEGRAD * 0.5);
            ra = re * sf / Math.pow(ra, sn);
            double theta = lng_Y * DEGRAD - olon;
            if (theta > Math.PI) theta -= 2.0 * Math.PI;
            if (theta < -Math.PI) theta += 2.0 * Math.PI;
            theta *= sn;
            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
        }
        else {
            rs.x = lat_X;
            rs.y = lng_Y;
            double xn = lat_X - XO;
            double yn = ro - lng_Y + YO;
            double ra = Math.sqrt(xn * xn + yn * yn);
            if (sn < 0.0) {
                ra = -ra;
            }
            double alat = Math.pow((re * sf / ra), (1.0 / sn));
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

            double theta = 0.0;
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0;
            }
            else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5;
                    if (xn < 0.0) {
                        theta = -theta;
                    }
                }
                else theta = Math.atan2(xn, yn);
            }
            double alon = theta / sn + olon;
            rs.lat = alat * RADDEG;
            rs.lng = alon * RADDEG;
        }
        return rs;
    }
    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(mContext, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(mContext, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(mContext, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString();

    }
}