package hw2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by fhrenic on 12/11/2016.
 */
public class Runner {

	public static void main(String[] args) throws IOException {

		List<Process> processes = new LinkedList<>();
		Util.debug("Starting nodes...");

		URL url = Runner.class.getClassLoader().getResource("");
		if (url == null) { // won't happen
			throw new IllegalStateException("Can't get working directory");
		}
		File root = new File(url.getPath());

		String[] cmd = new String[]{"java", Node.class.getName(), "nodename"};
		for (String nodename : Config.getNodeNames()) {
			cmd[2] = nodename;
			processes.add(new ProcessBuilder(cmd).directory(root).inheritIO().start());
		}

		loop();

		processes.forEach(Process::destroy);
	}

	private static void loop() throws IOException {
		System.out.println("> all nodes started");
		System.out.println("> type exit to stop");

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			System.out.print("> ");
			if ("exit".equalsIgnoreCase(in.readLine())) {
				break;
			}
			System.out.println("> unknown command, accepting only \"exit\"");
		}
	}
}
