package cn.carhouse.imoocproject.video;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cn.carhouse.imoocproject.R;
import cn.carhouse.video.CustomVideoView;

public class VideoActivity extends AppCompatActivity {
    private CustomVideoView mVideoView;
    String mUrl = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setUrl(mUrl);
        // mVideoView.setAutoPlay(true);
    }
}
