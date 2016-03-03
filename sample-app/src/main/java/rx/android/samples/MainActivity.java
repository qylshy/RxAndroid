package rx.android.samples;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func0;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class MainActivity extends Activity {
    private static final String TAG = "RxAndroidSamples";

    private Handler backgroundHandler;

    private Button firstObservableBtn;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        findViewById(R.id.button_run_scheduler).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onRunSchedulerExampleButtonClicked();
            }
        });

        firstObservableBtn = (Button)findViewById(R.id.first_observable_btn);
        firstObservableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshList();
            }
        });
    }

    private Observable<AppInfo> getApps(){
        return Observable.create(new Observable.OnSubscribe<AppInfo>() {
            @Override
            public void call(Subscriber<? super AppInfo> subscriber) {

                List<PackageInfo> apps = new ArrayList<PackageInfo>();
                PackageManager pManager = getPackageManager();
                //获取手机内所有应用
                List<PackageInfo> paklist = pManager.getInstalledPackages(0);
                for (int i = 0; i < paklist.size(); i++) {
                    PackageInfo pak = (PackageInfo) paklist.get(i);
                    //判断是否为非系统预装的应用程序
                    if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
                        // customs applications
                        apps.add(pak);
                    }
                }

                for (PackageInfo packageInfo : apps){
                    if (subscriber.isUnsubscribed()){
                        return;
                    }
                    subscriber.onNext(new AppInfo(packageInfo.packageName, packageInfo.lastUpdateTime, packageInfo.toString()));
                }

                if (!subscriber.isUnsubscribed()){
                    subscriber.onCompleted();
                }
            }
        });
    }

    private void refreshList(){
        getApps().toSortedList()
                .subscribe(new Subscriber<List<AppInfo>>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError " + e.toString());
                    }

                    @Override
                    public void onNext(List<AppInfo> appInfos) {
                        for (AppInfo appInfo : appInfos){
                            Log.i(TAG, appInfo.toString());
                        }
                    }
                });
    }

    void onRunSchedulerExampleButtonClicked() {
        sampleObservable()
                // Run on a background thread
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override public void onCompleted() {
                        Log.d(TAG, "onCompleted()");
                    }

                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override public void onNext(String string) {
                        Log.d(TAG, "onNext(" + string + ")");
                    }
                });
    }

    static Observable<String> sampleObservable() {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override public Observable<String> call() {
                try {
                    // Do some long running operation
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    throw OnErrorThrowable.from(e);
                }
                return Observable.just("one", "two", "three", "four", "five");
            }
        });
    }

    static class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
        }
    }
}
