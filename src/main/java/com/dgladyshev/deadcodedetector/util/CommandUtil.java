package com.dgladyshev.deadcodedetector.util;

import java.io.File;
import java.io.IOException;

public class CommandUtil {

	public static int runCommand(String cmd) throws IOException {
		Process process = Runtime.getRuntime().exec(cmd);
		int exitCode;
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException e) {
			exitCode = 1;
		}

		return exitCode;
	}

	public static int runCommandAndSaveOutput(String cmd, String output) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(cmd);
		builder.redirectOutput(new File(output));
		Process process = builder.start();
		int exitCode;
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException e) {
			exitCode = 1;
		}
		return exitCode;
	}



}
