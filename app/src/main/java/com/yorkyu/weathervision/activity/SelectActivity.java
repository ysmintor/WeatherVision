package com.yorkyu.weathervision.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.yorkyu.weathervision.R;
import com.yorkyu.weathervision.app.MyApplication;
import com.yorkyu.weathervision.model.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView mBackBtn;
    private static final String TAG = SelectActivity.class.getSimpleName();
    private ListView mListView;
    private List<City> cityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        mBackBtn = findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        initViews();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: " + v.getId());
        switch (v.getId()) {
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode", "101160101");
                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;
        }
    }

    private void initViews() {
        // 为 mBackBtn 设置监听事件
        mBackBtn = findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);


        mListView = findViewById(R.id.title_list);
        MyApplication myApplication = (MyApplication) getApplication();
        cityList = myApplication.getCityList();
        List<Map<String, Object>> listItems = new ArrayList<>();

        for (City city : cityList) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("city_name", city.getCity());
            listItem.put("city_code", city.getNumber());
            listItems.add(listItem);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems,
                R.layout.item_list_city, new String[]{"city_name", "city_code"},
                new int[]{R.id.city_name, R.id.city_code});
        mListView.setAdapter(simpleAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city = cityList.get(position);
                Intent i = new Intent();
                i.putExtra("cityCode", city.getNumber());
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    class MyAdapter extends SimpleAdapter {
        /**
         * Constructor
         *
         * @param context  The context where the View associated with this SimpleAdapter is running
         * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
         *                 Maps contain the data for each row, and should include all the entries specified in
         *                 "from"
         * @param resource Resource identifier of a view layout that defines the views for this list
         *                 item. The layout file should include at least those named views defined in "to"
         * @param from     A list of column names that will be added to the Map associated with each
         *                 item.
         * @param to       The views that should display column in the "from" parameter. These should all be
         *                 TextViews. The first N views in this list are given the values of the first N columns
         */
        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }
    }
}
