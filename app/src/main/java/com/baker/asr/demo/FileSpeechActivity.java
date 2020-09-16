package com.baker.asr.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.baker.asr.demo.permission.PermissionUtil;
import com.baker.speech.asr.BakerRecognizer;
import com.baker.speech.asr.basic.BakerRecognizerCallback;
import com.baker.speech.asr.utils.HLogger;
import com.baker.speech.asr.utils.WriteLog;
import com.blankj.utilcode.util.UriUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

public class FileSpeechActivity extends AppCompatActivity implements BakerRecognizerCallback, View.OnClickListener {
    public final static String clientId = "";
    public final static String clientSecret = "";
    private BakerRecognizer bakerRecognizer;
    private TextView resultTv, statusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_speech);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !PermissionUtil.hasPermission(FileSpeechActivity.this, Manifest.permission.RECORD_AUDIO)) {
            PermissionUtil.needPermission(FileSpeechActivity.this, 89, Manifest.permission.RECORD_AUDIO
            );
        }

        resultTv = findViewById(R.id.tv_Result);
        resultTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        statusTv = findViewById(R.id.tv_Status);
        statusTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        bakerRecognizer = BakerRecognizer.getInstance(FileSpeechActivity.this, clientId, clientSecret);
        bakerRecognizer.setCallback(this);
        bakerRecognizer.setDebug(FileSpeechActivity.this);
    }

    @Override
    public void onReadyOfSpeech() {
        statusTv.setText("");
        resultTv.setText("");
        appendStatus("\n麦克风已经准备好");
        HLogger.d("--onReadyOfSpeech--");
    }

    @Override
    public void onVolumeChanged(float volume, byte[] data) {
//        HLogger.d("--onVolumeChanged--" + volume);
    }

    @Override
    public void onResult(List<String> nbest, List<String> uncertain, boolean isLast) {
        HLogger.d("--onResult--");
        if (nbest != null && nbest.size() > 0) {
            appendResult(nbest.get(0));
        }
    }

    @Override
    public void onBeginOfSpeech() {
        appendStatus("\n识别开始");
        HLogger.d("--onBeginOfSpeech--");
        WriteLog.writeLogs("识别开始");
    }

    @Override
    public void onEndOfSpeech() {
        WriteLog.writeLogs("识别结束");
        appendStatus("\n识别结束");
        HLogger.d("--onEndOfSpeech--");
//        handler.sendEmptyMessageDelayed(100, 3000);
    }

    @Override
    public void onError(int code, String message) {
        WriteLog.writeLogs("识别错误 : " + code + ", " + message);
        appendStatus("\n识别错误 : " + code + ", " + message);
        HLogger.d("code=" + code + ", message=" + message);
//        handler.sendEmptyMessageDelayed(100, 3000);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            setParams();
            //返回0启动成功，返回1=callback为空，未启动成功
            int result = bakerRecognizer.startRecognize();
            HLogger.d("result==" + result);
        }
    };

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.startRecognize) {
            if (bakerRecognizer != null) {
                setParams();
                //返回0启动成功，返回1=callback为空，未启动成功
                int result = bakerRecognizer.startRecognize();
                HLogger.d("result==" + result);
            }
        }
        else if (id == R.id.stopRecognize) {
            bakerRecognizer.stopRecognition();
        } else if (id == R.id.startRecognizeUseCustomFile) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            //允许多选 长按多选
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            //不限制选取类型
            intent.setType("*/*");
            startActivityForResult(intent, 333);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        log("onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 333:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //当单选选了一个文件后返回
                    if (data.getData() != null) {
                        Uri uri = data.getData();
                        File file = UriUtils.uri2File(uri);
                        log(file.getPath());

                        int result = bakerRecognizer.startRecognize(file.getAbsolutePath());
                        HLogger.d("result==" + result);
                    }
                }
                break;
        }
    }

    private void log(String msg) {
        System.out.println("ASR =>" + msg);
    }


    private final Random random = new Random();

    private void setParams() {
        //设置采样率 目前只支持16000  默认16000
        bakerRecognizer.setSample(16000);

        //是否添加标点 true=返回标点，false=不返回标点，默认false
        bakerRecognizer.addPct(false);
        //是否执行归一化处理
        bakerRecognizer.enableItn(false);
        //设置领域、场景
//        bakerRecognizer.setDomain(1);
        //识别类型  0：一句话识别，sdk做vad   1：长语音识别，服务端做vad  默认为0
        bakerRecognizer.setRecognizeType(0);

        //设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        bakerRecognizer.setVadSos(110);
        //设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        bakerRecognizer.setVadEos(50);
        //设置语音最大识别时长，最长60s=1800
//        int ran = Util.randomNum(random, 6);
//        if (ran == 1) {
//            bakerRecognizer.setVadWait(300);
//        } else if (ran == 2) {
//            bakerRecognizer.setVadWait(600);
//        } else if (ran == 3) {
//            bakerRecognizer.setVadWait(900);
//        } else if (ran == 4) {
//            bakerRecognizer.setVadWait(1200);
//        } else if (ran == 5) {
//            bakerRecognizer.setVadWait(1500);
//        } else {
        bakerRecognizer.setVadWait(1800);
//        }

        //设置语句间停顿间隔，默认45
        bakerRecognizer.setVadPause(45);
        //私有化部署，请设置服务器URL
        bakerRecognizer.setUrl("wss://asr.data-baker.com"); //测试环境公有云
//        bakerRecognizer.setUrl("ws://192.168.1.21:9002"); //测试环境私有云
//        bakerRecognizer.setUrl("ws://106.38.72.202:19004");
    }

    private void appendStatus(final String str) {
        statusTv.post(new Runnable() {
            @Override
            public void run() {
                statusTv.append(str);
                int scrollAmount = statusTv.getLayout().getLineTop(statusTv.getLineCount())
                        - statusTv.getHeight();
                if (scrollAmount > 0)
                    statusTv.scrollTo(0, scrollAmount);
                else
                    statusTv.scrollTo(0, 0);
            }
        });
    }

    private void appendResult(final String str) {
        resultTv.post(new Runnable() {
            @Override
            public void run() {
                resultTv.setText(str);
                int scrollAmount = resultTv.getLayout().getLineTop(resultTv.getLineCount())
                        - resultTv.getHeight();
                if (scrollAmount > 0)
                    resultTv.scrollTo(0, scrollAmount);
                else
                    resultTv.scrollTo(0, 0);
            }
        });
    }
}
