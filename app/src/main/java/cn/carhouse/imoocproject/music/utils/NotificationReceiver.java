package cn.carhouse.imoocproject.music.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import cn.carhouse.audio.app.AudioHelper;
import cn.carhouse.audio.core.AudioController;

/**
 * 接收Notification发送的广播
 */
public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_STATUS_BAR =
            AudioHelper.getContext().getPackageName() + ".NOTIFICATION_ACTIONS";
    public static final String EXTRA = "extra";
    public static final String EXTRA_PLAY = "play_pause";
    public static final String EXTRA_NEXT = "play_next";
    public static final String EXTRA_PRE = "play_previous";
    public static final String EXTRA_FAV = "play_favourite";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
        }
        String extra = intent.getStringExtra(EXTRA);
        switch (extra) {
            case EXTRA_PLAY:
                // 处理播放暂停事件,可以封到AudioController中
                AudioController.getInstance().playOrPause();
                break;
            case EXTRA_PRE:
                AudioController.getInstance().previous(); // 不管当前状态，直接播放
                break;
            case EXTRA_NEXT:
                AudioController.getInstance().next();
                break;
            case EXTRA_FAV:
                // AudioController.getInstance().changeFavourite();
                break;
        }
    }
}
