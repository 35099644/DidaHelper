package com.example.shaoxiaofei.didahelper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shaoxiaofei.didahelper.util.QueryServiceTask;
import com.example.shaoxiaofei.didahelper.util.SPUtil;
import com.example.shaoxiaofei.didahelper.util.UIUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnTouchListener, QueryServiceTask.QueryInterface,NumberPicker.OnValueChangeListener {

    private final static String TAG = "MainActivity";

    private final static String X_KEY = "startX", Y_KEY = "startY";

    private TextView serviceTextView, realtimeTextView, bookTextView, airportTextView;

    private Switch serviceSwitch, realtimeSwitch, bookSwitch, airportSwitch;

    private NumberPicker realtimeConsumerPicker, realtimeWholePicker,bookConsumerPicker,bookWholePicker;

    private boolean isRealtimeOn, isBookOn, isAirportOn;

    private boolean isFromUser = false;

    private Button statusButton;

    private WindowManager windowManager;

    private boolean isShow = false;

    private float startX = 0, startY = 0;

    private final static int canMoveDistance = 10;

    private WindowManager.LayoutParams layoutParams;

    private boolean isServiceOn = false;

    private QueryServiceTask queryServiceTask;

    private MenuItem confirmItem;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        windowManager = getWindowManager();
        queryServiceTask = new QueryServiceTask(this, this);
        initView();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
        removeStatusView();
        queryServiceTask.stopLoop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SPUtil.saveField(this, X_KEY, startX);
        SPUtil.saveField(this, Y_KEY, startY);
        addStatusView();
        queryServiceTask.startLoop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeStatusView();
        queryServiceTask.stopLoop();
        queryServiceTask.shutdown();
    }

    private void initView() {
        serviceTextView = findViewById(R.id.tv_service);
        realtimeTextView = findViewById(R.id.tv_realtime);
        bookTextView = findViewById(R.id.tv_book);
        airportTextView = findViewById(R.id.tv_airport);
        statusButton = new Button(this);
        statusButton.setBackgroundResource(R.drawable.shape_circle_service_on);
        statusButton.setText(getString(R.string.service_off_tint));
        layoutParams = new WindowManager.LayoutParams();

        statusButton.setOnTouchListener(new View.OnTouchListener() {
            boolean hasDragged;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hasDragged = false;
                        startX = event.getRawX();
                        startY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        hasDragged = true;
                        layoutParams.x += event.getRawX() - startX;
                        layoutParams.y += event.getRawY() - startY;
                        windowManager.updateViewLayout(v, layoutParams);
                        startX = event.getRawX();
                        startY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        startX = layoutParams.x;
                        startY = layoutParams.y;
                        if (!hasDragged)
                        v.performClick();
                        break;
                }
                return true;
            }
        });

        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShow && !isServiceOn) {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }
            }
        });


        serviceSwitch = findViewById(R.id.sw_service);
        realtimeSwitch = findViewById(R.id.sw_realtime);
        bookSwitch = findViewById(R.id.sw_book);
        airportSwitch = findViewById(R.id.sw_airport);

        realtimeConsumerPicker = findViewById(R.id.np_distance_consumer_realtime);
        realtimeWholePicker = findViewById(R.id.np_distance_whole_realtime);
        bookConsumerPicker = findViewById(R.id.np_distance_consumer_book);
        bookWholePicker = findViewById(R.id.np_distance_whole_book);

        realtimeConsumerPicker.setMinValue(1);
        realtimeConsumerPicker.setMaxValue(10);
        realtimeConsumerPicker.setValue(SPUtil.getIntField(this,getString(R.string.consumer_distance_realtime)));
        realtimeWholePicker.setMinValue(1);
        realtimeWholePicker.setMaxValue(50);
        realtimeWholePicker.setValue(SPUtil.getIntField(this,getString(R.string.whole_distance_realtime)));
        bookConsumerPicker.setMinValue(1);
        bookConsumerPicker.setMaxValue(10);
        bookConsumerPicker.setValue(SPUtil.getIntField(this,getString(R.string.consumer_distance_book)));
        bookWholePicker.setMinValue(1);
        bookWholePicker.setMaxValue(50);
        bookWholePicker.setValue(SPUtil.getIntField(this,getString(R.string.whole_distance_book)));

        bookWholePicker.setOnValueChangedListener(this);
        bookConsumerPicker.setOnValueChangedListener(this);
        realtimeWholePicker.setOnValueChangedListener(this);
        realtimeWholePicker.setOnValueChangedListener(this);
        isRealtimeOn = SPUtil.getBooleanField(this, getString(R.string.realtime));
        realtimeSwitch.setChecked(isRealtimeOn);
        realtimeTextView.setText(isRealtimeOn?R.string.realtime_on:R.string.realtime_off);
        isBookOn = SPUtil.getBooleanField(this, getString(R.string.airport));
        bookSwitch.setChecked(isBookOn);
        bookTextView.setText(isBookOn?R.string.book_on:R.string.book_off);
        isAirportOn = SPUtil.getBooleanField(this, getString(R.string.book));
        airportSwitch.setChecked(SPUtil.getBooleanField(this, getString(R.string.book)));
        airportTextView.setText(isAirportOn?R.string.airport_on:R.string.airport_off);

        serviceSwitch.setOnCheckedChangeListener(this);
        serviceSwitch.setOnTouchListener(this);
        realtimeSwitch.setOnCheckedChangeListener(this);
        bookSwitch.setOnCheckedChangeListener(this);
        airportSwitch.setOnCheckedChangeListener(this);

    }

    private void updateServiceStatus() {
        isFromUser = false;
        boolean isChecked = getMService();
        serviceSwitch.setChecked(isChecked);
        serviceTextView.setText(getString(isChecked ? R.string.service_on : R.string.service_off));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_service:
                if (isFromUser) {
                    if (isChecked) {
                        serviceTextView.setText(getString(R.string.service_on));
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    } else {
                        serviceTextView.setText(getString(R.string.service_off));
                        sendBroadcast(new Intent(DidaService.STOP_FILTER));
                    }
                }

                break;
            case R.id.sw_realtime:
                isRealtimeOn = isChecked;
                realtimeTextView.setText(getString(isRealtimeOn ? R.string.realtime_on : R.string.realtime_off));
                SPUtil.saveField(this, getString(R.string.realtime), isRealtimeOn);

                break;
            case R.id.sw_book:
                isBookOn = isChecked;
                bookTextView.setText(getString(isBookOn ? R.string.book_on : R.string.book_off));
                SPUtil.saveField(this, getString(R.string.book), isBookOn);
                break;
            case R.id.sw_airport:
                isAirportOn = isChecked;
                airportTextView.setText(getString(isAirportOn ? R.string.airport_on : R.string.airport_off));
                SPUtil.saveField(this, getString(R.string.airport), isAirportOn);
                break;
            default:
                break;
        }
    }


    private boolean getMService() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Toast.makeText(this, "android O 以上不支持服务查询,请手动确认服务开启状态", Toast.LENGTH_SHORT).show();
