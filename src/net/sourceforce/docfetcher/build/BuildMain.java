/*******************************************************************************
 * Copyright (c) 2011 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.build;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import net.sourceforge.docfetcher.Main;
import net.sourceforge.docfetcher.TestFiles;
import net.sourceforge.docfetcher.UtilGlobal;
import net.sourceforge.docfetcher.build.U.LineSep;
import net.sourceforge.docfetcher.man.Manual;
import net.sourceforge.docfetcher.util.AppUtil;
import net.sourceforge.docfetcher.util.Util;
import net.sourceforge.docfetcher.util.annotations.NotNull;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.Manifest.Attribute;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.google.common.base.Strings;

/**
 * @author Tran Nam Quang
 */
public final class BuildMain {

	public static final String appName = "DocFetcher";
	private static final String version = readVersionNumber();

	private static final String packageId = Main.class.getPackage().getName();
	private static final String packagePath = packageId.replace(".", "/");
	private static final String mainPath = "build/tmp/src/" + packagePath;

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
	private static final String buildDate = dateFormat.format(new Date());
	
	// Reads version number from file 'current-version.txt'
	@NotNull
	public static String readVersionNumber() {
		String versionStr = "";
		try {
			versionStr = U.read("current-version.txt").trim();
		}
		catch (Exception e) {
			Util.printErr(e);
			System.exit(0);
		}
		Util.checkThat(versionStr.split("\r?\n").length == 1);
		return versionStr;
	}

	public static void main(String[] args) throws Exception {
		Util.println("Copying sources to build directory...");
		U.copyDir("src", "build/tmp/src");

		// Licenses
		String licensePatterns = U.readPatterns("lib/license_patterns.txt");
		String eplFilename = "epl-v10.html";
		U.copyDir(
			"lib", "build/tmp/licenses", licensePatterns,
			"**/license_patterns.txt");
		U.copyBinaryFile("dist/" + eplFilename, "build/tmp/licenses/docfetcher/"
				+ eplFilename);
		U.zipDirContents("build/tmp/licenses", "build/tmp/licenses.zip");

		Util.println("Compiling sources...");
		Javac javac = new Javac();
		javac.setProject(new Project());
		javac.setSrcdir(new Paths().addDirSet("build/tmp/src").get());
		javac.setClasspath(new Paths().addFileSet("lib", "**/*.jar").get());
		javac.setSource("1.7");
		javac.setTarget("1.7");
		javac.setDebug(true);
		javac.setOptimize(true);
		javac.setFork(true); // Won't find javac executable without this
		javac.setEncoding("utf8"); // Needed for some Tika source files
		javac.execute();
		
		/*
		 * The following is an alternative way to compile the Java sources: It
		 * uses the javac command found on the PATH instead of Ant's Javac
		 * class. The code below might be helpful if there's an issue with
		 * Ant's Javac.
		 */
//		final List<String> javaFiles = new ArrayList<String>();
//		final FileWalker javaCollector = new FileWalker() {
//			@Override
//			protected void handleFile(@NotNull File file) {
//				if (!file.getName().toLowerCase().endsWith(".java")) {
//					return;
//				}
//				javaFiles.add(file.getPath());
//			}
//		};
//		javaCollector.run(new File("src"));
//		final String classPathSep = System.getProperty("path.separator");
//		final StringBuilder classPathStr = new StringBuilder();
//		final FileWalker jarCollector = new FileWalker() {
//			@Override
//			protected void handleFile(@NotNull File file) {
//				if (!file.getName().toLowerCase().endsWith(".jar")) {
//					return;
//				}
//				if (classPathStr.length() != 0) {
//					classPathStr.append(classPathSep);
//				}
//				classPathStr.append(file.getPath());
//			}
//		};
//		jarCollector.run(new File("lib"));
//		final String[] javaCmdArr = new String[] {
//			"javac",
//			"-sourcepath", "build/tmp/src",
//			"-classpath", classPathStr.toString(),
//			"-g:lines",
//			"-encoding", "UTF8",
//			"-source", "1.7",
//			"-target", "1.7"
//		};
//		final List<String> javaCmdList = new ArrayList<String>();
//		for (String s : javaCmdArr) {
//			javaCmdList.add(s);
//		}
//		for (String s : javaFiles) {
//			javaCmdList.add(s);
//		}
//		ProcessBuilder pb = new ProcessBuilder().inheritIO();
//		pb.redirectErrorStream(true);
//		pb.command(javaCmdList).start().waitFor();

		recreateJarFile("", false, LineSep.WINDOWS); // Needed for NSIS script
		File portableJar = recreateJarFile("portable_", true, LineSep.WINDOWS);
		File macOsXJar = recreateJarFile("macosx_", false, LineSep.UNIX);

		rebuildManuals();
		createPortableBuild(portableJar);
		createMacOsXBuild(macOsXJar);
		runTests();
	}

