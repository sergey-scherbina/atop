package scherbina.sergey.atop;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class ATopService extends Service {

	public static void start(final Activity activity) {
		final Intent intent = new Intent(activity, ATopService.class);
		activity.startService(intent);
	}

	public static void stop(final Activity activity) {
		final Intent intent = new Intent(activity, ATopService.class);
		activity.stopService(intent);
	}

	private static final int NOTIFY_ID = 1;
	private static final NumberFormat format = DecimalFormat
			.getNumberInstance();

	private long delayMillis = 5;

	private NotificationManager notificationManager;
	private boolean stop = false;

	private final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> future;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void init() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		try {
			showCpuUsage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		startUpdateUsage();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			startForeground(NOTIFY_ID, buildNotification(CpuInfo.getCpuUsage()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		init();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stop = true;
		notificationManager.cancel(NOTIFY_ID);
		stopForeground(true);
		if (future != null) {
			future.cancel(true);
		}
		try {
			executor.shutdownNow();
			executor.awaitTermination(delayMillis, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startUpdateUsage() {
		if (stop) {
			notificationManager.cancel(NOTIFY_ID);
		} else {
			future = executor.schedule(new Runnable() {
				public void run() {
					try {
						showCpuUsage();
						startUpdateUsage();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, delayMillis, TimeUnit.SECONDS);
		}
	}

	private void showCpuUsage() throws IOException, InterruptedException {
		showNotification(CpuInfo.getCpuUsage());
	}

	private void showNotification(final double value) {
		notificationManager.notify(NOTIFY_ID, buildNotification(value));
	}

	private Notification buildNotification(final double value) {
		final Notification notification = new Notification(
				R.drawable.ic_launcher, null, System.currentTimeMillis());
		final String percents = format.format(value) + "%";
		notification.setLatestEventInfo(this, "CPU Usage", percents,
				PendingIntent.getActivity(this, 0, new Intent(this,
						ProcessesActivity.class), 0));
		notification.number = (int) value;
		return notification;
	}

}
