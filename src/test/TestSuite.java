package test;

import common.util.ConfigUtil;
import common.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class TestSuite {

	public static int NODE_COUNT = 2;


	private static class TestCLI implements Runnable {
		
		private List<Process> processes;
		
		public TestCLI(List<Process> processes) {
			this.processes = processes;
		}
		
		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);
			
			while(true) {
				String line = sc.nextLine();
				if (line.equals("stop")) {
					for (Process process : processes) {
						process.destroy();
					}
					break;
				}
			}
			sc.close();
		}
	}
	
	/**
	 * The parameter for this function should be the name of a directory that
	 * contains a test.properties file which will describe our distributed system.
	 */
	private static void startTest(String testName) {
		List<Process> processes = new ArrayList<>();
		
		Log.info("Starting multiple servent runner. If servents do not finish on their own, type \"stop\" to finish them");

		try {
			ProcessBuilder builder = new ProcessBuilder("java", "-cp", "bin/", "bootstrap.BootstrapMain", "0.0.0.0", "11000");

			//We use files to read and write.
			//System.out, System.err and System.in will point to these files.
			builder.redirectOutput(new File(testName+"/output/bootstrap_out.txt"));
			builder.redirectError(new File(testName+"/error/bootstrap_err.txt"));
			builder.redirectInput(new File(testName+"/input/bootstrap_in.txt"));

			//Starts the servent as a completely separate process.
			Process p = builder.start();
			processes.add(p);

		} catch (IOException e) {
			e.printStackTrace();
		}


		for(int i = 1; i <= NODE_COUNT; i++) {
			try {
				ProcessBuilder builder = new ProcessBuilder("java", "-cp", "bin/", "node.NodeMain", "localhost", "1100"+i, "0.0.0.0", "11000");

				//We use files to read and write.
				//System.out, System.err and System.in will point to these files.
				builder.redirectOutput(new File(testName+"/output/node" + i + "_out.txt"));
				builder.redirectError(new File(testName+"/error/node" + i + "_err.txt"));
				builder.redirectInput(new File(testName+"/input/node" + i + "_in.txt"));

				//Starts the servent as a completely separate process.
				Process p = builder.start();
				processes.add(p);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		Thread t = new Thread(new TestCLI(processes));
		
		t.start(); //CLI thread waiting for user to type "stop".
		
		for (Process process : processes) {
			try {
				process.waitFor(); //Wait for graceful process finish.
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Log.info("All servent processes finished. Type \"stop\" to exit.");
	}
	
	public static void main(String[] args) {
		startTest("showtime");
	}

}
