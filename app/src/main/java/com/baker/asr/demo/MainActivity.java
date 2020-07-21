package com.baker.asr.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.baker.asr.demo.permission.PermissionFail;
import com.baker.asr.demo.permission.PermissionSuccess;
import com.baker.asr.demo.permission.PermissionUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //刷新token
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !PermissionUtil.hasPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)) {
            PermissionUtil.needPermission(MainActivity.this, 89, Manifest.permission.RECORD_AUDIO
            );
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.file_speech:
                startActivity(new Intent(MainActivity.this, FileSpeechActivity.class));
                break;
            case R.id.voice_speech:
                startActivity(new Intent(MainActivity.this, VoiceSpeechActivity.class));
                break;
        }
    }

    /**
     * 授权成功
     */
    @PermissionSuccess(requestCode = 89)
    public void camouflageCallSuccess() {
    }

    /**
     * 授权失败
     */
    @PermissionFail(requestCode = 89)
    public void camouflageCallFail() {
        //刷新token
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !PermissionUtil.hasPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)) {
            PermissionUtil.needPermission(MainActivity.this, 89, Manifest.permission.RECORD_AUDIO
            );
        }
    }
}
