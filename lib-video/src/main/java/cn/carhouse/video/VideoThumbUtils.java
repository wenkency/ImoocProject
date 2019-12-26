package cn.carhouse.video;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.collection.ArrayMap;

import java.util.HashMap;
import java.util.Map;

import cn.carhouse.utils.ThreadUtils;

/**
 * 加载缩略图片
 */
public class VideoThumbUtils {
    private static VideoThumbUtils instance = new VideoThumbUtils();
    private Map<String, Bitmap> maps = new ArrayMap<>();

    public static VideoThumbUtils getInstance() {
        return instance;
    }

    private VideoThumbUtils() {
    }

    /**
     * 获取视频文件截图
     *
     * @param path 视频文件的路径
     */
    public void getVideoThumb(final ImageView imageView, final String path) {
        if (imageView == null || TextUtils.isEmpty(path)) {
            return;
        }
        // 从缓存取
        Bitmap bitmap = maps.get(path);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        ThreadUtils.getNormalPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    MediaMetadataRetriever media = new MediaMetadataRetriever();
                    media.setDataSource(path, new HashMap<String, String>());
                    final Bitmap bitmap = media.getFrameAtTime();
                    if (bitmap == null) {
                        return;
                    }
                    maps.put(path, bitmap);
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }catch (Throwable e){
                    e.printStackTrace();
                }

            }
        });
    }
}
