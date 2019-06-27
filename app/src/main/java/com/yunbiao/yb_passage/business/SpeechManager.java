package com.yunbiao.yb_passage.business;

import android.media.AudioManager;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.yunbiao.yb_passage.APP;
import com.yunbiao.yb_passage.utils.ThreadUitls;

public class SpeechManager {
    private static final String TAG = "SpeechManager";
    private static SpeechManager speechManager = new SpeechManager();
    private SpeechSynthesizer synthesizer;
    private String welcom = "您好 ";

    public static SpeechManager instance(){
        return speechManager;
    }

    public void init(){
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                synthesizer = SpeechSynthesizer.getInstance();
                synthesizer.setContext(APP.getContext());
                synthesizer.setAppId("16559942");
                synthesizer.setApiKey("BC1rnoS8G2TGI0oUvfZuI9tN","blTHPT4Agkxed43oupqsxfRq6vfjkMDk");
                synthesizer.auth(TtsMode.MIX); // 离在线混合
                synthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0"); // 设置发声的人声音，在线生效
                synthesizer.initTts(TtsMode.MIX); // 初始化离在线混合模式，如果只需要在线合成功能，使用 TtsMode.ONLINE
                synthesizer.setAudioStreamType(AudioManager.STREAM_ALARM);
                LoggerProxy.printable(true);
            }
        });
    }

    public void speak(String msg){
        if(synthesizer != null){
            synthesizer.speak(welcom + msg,msg);
        }
    }

    public void resume(){
        if(synthesizer != null){
            synthesizer.resume();
        }
    }

    public void pause(){
        if(synthesizer != null){
            synthesizer.pause();
        }
    }

    public void stop(){
        if(synthesizer != null){
            synthesizer.stop();
        }
    }

    public void destory(){
        if(synthesizer != null){
            synthesizer.release();
        }
    }
}
