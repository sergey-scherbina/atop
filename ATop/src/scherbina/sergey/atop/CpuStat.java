package scherbina.sergey.atop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CpuStat {

	private static final String PROC = "/proc";
	private static final String STAT = "stat";
	private static final String space = "\\s+";

	public static class CpuInfo {

		private final long /* unsigned */user;
		private final long /* unsigned */nice;
		private final long /* unsigned */system;
		private final long /* unsigned */idle;
		private final long /* unsigned */iowait;
		private final long /* unsigned */irq;
		private final long /* unsigned */softirq;
		private final long /* unsigned */total;

		private CpuInfo(final long user, final long nice, final long system,
				final long idle, final long iowait, final long irq,
				final long softirq) {
			this.user = user;
			this.nice = nice;
			this.system = system;
			this.idle = idle;
			this.iowait = iowait;
			this.irq = irq;
			this.softirq = softirq;
			total = user + nice + system + idle + iowait + irq + softirq;
		}

	}

	public static CpuInfo readCpuInfo() throws IOException {
		final BufferedReader stat = new BufferedReader(new FileReader(new File(
				PROC, STAT)));
		try {
			final String line = stat.readLine();
			final String[] cpu = line.split(space);
			final long user = Long.parseLong(cpu[1]);
			final long nice = Long.parseLong(cpu[2]);
			final long system = Long.parseLong(cpu[3]);
			final long idle = Long.parseLong(cpu[4]);
			final long iowait = Long.parseLong(cpu[5]);
			final long irq = Long.parseLong(cpu[6]);
			final long softirq = Long.parseLong(cpu[7]);
			return new CpuInfo(user, nice, system, idle, iowait, irq, softirq);
		} finally {
			stat.close();
		}
	}

	public static class ProcessInfo {

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

	}

	private static Map<Integer, ProcessInfo> readProcessInfos() {
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
}
