package com.example.shaoxiaofei.didahelper.util;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;


public class QueryServiceTask {

    private final static int MSG_WHAT = 0x123;
    private final static int MSG_DELAY = 1500;

    private HandlerThread handlerThread;
    private Handler handler;
    private boolean isWorking = false;

    public QueryServiceTask(final QueryInterface queryInterface, final Activity activity) {
        handlerThread = new HandlerThread("QueryServiceTask");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (queryInterface != null) {
                    final boolean result = queryInterface.query();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            queryInterface.onQueryFinish(result);
                        }
                    });
                    if (isWorking)
                    handler.sendEmptyMessageDelayed(MSG_WHAT, MSG_DELAY);
                }

                return false;
            }
        });
    }

    public void startLoop() {
        if (isWorking) return;
        isWorking = true;
        handler.sendEmptyMessage(MSG_WHAT);
    }

    public void stopLoop() {
        if (isWorking) return;
        isWorking = false;
        handler.removeMessages(MSG_WHAT);
    }

    public void shutdown() {
        handlerThread.quit();
    }


    public interface QueryInterface {
        boolean query();

        void onQueryFinish(boolean result);
    }
}
