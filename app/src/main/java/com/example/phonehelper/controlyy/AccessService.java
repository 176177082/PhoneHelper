package com.example.phonehelper.controlyy;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class AccessService extends BaseAccessibilityService {

    private String appPackageName = "cn.xuexi.android";


    private boolean refresh = true; // 控制在未处理完逻辑前不要进入逻辑空间
    private int progress = 0; // 自动化进度

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName() == null ? "" : event.getPackageName().toString();
        Log.d("packageName", packageName);
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> allPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : allPackages) {
            Log.d("packageName", packageInfo.packageName);
        }
        Intent CalculatorIntent = getPackageManager().getLaunchIntentForPackage(appPackageName);
//        CalculatorIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(CalculatorIntent);
        if (CalculatorIntent != null) {
            try {
                Log.d("packageName:计算器为不为空", packageName);
                CalculatorIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(CalculatorIntent);
            } catch (Exception e) {
            }
        } else {
            Log.d("packageName:计算器为为空", packageName);
        }
        if (!packageName.equals(appPackageName)) {
            // 如果活动APP不是目标APP则不响应
            return;
        }


        int eventType = event.getEventType();

        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:// 捕获窗口内容改变事件
                if (packageName.equals(appPackageName)) {
                    if (refresh) {
                        refresh = false;
                        controlYY();
                        refresh = true;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void controlYY() {
        // progress自动化进度，防止顺序出错
        // 步骤1：点击 1
        if (progress == 0) {
            // 直接通过文本查找数字 1
            AccessibilityNodeInfo nodeOne = findViewByText("百灵");

            if (nodeOne != null) {
                Log.d("packageName:找到百灵了", appPackageName);

                performViewClick(nodeOne);
                progress++;
                sleep(500);
            }
        }

        // 步骤3：点击 2
        if (progress == 1) {
            // 通过文本查找所有的 2，并点击

            // 用于获取控件的矩形信息：坐标位置和宽高
            Rect rect = new Rect();

            rect.left = 0;
            rect.right = 1080;
            rect.top = 400;
            rect.bottom = 1007;


            int moveToX = (rect.left + rect.right) / 2;
            int moveToY = (rect.top + rect.bottom) / 2;
            int lineToX = (rect.left + rect.right) / 2;
            int lineToY = (rect.top + rect.bottom) / 2;

            // 有些View是不能点击，这时候可以用手势来处理
            gesture(moveToX, moveToY, lineToX, lineToY, 100L, 400L);
            progress++;
            sleep(500);
        }


        // 更多的操作请看BaseService，或者自行百度
    }
}
