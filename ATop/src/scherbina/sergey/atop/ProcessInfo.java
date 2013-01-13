package scherbina.sergey.atop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessInfo {

	private static final String space = "\\s+";

	private int pid;
	private String name;
	private String state;
	private long /* unsigned */utime;
	private long /* unsigned */stime;

	public String getName() {
		return name;
	}

	public ProcessInfo(final File stat) throws IOException {
		readStat(stat);
	}

	public ProcessInfo(final ProcessInfo oldProc, final ProcessInfo newProc) {
		this.pid = newProc.pid;
		this.name = newProc.name;
		this.state = newProc.state;
		this.utime = oldProc != null ? newProc.utime - oldProc.utime : 0;
		this.stime = oldProc != null ? newProc.stime - oldProc.stime : 0;
	}

	private void readStat(final File file) throws IOException {
		final BufferedReader stat = new BufferedReader(new FileReader(file));
		try {
			final String line = stat.readLine();
			final String[] proc = line.split(space);
			this.pid = Integer.parseInt(proc[0]);
			this.name = proc[1];
			this.state = proc[2];
			this.utime = Long.parseLong(proc[13]);
			this.stime = Long.parseLong(proc[14]);
		} finally {
			stat.close();
		}
	}

	private static Map<Integer, ProcessInfo> readProcesses() {
		final Map<Integer, ProcessInfo> procs = new HashMap<Integer, ProcessInfo>();
		for (final File f : new File("proc").listFiles()) {
			try {
				procs.put(Integer.parseInt(f.getName()), new ProcessInfo(
						new File(f, "stat")));
			} catch (Throwable e) {
				continue;
			}
		}
		return procs;
	}

	public static List<ProcessInfo> readProcessesList(final int idle)
			throws InterruptedException {
		final Map<Integer, ProcessInfo> oldProcs = readProcesses();
		Thread.sleep(idle);
		final Map<Integer, ProcessInfo> newProcs = readProcesses();
		final List<ProcessInfo> processesList = new ArrayList<ProcessInfo>();
		for (final Integer pid : newProcs.keySet()) {
			processesList.add(new ProcessInfo(oldProcs.get(pid), newProcs
					.get(pid)));
		}
		Collections.sort(processesList, new Comparator<ProcessInfo>() {
			public int compare(final ProcessInfo lhs, final ProcessInfo rhs) {
				final Long lhsTotal = Long.valueOf(lhs.utime + lhs.stime);
				final Long rhsTotal = Long.valueOf(rhs.utime + rhs.stime);
				return rhsTotal.compareTo(lhsTotal);
			}
		});
		return processesList;
	}

	public static List<ProcessInfo> readProcessesList()
			throws InterruptedException {
		return readProcessesList(1000);
	}
}
