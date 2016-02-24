package kr.co.bit.osf.flashcard;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDTO;
import kr.co.bit.osf.flashcard.debug.Dlog;

// http://gun0912.tistory.com/12
public class BaseApplication extends Application {
    public static boolean DEBUG = false;

    @Override
    public void onCreate() {
        super.onCreate();
        this.DEBUG = isDebuggable(this);

        // db initialize
        FlashCardDB db = new FlashCardDB(this);
        StateDTO state = db.getState();
        if (state == null) {
            Dlog.i("db initialize");
            db.initialize();
            state = db.getState();
            Dlog.i("state:" + state);
        } else {
            Dlog.i("db already initialized");
        }
        // setup test state
        //SetupInitialState.setupState(db);
    }

    /**
     * get Debug Mode
     *
     * @param context
     * @return
     */
    private boolean isDebuggable(Context context) {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /* debuggable variable will remain false */
        }

        return debuggable;
    }
}
