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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.GroupedObservable;

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

        findViewById(R.id.create_observable_from_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                observablefrom();
            }
        });

        findViewById(R.id.observable_just_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppInfo oneAppInfo = getAppList().get(0);
                AppInfo twoAppInfo = getAppList().get(5);
                AppInfo threeAppInfo = getAppList().get(10);
                loadApp(oneAppInfo, twoAppInfo, threeAppInfo);
            }
        });

        findViewById(R.id.observable_defer_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Observable<Integer> deferred = Observable.defer(new Func0<Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call() {
                        return getInt();
                    }
                });
                deferred.subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.i(TAG, "defer " + integer);
                    }
                });
            }
        });

        findViewById(R.id.observable_range_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Observable.range(10, 3)
                        .subscribe(new Observer<Integer>() {
                            @Override
                            public void onCompleted() {
                                Log.i(TAG, "range: onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.i(TAG, "range: onError " + e.toString());
                            }

                            @Override
                            public void onNext(Integer integer) {
                                Log.i(TAG, "range: " + integer);
                            }
                        });
            }
        });

        findViewById(R.id.observable_interval_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Subscription stopMePlease = Observable.interval(3, TimeUnit.SECONDS)
                        .subscribe(new Observer<Long>() {
                            @Override
                            public void onCompleted() {
                                Log.i(TAG, "range: onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.i(TAG, "range: onError " + e.toString());
                            }

                            @Override
                            public void onNext(Long aLong) {
                                Log.i(TAG, "range: onNext " + aLong);
                            }
                        });

            }
        });

        findViewById(R.id.observable_filter_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Observable.from(getAppList())
                        .filter(new Func1<AppInfo, Boolean>() {
                            @Override
                            public Boolean call(AppInfo appInfo) {
                                return appInfo.getName().startsWith("c");
                            }
                        })
                        .map(new Func1<AppInfo, AppInfo>() {
                            @Override
                            public AppInfo call(AppInfo appInfo) {
                                String currName = appInfo.getName();
                                String upperCaseName = currName.toUpperCase();
                                appInfo.mName = upperCaseName;
                                return appInfo;
                            }
                        })
                        .scan(new Func2<AppInfo, AppInfo, AppInfo>() {
                            @Override
                            public AppInfo call(AppInfo appInfo, AppInfo appInfo2) {
                                if(appInfo.getName().length() > appInfo2.getName().length()){
                                    return appInfo;
                                } else {
                                    return appInfo2;
                                }
                            }
                        })
                        .take(10)
                        .repeat(3)
                        .distinct()
//                        .elementAt(1)
                        .subscribe(new Observer<AppInfo>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "filter: onComplted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "filter: onError" +e.toString());
                    }

                    @Override
                    public void onNext(AppInfo appInfo) {
                        Log.i(TAG, "filter：" + appInfo.toString());
                    }
                });
            }
        });

        findViewById(R.id.observable_group_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Observable<GroupedObservable<String, AppInfo>> groupItems = Observable.from(getAppList())
                        .groupBy(new Func1<AppInfo, String>() {
                            @Override
                            public String call(AppInfo appInfo) {
                                SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
                                return formatter.format(new Date(appInfo.mLastUpdateTime));
                            }
                        });

                Observable.concat(groupItems)
                        .subscribe(new Observer<AppInfo>() {
                            @Override
                            public void onCompleted() {
                                Log.i(TAG, "GroupBy: onComplete");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.i(TAG, "GroupBy: " + e.toString());
                            }

                            @Override
                            public void onNext(AppInfo appInfo) {
                                Log.i(TAG, "GroupBy: " + appInfo.toString());
                            }
                        });
            }
        });
    }

    private Observable<Integer> getInt(){
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                if (subscriber.isUnsubscribed()){
                    return;
                }
                Log.i(TAG, "getInt");
                subscriber.onNext(42);
                subscriber.onCompleted();
            }
        });
    }

    private  void loadApp(AppInfo appInfo1, AppInfo appInfo2, AppInfo appInfo3){
        Observable.just(appInfo1, appInfo2, appInfo3)
                .repeat(3)
                .subscribe(new Subscriber<AppInfo>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "just: onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "just: onError " + e.toString());
                    }

                    @Override
                    public void onNext(AppInfo appInfo) {
                        Log.i(TAG, "just: " + appInfo.toString());
                    }
                });
    }

    private List<AppInfo> getAppList(){
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

        List<AppInfo> result = new ArrayList<>();
        for (PackageInfo packageInfo : apps){
            result.add(new AppInfo(packageInfo.packageName, packageInfo.lastUpdateTime, packageInfo.toString()));
        }
        return result;
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

    private void observablefrom(){
        Observable.from(getAppList())
                .subscribe(new Subscriber<AppInfo>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "from :  onCompleted" );
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "from :  onError " + e.toString() );
                    }

                    @Override
                    public void onNext(AppInfo appInfo) {
                        Log.i(TAG, "from :" + appInfo.toString());

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