//            return false;
//        }
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(1000);


        Log.e(TAG, "getMService count : " + rs.size());
        for (ActivityManager.RunningServiceInfo rsi : rs) {
            Log.e(TAG, "getMService name : " + rsi.service.getPackageName());
            if (rsi.service.getShortClassName().contains(DidaService.class.getSimpleName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.sw_service:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isFromUser = true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        confirmItem = menu.findItem(R.id.ac_main_menu_confirm);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ac_main_menu_join:
                showJoinDialog();
                break;
            case R.id.ac_main_menu_notice:
                showNoticeDialog();
                break;
            case R.id.ac_main_menu_confirm:
                saveDistanceArgs();
                item.setVisible(false);
                break;
            default:
                break;
        }
        return true;
    }

    private void saveDistanceArgs(){
        SPUtil.saveField(this,getString(R.string.consumer_distance_realtime),realtimeConsumerPicker.getValue());
        SPUtil.saveField(this,getString(R.string.consumer_distance_book),bookConsumerPicker.getValue());
        SPUtil.saveField(this,getString(R.string.whole_distance_realtime),realtimeWholePicker.getValue());
        SPUtil.saveField(this,getString(R.string.whole_distance_book),bookWholePicker.getValue());
    }

    private void showJoinDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.join)
                .setMessage(R.string.join_content)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    private void showNoticeDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.notice)
                .setMessage(R.string.notice_content)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    private void addStatusView() {
        if (isShow) return;
        layoutParams.height = UIUtil.dip2px(this, 40);
        layoutParams.width = UIUtil.dip2px(this, 40);
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.gravity = Gravity.NO_GRAVITY;
        layoutParams.x = (int) SPUtil.getFloatField(this, X_KEY);
        layoutParams.y = (int) SPUtil.getFloatField(this, Y_KEY);
        windowManager.addView(statusButton, layoutParams);
        isShow = true;
    }

    private void removeStatusView() {
        if (!isShow) return;
        windowManager.removeView(statusButton);
        isShow = false;
    }


    @Override
    public boolean query() {
        return getMService();
    }

    @Override
    public void onQueryFinish(boolean result) {
        isServiceOn = result;
        if (result) {
            statusButton.setBackgroundResource(R.drawable.shape_circle_service_on);
            statusButton.setText(R.string.service_on_tint);
        } else {
            statusButton.setBackgroundResource(R.drawable.shape_circle_service_off);
            statusButton.setText(R.string.service_off_tint);
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (!confirmItem.isVisible())
        confirmItem.setVisible(true);
    }
    private long firstTime = 0;
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(R.string.alert)
                .setMessage(R.string.leave_away)
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false)
                .create().show();
    }
}
