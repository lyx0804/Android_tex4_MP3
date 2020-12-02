package com.example.mp3;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import static java.lang.Integer.parseInt;

public class Music_Activity extends AppCompatActivity implements View.OnClickListener{
    private static SeekBar sb;
    private static TextView tv_progress,tv_total,name_song;
    private ObjectAnimator animator;
    private MusicService.MusicControl musicControl;
    String name;
    Intent intent1,intent2;
    MyServiceConn conn;
    private boolean isUnbind =false;//记录服务是否被解绑
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        intent1=getIntent();
        init();
    }
    private void init(){
        tv_progress=(TextView)findViewById(R.id.tv_progress);
        tv_total=(TextView)findViewById(R.id.tv_total);
        sb=(SeekBar)findViewById(R.id.sb);
        name_song=(TextView)findViewById(R.id.song_name);

        findViewById(R.id.btn_play).setOnClickListener(this);
        findViewById(R.id.btn_pause).setOnClickListener(this);
        findViewById(R.id.btn_continue_play).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.pre).setOnClickListener(this);

        name=intent1.getStringExtra("name");
        name_song.setText(name);
        intent2=new Intent(this,MusicService.class);//创建意图对象
        conn=new MyServiceConn();//创建服务连接对象
        bindService(intent2,conn,BIND_AUTO_CREATE);//绑定服务
        //为滑动条添加事件监听
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //进度条改变时，会调用此方法
                if (progress==seekBar.getMax()){//当滑动条到末端时，结束动画
                    animator.pause();//停止播放动画
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {//滑动条开始滑动时调用
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {//滑动条停止滑动时调用
                //根据拖动的进度改变音乐播放进度
                int progress=seekBar.getProgress();//获取seekBar的进度
                musicControl.seekTo(progress);//改变播放进度
            }
        });
        ImageView iv_music=(ImageView)findViewById(R.id.iv_music);
        String position= intent1.getStringExtra("position");
        int i=parseInt(position);
        iv_music.setImageResource(frag1.icons[i]);

    }


    public static Handler handler=new Handler(){//创建消息处理器对象
        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg){
            Bundle bundle=msg.getData();//获取从子线程发送过来的音乐播放进度
            int duration=bundle.getInt("duration");
            int currentPosition=bundle.getInt("currentPosition");
            sb.setMax(duration);
            sb.setProgress(currentPosition);
            //歌曲总时长
            int minute=duration/1000/60;
            int second=duration/1000%60;
            String strMinute=null;
            String strSecond=null;
            if(minute<10){//如果歌曲的时间中的分钟小于10
                strMinute="0"+minute;//在分钟的前面加一个0
            }else{
                strMinute=minute+"";
            }
            if (second<10){//如果歌曲中的秒钟小于10
                strSecond="0"+second;//在秒钟前面加一个0
            }else{
                strSecond=second+"";
            }
            tv_total.setText(strMinute+":"+strSecond);
            //歌曲当前播放时长
            minute=currentPosition/1000/60;
            second=currentPosition/1000%60;
            if(minute<10){//如果歌曲的时间中的分钟小于10
                strMinute="0"+minute;//在分钟的前面加一个0
            }else{
                strMinute=minute+" ";
            }
            if (second<10){//如果歌曲中的秒钟小于10
                strSecond="0"+second;//在秒钟前面加一个0
            }else{
                strSecond=second+" ";
            }
            tv_progress.setText(strMinute+":"+strSecond);
        }
    };
    class MyServiceConn implements ServiceConnection{//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(MusicService.MusicControl) service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name){

        }
    }
    private void unbind(boolean isUnbind){
        if(!isUnbind){//判断服务是否被解绑
            musicControl.pausePlay();//暂停播放音乐
            unbindService(conn);//解绑服务
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play://播放按钮点击事件
                String position=intent1.getStringExtra("position");
                int i=parseInt(position);
                musicControl.play(i);
                break;
            case R.id.btn_pause://暂停按钮点击事件
                musicControl.pausePlay();
                break;
            case R.id.btn_continue_play://继续播放按钮点击事件
                musicControl.continuePlay();
                break;
            case R.id.btn_exit://退出按钮点击事件
                unbind(isUnbind);
                isUnbind=true;
                finish();
                break;
            case R.id.next:
                String position2=intent1.getStringExtra("position");
                int i2=parseInt(position2);
                musicControl.nextmusic(i2);
                ImageView iv_music=(ImageView)findViewById(R.id.iv_music);
                iv_music.setImageResource(frag1.icons[i2+1]);
                name_song=(TextView)findViewById(R.id.song_name);
                name_song.setText(frag1.name[i2+1]);
                break;
            case R.id.pre:
                String position3=intent1.getStringExtra("position");
                int i3=parseInt(position3);
                musicControl.premusic(i3);
                ImageView iv_music3=(ImageView)findViewById(R.id.iv_music);
                iv_music3.setImageResource(frag1.icons[i3-1]);
                name_song=(TextView)findViewById(R.id.song_name);
                name_song.setText(frag1.name[i3-1]);
                break;
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbind(isUnbind);//解绑服务
    }
}