	private static File recreateJarFile(String jarPrefix, boolean isPortable,
			LineSep lineSep) throws Exception {
		String msgPrefix = isPortable ? "" : "non-";
		Util.println(U.format("Creating %sportable jar file...", msgPrefix));

		File systemConfDest = new File(mainPath + "/system-conf.txt");
		systemConfDest.delete();

		File programConfDest = new File(mainPath + "/program-conf.txt");
		programConfDest.delete();

		File mainJarFile = new File(String.format(
			"build/tmp/%s%s_%s_%s.jar", jarPrefix, packageId, version, buildDate));
		mainJarFile.delete();

		U.copyTextFile(
			"dist/system-template-conf.txt",
			systemConfDest.getPath(),
			lineSep,
			"${app_name}", appName,
			"${app_version}", version,
			"${build_date}", buildDate,
			"${is_portable}", String.valueOf(isPortable));

		U.copyTextFile(
			"dist/program-conf.txt",
			programConfDest.getPath(),
			lineSep);

		Jar jar = new Jar();
		jar.setProject(new Project());
		jar.setDestFile(mainJarFile);
		jar.setBasedir(new File("build/tmp/src"));

		Manifest manifest = new Manifest();
		Attribute attr = new Attribute();
		attr.setName("Main-Class");
		attr.setValue(Main.class.getName());
		manifest.addConfiguredAttribute(attr);
		jar.addConfiguredManifest(manifest);

		jar.execute();
		return mainJarFile;
	}

	// Must be run before updating the version numbers in the manual pages
	private static void rebuildManuals() throws Exception {
		File helpDir = new File("dist/help/");
		if (helpDir.isDirectory())
			Util.deleteContents(helpDir);
		
		for (File dir : Util.listFiles(new File(Manual.manDir))) {
			if (!dir.isDirectory() || dir.getName().equals("all"))
				continue;
			Manual.main(new String[] { dir.getName() });
		}
	}

