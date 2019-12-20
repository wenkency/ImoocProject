package cn.carhouse.audio.app;

import android.content.Context;

public class AudioHelper {
    private static Context mContext;

    public static void init(Context context) {
        AudioHelper.mContext = context.getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
