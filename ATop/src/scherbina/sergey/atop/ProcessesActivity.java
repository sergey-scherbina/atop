package scherbina.sergey.atop;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ProcessesActivity extends Activity {

	private Button runService;

	private ListView processesList;

	private final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> future;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.processes);
		runService = (Button) findViewById(R.id.runService);
		processesList = (ListView) findViewById(R.id.processesList);
		runService.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				runService();
			}
		});
		ATopService.stop(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		future = executor.schedule(new Runnable() {
			public void run() {
				showProcessList();
			}
		}, 5, TimeUnit.SECONDS);
	}

	@Override
	protected void onPause() {
		super.onPause();
		future.cancel(true);
	}

	@Override
	protected void onStop() {
		super.onStop();
		executor.shutdownNow();
	}

	protected void showProcessList() {
		try {
			final List<ProcessInfo> processes = ProcessInfo.readProcessesList();
			final BaseAdapter adapter = new BaseAdapter() {

				public View getView(int position, View convertView,
						ViewGroup parent) {
					final TextView textView = new TextView(
							ProcessesActivity.this);
					textView.setLayoutParams(new ListView.LayoutParams(
							LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
					textView.setText(processes.get(position).getName());
					return textView;
				}

				public long getItemId(int position) {
					return position;
				}

				public Object getItem(int position) {
					return processes.get(position);
				}

				public int getCount() {
					return processes.size();
				}
			};
			runOnUiThread(new Runnable() {
				public void run() {
					processesList.setAdapter(adapter);
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void runService() {
		ATopService.start(this);
		finish();
	}
}