	private static void createPortableBuild(File tmpMainJar) throws Exception {
		Util.println("Creating portable build...");
		String releaseDir = U.format("build/%s-%s", appName, version);
		U.copyDir("dist/img", releaseDir + "/img");
		U.copyDir("dist/help", releaseDir + "/help");
//		U.copyDir("dist/templates", releaseDir + "/templates");
		U.copyDir("dist/lang", releaseDir + "/lang", "**/*.properties", null);
		updateManualVersionNumber(new File(releaseDir, "help"));

		String excludedLibs = U.readPatterns("lib/excluded_jar_patterns.txt");
		U.copyFlatten("lib", releaseDir + "/lib", "**/*.jar", excludedLibs);
		U.copyFlatten("lib", releaseDir + "/lib/swt", "**/swt*.jar", null);
		U.copyFlatten("lib", releaseDir + "/lib", "**/*.so, **/*.dll, **/*.dylib", null);

		String jarName = U.removePrefix(tmpMainJar.getName(), "portable_");
		String dstMainJar = U.format("%s/lib/%s", releaseDir, jarName);
		U.copyBinaryFile(tmpMainJar.getPath(), dstMainJar);

		// Linux launchers
		String linuxLauncherGtk2 = U.format("%s/%s-GTK2.sh", releaseDir, appName);
		U.copyTextFile(
			"dist/launchers/launcher-linux-gtk2.sh",
			linuxLauncherGtk2,
			LineSep.UNIX,
			"${main_class}", Main.class.getName()
		);
		String linuxLauncherGtk3 = U.format("%s/%s-GTK3.sh", releaseDir, appName);
		U.copyTextFile(
			"dist/launchers/launcher-linux-gtk3.sh",
			linuxLauncherGtk3,
			LineSep.UNIX,
			"${main_class}", Main.class.getName()
		);

		/*
		 * Alternative (fallback) launcher for OS X, workaround for the
		 * application bundle not being signed. See:
		 * https://sourceforge.net/p/docfetcher/discussion/702424/thread/ee550dd7e4/
		 */
		String macOsXLauncherAlt = U.format("%s/%s-macOS", releaseDir, appName);
		U.copyTextFile(
			"dist/launchers/launcher-macosx-portable-alt.sh",
			macOsXLauncherAlt,
			LineSep.UNIX
		);
		
		// Create DocFetcher.app launcher for Mac OS X
		String macOsXLauncher = U.format("%s/%s.app/Contents/MacOS/%s", releaseDir, appName, appName);
		U.copyTextFile(
			"dist/launchers/launcher-macosx-portable.sh",
			macOsXLauncher,
			LineSep.UNIX,
			"${app_name}", appName,
			"${main_class}", Main.class.getName()
		);
		U.copyBinaryFile(
			"dist/DocFetcher.icns",
			U.format("%s/%s.app/Contents/Resources/%s.icns", releaseDir, appName, appName));
		deployInfoPlist(new File(U.format("%s/%s.app/Contents", releaseDir, appName)));

		makeExecutable(
			"Cannot make the portable launcher shell scripts executable.",
			linuxLauncherGtk2, linuxLauncherGtk3, macOsXLauncher,
			macOsXLauncherAlt);

		String exeLauncher = U.format("%s/%s.exe", releaseDir, appName);
		U.copyBinaryFile("dist/launchers/DocFetcher-1024.exe", exeLauncher);
		
		// Copy alternative Windows exe launchers
		for (File file : new File("dist/launchers").listFiles()) {
			String name = file.getName();
			if (!file.isFile() || !name.matches("DocFetcher-\\d+.*?\\.exe")) {
				continue;
			}
			exeLauncher = U.format("%s/misc/%s", releaseDir, name);
			U.copyBinaryFile("dist/launchers/" + name, exeLauncher);
		}

		String batLauncher = U.format("%s/misc/%s.bat", releaseDir, appName);
		U.copyTextFile(
			"dist/launchers/launcher.bat", batLauncher, LineSep.WINDOWS,
			"${main_class}", Main.class.getName());

		String[] daemonNames = new String[] {
			"docfetcher-daemon-windows.exe", "docfetcher-daemon-linux" };
		for (String daemonName : daemonNames) {
			String dstPath = releaseDir + "/" + daemonName;
			U.copyBinaryFile("dist/daemon/" + daemonName, dstPath);
		}
		makeExecutable("Cannot make the Linux daemon executable.", releaseDir
				+ "/" + daemonNames[1]);

		U.copyTextFile(
			"dist/program-conf.txt", releaseDir + "/conf/program-conf.txt", LineSep.WINDOWS);

		U.copyTextFile("dist/paths.txt", releaseDir + "/misc/paths.txt", LineSep.WINDOWS);
		
		U.copyBinaryFile("build/tmp/licenses.zip", releaseDir
				+ "/misc/licenses.zip");

		/*
		 * Create an empty file 'indexes/.indexes.txt' to let the daemons know
		 * we're the portable version.
		 */
		U.write("", releaseDir + "/indexes/.indexes.txt");

		U.copyTextFile(
			"dist/Readme.txt", releaseDir + "/Readme.txt", LineSep.WINDOWS);
		
		// Scripting support
		U.copyDir("dist/py4j", releaseDir + "/py4j");
		U.copyTextFile(
			"dist/search.py", releaseDir + "/search.py", LineSep.WINDOWS);
		makeExecutable("Cannot make search.py executable.", releaseDir + "/search.py");
		
		// Wrap the portable build in a zip archive for deployment
		if (Util.IS_LINUX || Util.IS_MAC_OS_X) {
			String cmd = "zip -r -q %s-%s-portable.zip %s-%s";
			U.execInDir(new File("build"), cmd, appName.toLowerCase(), version, appName, version);
		}
		else {
			String zipPath = U.format("build/%s-%s-portable.zip", appName.toLowerCase(), version);
			U.zipDir(releaseDir, zipPath);
			Util.println("** Warning: Could not preserve executable flags in archive: " + zipPath);
		}
	}

	private static void deployInfoPlist(File dstDir) throws Exception {
		U.copyTextFile(
			"dist/Info.plist",
			new File(dstDir, "Info.plist").getPath(),
			LineSep.UNIX,
			"${app_name}", appName,
			"${app_version}", version,
			"${package_id}", packageId);
	}

	private static void makeExecutable(String errorMessage, String... paths)
			throws Exception {
		if (Util.IS_LINUX || Util.IS_MAC_OS_X) {
			for (String path : paths)
				U.exec("chmod +x %s", Util.getAbsPath(path));
		}
		else {
			Util.printErr("** Warning: " + errorMessage);
		}
	}

