package com.bms;

import java.net.URL;

import org.springframework.stereotype.Component;

@Component
public class PathHolderBean {
	private static String jarPath;
	private static String homeDir;
	private static String userDir;
	private static String JAR_TOKEN = ".jar!";

	/**
	 * Get the folder of the executable JAR file that is used to run this program with
	 * the java -jar JARfile.jar command.
	 * 
	 * If this application was not started as an executable JAR it will return null.
	 * 
	 * @return String jarPath, the fully qualified path of the executable JAR file
	 */
	public String getJarPath() {
		if (jarPath == null) {
			String fs = System.getProperty("file.separator");
			String c1 = this.getClass().getName();
			String file = c1.replaceAll("\\.", fs)+".class";
			ClassLoader classLoader = this.getClass().getClassLoader();
			URL url = classLoader.getResource(file);
			System.out.printf("url path: %s%nurl file: %s%n", url.getPath(), url.getFile());
			String s = url.getPath();
			if (s.contains(JAR_TOKEN)) {
				String[] ss = s.split(JAR_TOKEN);
				String[] b = ss[0].split("file:");
				String base = b[1].substring(0, b[1].lastIndexOf('/'));
				System.out.println("Path to spring boot jar file: " + base);
				jarPath = base;
			}
		}
		return jarPath;
	}
	
	public String getHomeDir() {
		if (homeDir == null) {
			// user.home directory:
			String uh = System.getProperty("user.home");
			System.out.println("User Home: " + uh);
			homeDir = uh;
		}
		return homeDir;
	}
	
	public String getUserDir() {
		if (userDir == null) {
			// this will get the directory that the java -jar command was run from! - User working directory
			String ud = System.getProperty("user.dir");
			System.out.println("User Directory: " + ud);
			userDir = ud;
		}
		return userDir;
	}

}
