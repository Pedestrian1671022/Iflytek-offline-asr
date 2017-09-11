package com.example.liuxin.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

public class MainActivity extends AppCompatActivity {
    // 语音识别对象
    private SpeechRecognizer mAsr;
    private Toast mToast;
    // 本地语法文件
    private String mLocalGrammar = null;
    private String mContent;
    private TextView recognize_textView;
    private Button recognize;

    // 函数调用返回值
    private int ret = 0;

    @SuppressLint("ShowToast")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID+"=581c7563");

        recognize_textView = (TextView) findViewById(R.id.re_result);
        recognize = (Button) findViewById(R.id.recognize);

        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        // 初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);


        mLocalGrammar = FucUtil.readFile(this,"call.bnf", "utf-8");
        mContent = new String(mLocalGrammar);
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        //指定引擎类型
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 设置参数
        setParam();
        ret = mAsr.buildGrammar("bnf", mContent, mLocalGrammarListener);
        if (ret != ErrorCode.SUCCESS) {
            mToast.setText("语法构建失败,错误码：" + ret);
            mToast.show();
        }

        recognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognize_textView.setText(null);
                asr();
            }
        });

    }

    public void asr(){
        ret = mAsr.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            mToast.setText("识别失败,错误码: " + ret);
            mToast.show();
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                mToast.setText("初始化失败,错误码："+code);
                mToast.show();
            }
        }
    };

    /**
     * 本地构建语法监听器。
     */
    private GrammarListener mLocalGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if(error == null){
                mToast.setText("语法构建成功：" + grammarId);
                mToast.show();
            }else{
                mToast.setText("语法构建失败,错误码：" + error.getErrorCode());
                mToast.show();
            }
        }
    };
    /**
     * 识别监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            mToast.setText("当前正在说话，音量大小：" + volume);
            mToast.show();
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result) {
                recognize_textView.setText(JsonParser.parseLocalGrammarResult(result.getResultString()));
            } else {
                mToast.setText("recognizer result : null");
                mToast.show();
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            mToast.setText("结束说话");
            mToast.show();
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            mToast.setText("开始说话");
            mToast.show();
        }

        @Override
        public void onError(SpeechError error) {
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

    };

    public void setParam(){
        //设置识别引擎
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //设置返回结果为json格式
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //设置本地识别使用语法id
        mAsr.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call");
        //设置本地识别的门限值
        mAsr.setParameter(SpeechConstant.ASR_THRESHOLD, "30");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/讯飞语音平台/asr.wav");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时释放连接
        mAsr.cancel();
        mAsr.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}