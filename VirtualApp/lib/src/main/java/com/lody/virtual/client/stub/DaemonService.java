package com.lody.virtual.client.stub;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


/**
 * DaemonService用于保活
 * 1. 在onCreate时，启动InnerService
 * 2. 保活方案:
 *	  a. 在onCreate时，调用startForeground将Service设置为前台Service（<b>注意前台Service处理不当，会出现耗电提醒问题</b>)
 *	  b. onStartCommand返回的是START_STICKY;
 *	  c. 在Service.onDestroy时，会重新启动DaemonService
 *
 * @author Lody
 *
 */
public class DaemonService extends Service {

    private static final int NOTIFY_ID = 1001;

	public static void startup(Context context) {
		context.startService(new Intent(context, DaemonService.class));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		startup(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
        startService(new Intent(this, InnerService.class));
        startForeground(NOTIFY_ID, new Notification());

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 如果service进程在启动时被kill掉，service仍处于start状态，但不保留本次的intent.
		// 随后系统会尝试重新创建service，由于Service处于start状态，在创建新的Service实例之后，会保证调用onStartCommand(***).
		// 如果在此期间，没有滞留的start command，则调用onStartCommand(***)时的intent为null. 我们需要intent=null做防护
		return START_STICKY;
	}

	public static final class InnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(NOTIFY_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}
	}


}
