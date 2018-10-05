package com.example.shaoxiaofei.didahelper;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.example.shaoxiaofei.didahelper.util.AccessInfoUtil;
import com.example.shaoxiaofei.didahelper.util.SPUtil;

import java.util.HashMap;
import java.util.Map;

public class DidaService extends AccessibilityService {

    private static final String TAG = DidaService.class.getSimpleName();
    public static final String STOP_FILTER = "com.example.shaoxiaofei.didahelper.stop";
    private StopServiceReceiver receiver;

    public final static String RESULT_NOT_REGISTER = "-3";
    public final static String RESULT_QUEST_ERROR = "-2";
    public final static String RESULT_USER_FULL = "-1";
    public final static String RESULT_FIRST = "0";
    public final static String RESULT_SECOND = "1";
    public final static String RESULT_THIRD = "2";


    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ");
        super.onCreate();
        receiver = new StopServiceReceiver();
        registerReceiver(receiver, new IntentFilter(STOP_FILTER));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent: " + event.getEventType());
//        switch (event.getEventType()){
//            case AccessibilityEvent.TYPE_FO
//        }
        Map<String, AccessibilityNodeInfo> targetNodes = initTagMap();
        AccessibilityNodeInfo root = getRootInActiveWindow();
        AccessInfoUtil.printPacketInfo(root);
        boolean findResult = AccessInfoUtil.findNodeByViewTags(root, targetNodes);

        if (findResult) {
            for (String tag : targetNodes.keySet()) {
                Log.e(TAG, "onAccessibilityEvent key:" + tag + "  value: " + targetNodes.get(tag).getText().toString());
            }
            if (shouldClick(targetNodes))
                targetNodes.get(AccessInfoUtil.NODE_TARGET).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Toast.makeText(this, "智能抢单成功" + shouldClick(targetNodes), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onAccessibilityEvent: auto finish ");
        } else {
            Log.d(TAG, "onAccessibilityEvent: target id is null");
        }

    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        unregisterReceiver(receiver);
    }

    private Map<String, AccessibilityNodeInfo> initTagMap() {
        Map<String, AccessibilityNodeInfo> map = new HashMap<>();
        map.put(AccessInfoUtil.NODE_TITLE, null);
        map.put(AccessInfoUtil.NODE_DISTANCE_CONSUMER, null);
        map.put(AccessInfoUtil.NODE_TARGET, null);
        map.put(AccessInfoUtil.NODE_DISTANCE_WHOLE, null);
        return map;
    }

    private class StopServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                Toast.makeText(DidaService.this, "当前系统不支持自动关闭服务，请升级到android N以上", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } else {
                disableSelf();
                Toast.makeText(DidaService.this, "关闭抢单服务", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean shouldClick(Map<String, AccessibilityNodeInfo> nodes) {
        String topic = nodes.get(AccessInfoUtil.NODE_TITLE).getText().toString();
        if (topic.equals(getString(R.string.realtime)) && SPUtil.getBooleanField(this, getString(R.string.realtime))) {
            return Integer.parseInt(nodes.get(AccessInfoUtil.NODE_DISTANCE_WHOLE).getText().toString()) >= SPUtil.getIntField(this, getString(R.string.whole_distance_realtime))
                    && Integer.parseInt(nodes.get(AccessInfoUtil.NODE_DISTANCE_CONSUMER).getText().toString()) <= SPUtil.getIntField(this, getString(R.string.consumer_distance_realtime));
        } else if (topic.equals(getString(R.string.book)) && SPUtil.getBooleanField(this, getString(R.string.book))) {
            Log.e(TAG, "shouldClick: " + nodes.get(AccessInfoUtil.NODE_DISTANCE_WHOLE).getText().toString() + " :" +
                    SPUtil.getIntField(this, getString(R.string.whole_distance_book) + " :" +
                            Integer.parseInt(nodes.get(AccessInfoUtil.NODE_DISTANCE_CONSUMER).getText().toString()) + " :" +
                            SPUtil.getIntField(this, getString(R.string.consumer_distance_book))
                    ));
            return Integer.parseInt(nodes.get(AccessInfoUtil.NODE_DISTANCE_WHOLE).getText().toString()) >= SPUtil.getIntField(this, getString(R.string.whole_distance_book))
                    && Integer.parseInt(nodes.get(AccessInfoUtil.NODE_DISTANCE_CONSUMER).getText().toString()) <= SPUtil.getIntField(this, getString(R.string.consumer_distance_book));
        }
        return false;
    }
}
