package com.yorkyu.weathervision.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import com.yorkyu.weathervision.R;
import com.yorkyu.weathervision.app.MyApplication;
import com.yorkyu.weathervision.model.City;
import com.yorkyu.weathervision.model.TodayWeather;
import com.yorkyu.weathervision.util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final String TAG = "myWeather";
    private static final String DEFAULT_CITY_CODE = "default_city_code";
    private ImageView mUpdateBtn, mLocationBtn, mShareBtn;
    private ImageView mCitySelect;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;
    private RotateAnimation rotateAnimation;
    private LocationClient mLocationClient;
    private List<City> mCityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intiView();
        mCityList = ((MyApplication)getApplication()).getCityList();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        initLocation();
        // 设置监听器
        mUpdateBtn.setOnClickListener(this);
        mLocationBtn.setOnClickListener(this);
        mShareBtn.setOnClickListener(this);

        // 绑定控件
        mCitySelect = findViewById(R.id.title_city_manager);
        // 设置监听器
        mCitySelect.setOnClickListener(this);

        // 设置一个放置动画用于刷新按钮
        rotateAnimation = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(500);
        rotateAnimation.setRepeatCount(Animation.INFINITE);

    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //就是这个方法设置为 true，才能获取当前的位置信息
        option.setIsNeedAddress(true);
        option.setOpenGps(true);

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("gcj02");//可选，默认 gcj02，设置返回的定位结果坐标系
        //int span = 1000;
        //option.setScanSpan(span);//可选，默认 0，即仅定位一次，设置发起定位请求的间隔需要大于等于 1000ms 才是有效的
        mLocationClient.setLocOption(option);
    }

    /**
     * 初始化主界面元素，先绑定 View，再设置内容
     */
    private void intiView() {
        city_name_Tv = findViewById(R.id.title_city_name);
        cityTv = findViewById(R.id.city);
        timeTv = findViewById(R.id.time);
        humidityTv = findViewById(R.id.humidity);
        weekTv = findViewById(R.id.week_today);
        pmDataTv = findViewById(R.id.pm_data);
        pmQualityTv = findViewById(R.id.pm2_5_quality);
        temperatureTv = findViewById(R.id.temperature);
        climateTv = findViewById(R.id.climate);
        windTv = findViewById(R.id.wind);

        mLocationBtn = findViewById(R.id.title_location);
        mShareBtn = findViewById(R.id.title_share);
        mUpdateBtn = findViewById(R.id.title_update_btn);

        weatherImg = findViewById(R.id.weather_img);
        pmImg = findViewById(R.id.pm2_5_img);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

    }


    /**
     * Handler 用于异步处理，将结果交线主线程即 UI 线程更新界面数据
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_city_manager) {
            // 通过 Intent 跳转到城市选择界面
            Intent i = new Intent(this, SelectActivity.class);
            startActivityForResult(i,1);
        }
        if (v.getId() == R.id.title_update_btn) {
            // 通过 SharedPreferences 存储 city code
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString(DEFAULT_CITY_CODE, "101010100");
            Log.d("myWeahter city code", cityCode);


            // 检测网络连接
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");

                mUpdateBtn.startAnimation(rotateAnimation);

                queryWeatherCode(cityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
            }
        }
        if (v.getId() == R.id.title_location) {
            // 检测网络连接
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");

                mLocationBtn.setClickable(false);
                mLocationBtn.startAnimation(rotateAnimation);
                mLocationClient.start();
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
            }

        }
    }

    /**
     *  接收其它界面返回后的值来做判断
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为" + newCityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d(TAG, address);
        // 在非主线程进行网络请求，否则可能会产生 ANR
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 定义 URL connection
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();     // 打开http 连接
                    con.setRequestMethod("GET");                        // 设置请求方式为 GET
                    con.setConnectTimeout(8000);                        // 设置连接超时时间为8000毫秒
                    con.setReadTimeout(8000);                           // 设置从连接读取超时时间为8000毫秒
                    InputStream in = con.getInputStream();              // 从连接中获取输入流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();       // 初始化拼接字符对象
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);                           // 从流中读取每一行并加入到 response 中
                    }
                    String responseStr = response.toString();
//                    Log.d(TAG, responseStr);
                    if (!responseStr.contains("<error>")) {                 // 处理没有对应的city code 返回结果的情形
                        todayWeather = parseXML(responseStr);               // 从获取的数据中解析需要的内容到 TodayWeather 实体中
                        if (todayWeather != null) {                         // 获取的数据不为空，则将解析后的数据发送到主线程进行显示
                            Log.d(TAG, todayWeather.toString());

                            Message msg = new Message();
                            msg.what = UPDATE_TODAY_WEATHER;
                            msg.obj = todayWeather;
                            mHandler.sendMessage(msg);

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 清除动画及使用更新按钮可点击
                            mUpdateBtn.clearAnimation();
                            mUpdateBtn.setClickable(true);
                        }
                    });

                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmldata) {
        TodayWeather todayWeather = null;
        int fengxiangCount = 0, fengliCount = 0, dateCount = 0, highCount = 0, lowCount = 0, typeCount = 0;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d(TAG, "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        // 有resp则初始化新的 TodayWeather 实体
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        // 依据不同的标签开始点解析到对应的字段
                        if (xmlPullParser.getName().equals("city")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setCity(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("updatetime")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setUpdatetime(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("shidu")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setShidu(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("wendu")) {
                            eventType = xmlPullParser.next();
                            Log.d(TAG, "wendu: " + xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("pm25")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setPm25(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("quality")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setQuality(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setFengxiang(xmlPullParser.getText());
                            fengxiangCount++;
                        } else if ((xmlPullParser.getName().equals("fengli")) && fengliCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setFengli(xmlPullParser.getText());
                            fengliCount++;
                        } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setDate(xmlPullParser.getText());
                            dateCount++;
                        } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setHigh(xmlPullParser.getText());
                            highCount++;
                        } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setLow(xmlPullParser.getText());
                            lowCount++;
                        } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setType(xmlPullParser.getText());
                            typeCount++;
                        }
                        break;
                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }


    /**
     * 更新天气信息到主界面中
     * @param todayWeather 天气信息
     */
    void updateTodayWeather(TodayWeather todayWeather) {
        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度:" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:" + todayWeather.getFengli());

        // 依据解析的 PM2.5值更新相应的人物头像
        if (todayWeather.getPm25() != null) {
            int pm25 = Integer.valueOf(todayWeather.getPm25());
            if (pm25 <= 50) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            } else if (pm25 <= 100) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            } else if (pm25 <= 150) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            } else if (pm25 <= 200) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            } else if (pm25 <= 300) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            } else {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
            }
        }

        //根据解析的天气类型更新界面的天气图案
        String climate = todayWeather.getType();
        if (climate.equals("暴雪")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
        } else if (climate.equals("暴雨")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
        } else if (climate.equals("大暴雨")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
        } else if (climate.equals("大雪")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
        } else if (climate.equals("大雨")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
        } else if (climate.equals("多云")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
        } else if (climate.equals("雷阵雨")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
        } else if (climate.equals("雷阵雨冰雹")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
        } else if (climate.equals("晴")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        } else if (climate.equals("沙尘暴")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
        } else if (climate.equals("特大暴雨")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
        } else if (climate.equals("雾")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
        } else if (climate.equals("小雪")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
        } else if (climate.equals("小雨")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
        } else if (climate.equals("阴")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
        } else if (climate.equals("雨夹雪")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
        } else if (climate.equals("阵雨")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
        } else if (climate.equals("阵雪")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
        } else if (climate.equals("中雪")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
        } else if (climate.equals("中雨")) {
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        }
        Toast.makeText(MainActivity.this, "更新成功!", Toast.LENGTH_LONG).show();

    }

    /**
     * 定位监听器，处理定位后信息
     */
    private class MyLocationListener extends BDAbstractLocationListener  {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            String city = bdLocation.getCity();
            String province = bdLocation.getProvince();
            city = city.substring(0, city.length() - 1);
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);

            String cityCode = null;
            for (City c : mCityList) {
                if (c.getCity().equals(city)) {
                    cityCode = c.getNumber();
                    Log.d("Location: ", city + "  " + province + "  " + cityCode);
                    break;
                }
            }

            sharedPreferences.edit().putString(DEFAULT_CITY_CODE, cityCode).apply();    // 存储当前默认 citycode

            mUpdateBtn.setClickable(false); // 后面即将进行更新天气数据，因此使更新操作按钮不可用
            queryWeatherCode(cityCode);     // 依据 citycode 查询天气
            mLocationBtn.clearAnimation();
            mLocationClient.stop();         // 停止定位旋转动画
            mLocationBtn.setClickable(true);
        }
    }
}
