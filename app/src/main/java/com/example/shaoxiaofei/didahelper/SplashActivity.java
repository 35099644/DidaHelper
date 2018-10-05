package com.example.shaoxiaofei.didahelper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.shaoxiaofei.didahelper.bean.Greeting;
import com.example.shaoxiaofei.didahelper.bean.User;
import com.example.shaoxiaofei.didahelper.util.RetrofitHelper;
import com.example.shaoxiaofei.didahelper.util.SPUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = SplashActivity.class.getSimpleName();
    private MaterialEditText editText;
    private Button button;
    private RetrofitHelper retrofitHelper;
    private TelephonyManager mTm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        editText = findViewById(R.id.et_phone);
        String phoneNumber = SPUtil.getStringField(this,getString(R.string.phonenumber));
        editText.setText(phoneNumber);
        button = findViewById(R.id.bt_login);
        button.setOnClickListener(this);
        retrofitHelper = new RetrofitHelper();
        mTm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
            if (!Settings.canDrawOverlays(this)){
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),101);
            }

        }

    }

    @Override
    public void onClick(View v) {
        String phone = editText.getText().toString().trim();
        if (phone.length() != 11) {
            Toast.makeText(this, "手机号长度非法", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
        }
        String immei = mTm.getDeviceId();
        doLogin(phone, immei);
    }

    private void doLogin(final String phone, String immei) {
        User user = new User(phone, immei);
        retrofitHelper.postEntity(user, new Callback<Greeting>() {
            @Override
            public void onResponse(Call<Greeting> call, Response<Greeting> response) {
                if (response.body() ==null) return;
                String resultCode = response.body().getGreeting();
                switch (resultCode) {
                    case DidaService.RESULT_QUEST_ERROR:
                        Toast.makeText(SplashActivity.this, "请检查手机号是否正确", Toast.LENGTH_SHORT).show();
                        break;
                    case DidaService.RESULT_USER_FULL:
                        Toast.makeText(SplashActivity.this, "三个名额已用尽", Toast.LENGTH_SHORT).show();
                        break;
                    case DidaService.RESULT_FIRST:
                    case DidaService.RESULT_SECOND:
                    case DidaService.RESULT_THIRD:
                        Toast.makeText(SplashActivity.this, "第"+(Integer.parseInt(resultCode)+1)+"个用户", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
                        SPUtil.saveField(SplashActivity.this,getString(R.string.phonenumber),phone);
                        finish();
                        break;
                    case DidaService.RESULT_NOT_REGISTER:
                        Toast.makeText(SplashActivity.this, "该手机号尚未激活，请联系管理员", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(SplashActivity.this, "请求非法", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(Call<Greeting> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(SplashActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "阻止读取immei，您的身份认证将会失败,请打开该权限", Toast.LENGTH_LONG).show();
            }
        } else {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "阻止读取immei，您的身份认证将会失败,请打开该权限", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==0 && Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            Log.d(TAG, "onActivityResult: "+resultCode);
            if (!Settings.canDrawOverlays(this)){
                Toast.makeText(this,"无浮窗权限，服务的状态显示将会异常",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
