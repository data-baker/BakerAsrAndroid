package com.baker.asr.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.baker.asr.demo.permission.PermissionUtil;
import com.baker.speech.asr.BakerRecognizer;
import com.baker.speech.asr.basic.BakerRecognizerCallback;

import java.util.List;

/**
 * @author hsj55
 */
public class VoiceSpeechActivity extends AppCompatActivity implements BakerRecognizerCallback, View.OnClickListener {
    private final String clientId = "your clientId";
    private final String clientSecret = "your client secret";
    private BakerRecognizer bakerRecognizer;
    private TextView resultTv, statusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_speech);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !PermissionUtil.hasPermission(VoiceSpeechActivity.this, Manifest.permission.RECORD_AUDIO)) {
            PermissionUtil.needPermission(VoiceSpeechActivity.this, 89, Manifest.permission.RECORD_AUDIO
            );
        }

        resultTv = findViewById(R.id.tv_Result);
        resultTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        statusTv = findViewById(R.id.tv_Status);
        statusTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        bakerRecognizer = BakerRecognizer.getInstance(VoiceSpeechActivity.this, clientId, clientSecret);
//        bakerRecognizer = BakerRecognizer.getInstance(VoiceSpeechActivity.this);
//        bakerRecognizer.setDebug(VoiceSpeechActivity.this);
    }

    @Override
    public void onReadyOfSpeech() {
        statusTv.setText("");
        resultTv.setText("");
        appendStatus("\n麦克风已经准备好");
//        HLogger.d("--onReadyOfSpeech--");
    }

    @Override
    public void onVolumeChanged(float volume, byte[] data) {
//        HLogger.d("--onVolumeChanged--" + volume);
    }

    @Override
    public void onResult(List<String> nbest, List<String> uncertain, boolean isLast) {
//        HLogger.d("--onResult--");
        if (nbest != null && nbest.size() > 0) {
            appendResult(nbest.get(0));
        }
    }

    @Override
    public void onBeginOfSpeech() {
        appendStatus("\n识别开始");
//        HLogger.d("--onBeginOfSpeech--");
//        WriteLog.writeLogs("识别开始");
    }

    @Override
    public void onEndOfSpeech() {
//        WriteLog.writeLogs("识别结束");
        appendStatus("\n识别结束");
//        HLogger.d("--onEndOfSpeech--");
    }

    @Override
    public void onError(int code, String message) {
//        WriteLog.writeLogs("识别错误 : " + code + ", " + message);
        appendStatus("\n识别错误 : " + code + ", " + message);
//        HLogger.d("code=" + code + ", message=" + message);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startRecognize:
                if (bakerRecognizer != null) {
                    setParams();
                    //返回0启动成功，返回1=callback为空，未启动成功
                    int result = bakerRecognizer.startRecognize();
                }
                break;
            case R.id.stopRecognize:
                break;
            default:
                break;
        }
    }

    private void setParams() {
        //******************************  必要设置  ***********************
        //设置回调
        bakerRecognizer.setCallback(this);

        //******************************  可选设置  ***********************
        //（仅）私有化部署，请设置服务器URL
//        bakerRecognizer.setUrl("ws://192.168.1.21:9007"); //测试环境公有云
//        bakerRecognizer.setUrl("ws://192.168.65.180:19002"); //测试环境私有云
//        bakerRecognizer.setUrl("wss://asr.data-baker.com"); //测试环境私有云

        //设置采样率 目前支持16000  默认16000
        bakerRecognizer.setSample(16000);
        //是否添加标点 true=返回标点，false=不返回标点，默认false
        bakerRecognizer.addPct(false);
        //是否执行归一化处理,true=需要处理，false=不需要处理，默认=false。
        bakerRecognizer.enableItn(false);
        //识别类型  0：一句话识别，sdk做vad   1：长语音识别，服务端做vad  默认为0
        bakerRecognizer.setRecognizeType(0);
        //设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        bakerRecognizer.setVadSos(110);
        //设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        bakerRecognizer.setVadEos(50);
        //设置语音最大识别时长，最长60s=1800
        bakerRecognizer.setVadWait(1800);

        //设置语句间停顿间隔，默认45
        bakerRecognizer.setVadPause(45);
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
