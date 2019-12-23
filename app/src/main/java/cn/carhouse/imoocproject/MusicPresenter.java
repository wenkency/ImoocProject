package cn.carhouse.imoocproject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import cn.carhouse.audio.bean.AudioBean;
import cn.carhouse.utils.ThreadUtils;

/**
 * ================================================================
 * 版权: 爱车小屋所有（C） 2017
 * <p>
 * 作者：刘付文 （61128910@qq.com）
 * <p>
 * 时间: 2017-02-21 18:04
 * <p>
 * 描述：
 * ================================================================
 */
public class MusicPresenter {
    public static final int FILTER_SIZE = 100 * 1024;// 100K
    public static final int FILTER_DURATION = 1 * 30 * 1000;// 秒
    private static final HashMap<Long, Bitmap> sArtCache = new HashMap<Long, Bitmap>();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();

    /**
     * 获取本地音乐
     */
    public static Future<?> getLocalMusic(final Context context, final OnLoadMusicListener listener) {

        return ThreadUtils.getNormalPool().submit(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, null);
                List<AudioBean> list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                    if (isMusic == 0) {
                        continue;
                    }
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    if (size < FILTER_SIZE || duration < FILTER_DURATION) {
                        continue;
                    }

                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    AudioBean info = new AudioBean();
                    info.setId(id + "");
                    info.setAlbum(album);
                    info.setUrl(path);
                    info.setName(title);
                    info.setTotalTime(duration + "");
                    info.setAlbumInfo(albumId + "");
                    if (list.contains(info)) {
                        continue;
                    }
                    list.add(info);
                }
                listener.onLoadCompleted(list);
            }
        });

    }

//    /**
//     * 获取SD卡音乐
//     */
//    private static Observable<List<MusicInfo>> getSDMusic(final Context activity) {
//
//        return Observable.create(new Observable.OnSubscribe<List<MusicInfo>>() {
//            @Override
//            public void call(Subscriber<? super List<MusicInfo>> subscriber) {
//                if (!subscriber.isUnsubscribed()) {
//                    try {
//                        List<MusicInfo> list = new ArrayList<>();
//                        ContentResolver resolver = activity.getContentResolver();
//                        Uri uri = Uri.parse("content://media/external/file");
//                        //Cursor cursor = resolver.query(uri, null,null,null,null);
//                        Cursor cursor = resolver.query(uri, null,
//                                MediaStore.Files.FileColumns.DATA + " like ? ",
//                                new String[]{"wa"},
//                                null);
//                        /*Cursor cursor = resolver.query(uri, null,
//                                MediaStore.Files.FileColumns.DATA + " like ?  or " + MediaStore.Files.FileColumns.DATA + " like ? or " +
//                                        MediaStore.Files.FileColumns.DATA + " like ?  or "+
//                                        MediaStore.Files.FileColumns.DATA + " like ?  ",
//                                new String[]{"%.iso", "%.dsf", "%.dff", "%.ape"},
//                                null);*/
//                        readData(list, cursor, true);
//                        subscriber.onNext(list);
//                        cursor.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                subscriber.onCompleted();
//            }
//        });
//    }
//
//    /**
//     * 读取数据
//     */
//    private static void readData(List<AudioBean> list, Cursor cursor, boolean isSD) {
//        while (cursor.moveToNext()) {
//            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
//            if (isMusic == 0 && !isSD) {
//                continue;
//            }
//            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
//            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
//            if (size < 1024 * 1024) continue;
//            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
//            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
//            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
//            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
//            int albumid = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//            MusicInfo info = new MusicInfo();
//            info.albumId = albumid;
//            info.artist = artist;
//            info.data = path;
//            info._id = id;
//            info.musicName = title;
//            info.duration = (int) duration;
//            info.album = album;
//
//            if(list.contains(info)){
//                continue;
//            }
//            list.add(info);
//        }
//    }


    public interface OnLoadMusicListener {
        void onLoadCompleted(List<AudioBean> list);
    }


    /**
     * 获取专辑图片
     */
    public static Bitmap getAlbumPicPath(final String filePath) {
        Bitmap bitmap = null;
        //能够获取多媒体文件元数据的类
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath); //设置数据源
            byte[] embedPic = retriever.getEmbeddedPicture(); //得到字节型数据
            bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length); //转换为图片
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return bitmap;
    }


    /**
     * 获取专辑图片
     */
    public static Bitmap getCachedArtwork(Context context, long artIndex, Bitmap defaultArtwork) {
        Bitmap bitmap = null;
        synchronized (sArtCache) {
            bitmap = sArtCache.get(artIndex);
        }
        if (context == null) {
            return null;
        }
        if (bitmap == null) {
            bitmap = defaultArtwork;
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Bitmap b = getArtworkQuick(context, artIndex, w, h);
            if (b != null) {
                bitmap = b;
                synchronized (sArtCache) {
                    // the cache may have changed since we checked
                    Bitmap value = sArtCache.get(artIndex);
                    if (value == null) {
                        sArtCache.put(artIndex, bitmap);
                    } else {
                        bitmap = value;
                    }
                }
            }
        }
        return bitmap;
    }

    public static Bitmap getArtworkQuick(Context context, long album_id, int w, int h) {
        w -= 1;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),
                        null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth > w && nextHeight > h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }
                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    if (sBitmapOptionsCache.outWidth != w
                            || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        if (tmp != b)
                            b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

}