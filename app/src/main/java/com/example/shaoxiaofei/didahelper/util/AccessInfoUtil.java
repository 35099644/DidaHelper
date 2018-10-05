package com.example.shaoxiaofei.didahelper.util;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Arrays;
import java.util.Map;

public class AccessInfoUtil {


    private static final String TAG = AccessInfoUtil.class.getSimpleName();

    private static int tabcount = -1;
    private static StringBuilder sb;

    public final static String NODE_TITLE = "实时/预约";
    public final static String NODE_DISTANCE_CONSUMER = "1";
    public final static String NODE_DISTANCE_WHOLE = "2";
    public final static String NODE_TARGET = "抢单";

    public static void printPacketInfo(AccessibilityNodeInfo root) {
        sb = new StringBuilder();
        tabcount = 0;
        int[] is = {};
        analysisPacketInfo(root, is);
        Log.e(TAG, sb.toString());
    }

    //打印此时的界面状况,便于分析
    private static void analysisPacketInfo(AccessibilityNodeInfo info, int... ints) {
        if (info == null) {
            return;
        }
        if (tabcount > 0) {
            for (int i = 0; i < tabcount; i++) {
                sb.append("\t\t");
            }
        }
        if (ints != null && ints.length > 0) {
            StringBuilder s = new StringBuilder();
            for (int j = 0; j < ints.length; j++) {
                s.append(ints[j]).append(".");
            }
            sb.append(s).append(" ");
        }

        String name = info.getClassName().toString();
        String[] split = name.split("\\.");
        name = split[split.length - 1];
        if ("TextView".equals(name)) {
            CharSequence text = info.getText();
            sb.append("TextView:").append(text);
        } else if ("Button".equals(name)) {
            CharSequence text = info.getText();
            sb.append("Button:").append(text);
        } else {
            sb.append(name);
        }
        sb.append("\n");

        int count = info.getChildCount();
        if (count > 0) {
            tabcount++;
            int len = ints.length + 1;
            int[] newInts = Arrays.copyOf(ints, len);

            for (int i = 0; i < count; i++) {
                newInts[len - 1] = i;
                analysisPacketInfo(info.getChild(i), newInts);
            }
            tabcount--;
        }
    }

    public static AccessibilityNodeInfo findNodeByViewTag(AccessibilityNodeInfo root, String viewTag) {
        if (root == null) return root;
        Log.d(TAG, "findNodeByViewName: ");
        String name = root.getText() == null ? "" : root.getText().toString();
        if (name.equals(viewTag)) {
            return root;
        } else {

            int count = root.getChildCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    AccessibilityNodeInfo inf = findNodeByViewTag(root.getChild(i), viewTag);
                    if (inf != null) {
                        return inf;
                    }
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public static boolean findNodeByViewTags(AccessibilityNodeInfo root, Map<String, AccessibilityNodeInfo> result) {
        if (root == null) return false;
        Log.d(TAG, "findNodeByViewName: ");

        if (!tryAddNode(root, result)) {
            int count = root.getChildCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    findNodeByViewTags(root.getChild(i), result);
                }
            }
        }
        for (String tag : result.keySet()) {
            if (result.get(tag) == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean tryAddNode(AccessibilityNodeInfo node, Map<String, AccessibilityNodeInfo> result) {
        if (node.getText() == null) return false;
        String name = node.getText().toString();
        for (String tag : result.keySet()) {
            if (tag.contains(name)) {
                result.put(tag, node);
                return true;
            } else {
                try {
                    int parseInt = Integer.parseInt(name);
                    Log.e(TAG, "tryAddNode in result: "+parseInt+ " NODE_DISTANCE_CONSUMER:"+result.get(NODE_DISTANCE_CONSUMER)+" NODE_DISTANCE_WHOLE:"+result.get(NODE_DISTANCE_WHOLE));
                    if (result.get(NODE_DISTANCE_CONSUMER) == null) {
                        result.put(NODE_DISTANCE_CONSUMER, node);
                        Log.e(TAG, "NODE_DISTANCE_CONSUMER node info: "+node.getText().toString());
                        return true;
                    } else if (result.get(NODE_DISTANCE_WHOLE) == null) {
                        result.put(NODE_DISTANCE_WHOLE, node);
                        Log.e(TAG, "NODE_DISTANCE_WHOLE node info: "+node.getText().toString());
                        return true;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
