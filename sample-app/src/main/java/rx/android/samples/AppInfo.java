package rx.android.samples;

/**
 * Created by qiuyunlong on 16/3/3.
 */
public class AppInfo implements Comparable<Object> {

    long mLastUpdateTime;
    String mName;
    String mIcon;

    public AppInfo(String nName, long lastUpdateTime, String icon) {
        mName = nName;
        mIcon = icon;
        mLastUpdateTime = lastUpdateTime;
    }

    @Override
    public int compareTo(Object another) {
        AppInfo f = (AppInfo)another;
        return getName().compareTo(f.getName());
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "mLastUpdateTime=" + mLastUpdateTime +
                ", mName='" + mName + '\'' +
                ", mIcon='" + mIcon + '\'' +
                '}';
    }
}