	private static void createMacOsXBuild(File tmpMainJar) throws Exception {
		String suffix = Util.IS_MAC_OS_X ? "" : " (without disk image packaging)";
		Util.println(U.format("Creating Mac OS X build%s...", suffix));

		String appDir = U.format("build/%s.app", appName);
		String contentsDir = appDir + "/Contents";
		String resourcesDir = contentsDir + "/Resources";

		U.copyDir("dist/img", resourcesDir + "/img");
		U.copyDir("dist/help", resourcesDir + "/help");
		U.copyDir("dist/lang", resourcesDir + "/lang");
		updateManualVersionNumber(new File(resourcesDir, "help"));
		U.copyBinaryFile(
			"dist/DocFetcher.icns",
			U.format("%s/%s.icns", resourcesDir, appName));
		deployInfoPlist(new File(contentsDir));

		String excludedLibs = U.readPatterns("lib/excluded_jar_patterns.txt");
		U.copyFlatten("lib", resourcesDir + "/lib", "**/*.jar", excludedLibs);
		U.copyFlatten("lib", resourcesDir + "/lib/swt", "**/swt*mac*.jar", null);
		U.copyFlatten("lib", resourcesDir + "/lib", "**/*.dylib", null);

		String jarName = U.removePrefix(tmpMainJar.getName(), "macosx_");
		String dstMainJar = U.format("%s/lib/%s", resourcesDir, jarName);
		U.copyBinaryFile(tmpMainJar.getPath(), dstMainJar);

		String launcher = U.format("%s/MacOS/%s", contentsDir, appName);
		U.copyTextFile(
			"dist/launchers/launcher-macosx-app.sh", launcher, LineSep.UNIX,
			"${app_name}", appName,
			"${main_class}", Main.class.getName()
		);
		makeExecutable(
			"Cannot make the Mac OS X launcher shell script executable.",
			launcher);

		U.copyTextFile("dist/paths.txt", resourcesDir + "/misc/paths.txt", LineSep.WINDOWS);
		
		U.copyBinaryFile("build/tmp/licenses.zip", resourcesDir
				+ "/misc/licenses.zip");
		
		// Scripting support
		U.copyDir("dist/py4j", resourcesDir + "/py4j");
		U.copyTextFile(
			"dist/search.py", resourcesDir + "/search.py", LineSep.WINDOWS);
		makeExecutable("Cannot make search.py executable.", resourcesDir + "/search.py");
		
		if (Util.IS_MAC_OS_X) {
			String dmgPath = U.format("build/%s-%s.dmg", appName, version);
			U.exec("hdiutil create -srcfolder %s %s", appDir, dmgPath);
		}
	}

	private static void updateManualVersionNumber(File helpDir) throws Exception {
		for (File dir : Util.listFiles(helpDir)) {
			File manFile = new File(dir, "DocFetcher_Manual.html");
			String manPath = manFile.getPath();
			String contents = U.read(manPath);
			contents = UtilGlobal.replace(manPath, contents, "${version}", version);
			U.write(contents, manPath);
		}
	}

	private static void runTests() {
		Util.println("Running tests...");
		
		Logger logger = Logger.getLogger("org.apache.pdfbox.pdfparser.PDFParser");
		logger.setLevel(java.util.logging.Level.OFF);
		
		final List<String> classNames = new ArrayList<String>();
		new FileWalker() {
			protected void handleFile(File file) {
				String name = file.getName();
				if (!name.endsWith(".java"))
					return;
				name = Util.splitFilename(name)[0];
				if (!name.startsWith("Test") && !name.endsWith("Test"))
					return;
				String path = file.getPath();
				int start = "src/".length();
				int end = path.length() - ".java".length();
				path = path.substring(start, end);
				path = path.replace("/", ".").replace("\\", ".");
				if (path.equals(TestFiles.class.getName()))
					return;
				classNames.add(path);
			}
		}.run(new File("src"));

		Collections.sort(classNames);

		JUnitCore junit = new JUnitCore();
		junit.addListener(new RunListener() {
			public void testFailure(Failure failure) throws Exception {
				Util.printErr(Strings.repeat(" ", 8) + "FAILED");
			}
		});

		for (String className : classNames) {
			/*
			 * AppUtil.Const must be cleared before each test, otherwise one
			 * test class could load AppUtil.Const and thereby hide
			 * AppUtil.Const loading failures in subsequent tests.
			 */
			AppUtil.Const.clear();
			try {
				Class<?> clazz = Class.forName(className);
				Util.println(Strings.repeat(" ", 4) + className);
				junit.run(clazz);
			}
			catch (ClassNotFoundException e) {
				Util.printErr(e);
			}
		}
		AppUtil.Const.clear();
	}

}
