package cn.carhouse.imoocproject.music;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import cn.carhouse.audio.app.AudioHelper;
import cn.carhouse.audio.bean.AudioBean;
import cn.carhouse.audio.state.MediaStatus;
import cn.carhouse.imoocproject.R;
import cn.carhouse.imoocproject.music.utils.MusicService;
import cn.carhouse.imoocproject.video.VideoActivity;

public class MusicActivity extends AppCompatActivity {
    /*
     * data
     */
    private ArrayList<AudioBean> mLists = new ArrayList<>();
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AudioHelper.init(this);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_music);
        view = findViewById(R.id.root_view);
        initData();
    }

    private void initData() {
        mLists.add(new AudioBean("100001", "http://sp-sycdn.kuwo.cn/resource/n2/85/58/433900159.mp3",
                "以你的名字喊我", "周杰伦", "七里香", "电影《不能说的秘密》主题曲,尤其以最美的不是下雨天,是与你一起躲过雨的屋檐最为经典",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1559698076304&di=e6e99aa943b72ef57b97f0be3e0d2446&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fblog%2F201401%2F04%2F20140104170315_XdG38.jpeg",
                "4:30"));
        mLists.add(
                new AudioBean("100002", "http://sq-sycdn.kuwo.cn/resource/n1/98/51/3777061809.mp3", "勇气",
                        "梁静茹", "勇气", "电影《不能说的秘密》主题曲,尤其以最美的不是下雨天,是与你一起躲过雨的屋檐最为经典",
                        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1559698193627&di=711751f16fefddbf4cbf71da7d8e6d66&imgtype=jpg&src=http%3A%2F%2Fimg0.imgtn.bdimg.com%2Fit%2Fu%3D213168965%2C1040740194%26fm%3D214%26gp%3D0.jpg",
                        "4:40"));
        mLists.add(
                new AudioBean("100003", "http://sp-sycdn.kuwo.cn/resource/n2/52/80/2933081485.mp3", "灿烂如你",
                        "汪峰", "春天里", "电影《不能说的秘密》主题曲,尤其以最美的不是下雨天,是与你一起躲过雨的屋檐最为经典",
                        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1559698239736&di=3433a1d95c589e31a36dd7b4c176d13a&imgtype=0&src=http%3A%2F%2Fpic.zdface.com%2Fupload%2F201051814737725.jpg",
                        "3:20"));
        mLists.add(
                new AudioBean("100004", "http://sr-sycdn.kuwo.cn/resource/n2/33/25/2629654819.mp3", "小情歌",
                        "五月天", "小幸运", "电影《不能说的秘密》主题曲,尤其以最美的不是下雨天,是与你一起躲过雨的屋檐最为经典",
                        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1559698289780&di=5146d48002250bf38acfb4c9b4bb6e4e&imgtype=0&src=http%3A%2F%2Fpic.baike.soso.com%2Fp%2F20131220%2Fbki-20131220170401-1254350944.jpg",
                        "2:45"));
        // AudioController.getInstance().setQueue(mLists);

        view.post(new Runnable() {
            @Override
            public void run() {
                MusicService.startMusicService(mLists);
            }
        });


//        XPermission.with(this)
//                .permissions(Permission.STORAGE)
//                .request(new PermissionListenerAdapter() {
//                    @Override
//                    public void onSucceed() {
//                        MusicPresenter.getLocalMusic(MusicActivity.this, new MusicPresenter.OnLoadMusicListener() {
//                            @Override
//                            public void onLoadCompleted(List<AudioBean> list) {
//                                AudioController.getInstance().setQueue(list);
//                                TSUtils.show("加载完成："+list.size());
//                            }
//                        });
//                    }
//                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(MediaStatus status) {
        switch (status) {
            case IDLE:
//                AudioBean nowPlaying = AudioController.getInstance().getNowPlaying();
//                if (nowPlaying != null) {
//                    ImageLoaderFactory.getInstance().displayBlurImage(view,
//                            nowPlaying.getAlbumPic(),
//                            100);
//                }
                break;
        }
    }

    public void openVideo(View view) {
        startActivity(new Intent(this, VideoActivity.class));
    }
}
