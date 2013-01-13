package scherbina.sergey.atop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CpuInfo {

	private final long /* unsigned */ user;
	private final long /* unsigned */ nice;
	private final long /* unsigned */ system;
	private final long /* unsigned */ idle;
	private final long /* unsigned */ iowait;
	private final long /* unsigned */ irq;
	private final long /* unsigned */ softirq;
	private final long /* unsigned */ total;

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

	private static final String PROC = "/proc";
	private static final String PROC_STAT = PROC + "/stat";
	private static final String space = "\\s+";

	private static CpuInfo readProcStat() throws IOException {
		final BufferedReader stat = new BufferedReader(new FileReader(new File(
				PROC_STAT)));
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

	public static double getCpuUsage() throws IOException, InterruptedException {
		return getCpuUsage(1000);
	}

	public static double getCpuUsage(final int idle) throws IOException,
			InterruptedException {
		final CpuInfo stat1 = readProcStat();
		Thread.sleep(idle);
		return readProcStat().getCpuUsage(stat1);
	}

	private double getCpuUsage(final CpuInfo old) {
		final long delta = total - old.total;
		return ((user + nice + system) - (old.user + old.nice + old.system))
				* 100 / delta;
	}
}
