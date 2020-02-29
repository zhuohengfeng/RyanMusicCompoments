package com.ryan.music.compoments.lib.alive;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 一个轻量的后台job service,利用空闲时间执行一些小事情，提高进程不被回收的概率
 */
@TargetApi(value = Build.VERSION_CODES.LOLLIPOP)
public class AliveJobService extends JobService {
    private static final String TAG = AliveJobService.class.getName();

    private JobScheduler mJobScheduler;

    private Handler mJobHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "pull alive.");
            jobFinished((JobParameters) msg.obj, false);
            return true;
        }
    });

    /**
     * 启动保活后台服务
     * @param context
     */
    public static void start(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(context, AliveJobService.class);
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    // startId的值是对这个service请求的activity或者其他实体的编号
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        JobInfo job = initJobInfo(startId);
        if (mJobScheduler.schedule(job) <= 0) {
            Log.d(TAG, "AliveJobService failed");
        } else {
            Log.d(TAG, "AliveJobService success");
        }
        // 可以自启
        return START_STICKY;
    }

    private JobInfo initJobInfo(int startId) {
        JobInfo.Builder builder = new JobInfo.Builder(startId,
                new ComponentName(getPackageName(), AliveJobService.class.getName()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS); //执行的最小延迟时间
            builder.setOverrideDeadline(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);  //执行的最长延时时间
            builder.setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS,
                    JobInfo.BACKOFF_POLICY_LINEAR);//线性重试方案
        } else {
            builder.setPeriodic(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);
        }
        builder.setPersisted(false);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setRequiresCharging(false);
        return builder.build();
    }


    // 如果返回值是false,系统假设这个方法返回时任务已经执行完毕。
    // 如果返回值是true,那么系统假定这个任务正要被执行,
    // 当任务执行完毕时你需要调用jobFinished(JobParameters params, boolean needsRescheduled)来通知系统。
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mJobHandler.sendMessage(Message.obtain(mJobHandler, 1, jobParameters));
        return true;
    }

    // 当系统接收到一个取消请求时，系统会调用onStopJob(JobParameters params)方法取消正在等待执行的任务。
    // 很重要的一点是如果onStartJob(JobParameters params)返回false,
    // 那么系统假定在接收到一个取消请求时已经没有正在运行的任务。
    // 换句话说，onStopJob(JobParameters params)在这种情况下不会被调用
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        mJobHandler.removeMessages( 1 );
        return false;
    }


}
