package com.bms;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.*;
import org.springframework.stereotype.Component;

@Component
public class PathHolderBean {
	private static final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static String JAR_TOKEN = ".jar!";
	
	private static String jarPath;
	private static String homeDir;
	private static String userDir;
	private static String keystorePath;

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
				try {
					String[] ss = s.split(JAR_TOKEN);
					String[] b = ss[0].split("file:");
					String base = b[1].substring(0, b[1].lastIndexOf('/'));
					System.out.println("Path to spring boot jar file: " + base);
					jarPath = base;
				} catch (Exception ex) {
					logger.info("Could not find base path of this appication's executable jar file");
				}
			}
		}
		return jarPath;
	}
	
	public String getHomeDir() {
		if (homeDir == null) {
			// user.home directory:
			String uh = System.getProperty("user.home");
			logger.info("User Home: " + uh);
			homeDir = uh;
		}
		return homeDir;
	}
	
	public String getUserDir() {
		if (userDir == null) {
			// this will get the directory that the java -jar command was run from, the user working directory
			String ud = System.getProperty("user.dir");
			logger.info("User Directory: " + ud);
			userDir = ud;
		}
		return userDir;
	}

	
	public String getKeystorePath() {
		Path path;
		if (keystorePath == null) {
			String[] testPaths = {this.getJarPath(), this.getHomeDir(), this.getUserDir()};
			// try jar path, then home directory, then user directory
			for (String testPath: testPaths) {
				if (testPath != null && !testPath.equals("")) {
					path = Paths.get(testPath);
					if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) && Files.isReadable(path) && Files.isWritable(path)) {
						keystorePath = testPath;
						return keystorePath;
					}
				}
			}
		}
		return keystorePath;
	}
}
