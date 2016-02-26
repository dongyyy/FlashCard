package kr.co.bit.osf.flashcard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.debug.Dlog;

/**
 * Created by yeon on 2/25/2016.
 */
public class SplashActivity extends Activity{
    Handler h;//핸들러 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //인트로화면이므로 타이틀바를 없앤다
        setContentView(R.layout.activity_splash);

        boolean isShowSplash = false;
        try {
            // read state from db
            FlashCardDB db = new FlashCardDB(this);
            if (db.getState().getBoxId() == -1) {
                isShowSplash = true;
                Dlog.i("is show splash:" + isShowSplash);
            }
        } catch (Exception e) {
            Dlog.e(e.toString());
        }
        if (isShowSplash) {
            h = new Handler(); //딜래이를 주기 위해 핸들러 생성

            Runnable mrun = new Runnable(){
                @Override
                public void run(){
                    Intent i = new Intent(SplashActivity.this, BoxListActivity.class); //인텐트 생성(현 액티비티, 새로 실행할 액티비티)
                    startActivityForResult(i, 1);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    //overridePendingTransition 이란 함수를 이용하여 fade in,out 효과를줌. 순서가 중요
                }
            };

            h.postDelayed(mrun, 1000); // 딜레이 ( 런어블 객체는 mrun, 시간 2초)
        } else {
            Intent i = new Intent(SplashActivity.this, BoxListActivity.class);
            startActivityForResult(i, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dlog.i("");
        finish();
    }

    //인트로 중에 뒤로가기를 누를 경우 핸들러를 끊어버려 아무일 없게 만드는 부분
    //미 설정시 인트로 중 뒤로가기를 누르면 인트로 후에 홈화면이 나옴.
    @Override
    public void onBackPressed(){
        Dlog.i("");
        finish();
    }

    @Override
    public void finish() {
        // save current state
        FlashCardDB db = new FlashCardDB(this);
        if (db != null) {
            Dlog.i("");
            db.updateState(0 ,0);
        }
        super.finish();
    }
}
