package com.yorkyu.weathervision.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yorkyu.weathervision.R;

public class SplashScreenActivity extends AppCompatActivity {

    private Button mBtnJump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mBtnJump = findViewById(R.id.btn_jump);
        startClock();

        mBtnJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toMainActivity();
            }
        });


    }

    private CountDownTimer countDownTimer = new CountDownTimer(3000,1000) {
        @Override
        public void onTick(long l) {
            mBtnJump.setText("跳过广告"+ l/1000 + "s" );
        }

        @Override
        public void onFinish() {
            mBtnJump.setText("跳过广告"+0+"s");
            toMainActivity();
        }
    };

    private void toMainActivity() {
        Intent intent = new Intent(getApplicationContext(),
                MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startClock() {
        mBtnJump.setVisibility(View.VISIBLE);
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 检查 countdowntimer， 当用户提前退出时需要提前取消 timer 任务，防止跳转两次
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
