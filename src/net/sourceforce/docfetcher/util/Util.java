/*******************************************************************************
 * Copyright (c) 2010, 2011 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;

import net.sourceforge.docfetcher.util.annotations.MutableCopy;
import net.sourceforge.docfetcher.util.annotations.NotNull;
import net.sourceforge.docfetcher.util.annotations.Nullable;
import net.sourceforge.docfetcher.util.annotations.ThreadSafe;

/**
 * @author Tran Nam Quang
 */
public final class Util {

	/*
	 * TODO pre-release: consider structuring the methods in this class by putting them into
	 * public static inner classes.
	 */

	/** Whether the platform is Windows. */
	public static final boolean IS_WINDOWS;

	/** Whether the platform is Linux. */
	public static final boolean IS_LINUX;

	/** Whether the platform is Linux KDE. */
	public static final boolean IS_LINUX_KDE;
	
	/** Whether the operating system is Ubuntu and has the Unity desktop. */
	public static final boolean IS_UBUNTU_UNITY;

	/** Whether the platform is Mac OS X. */
	public static final boolean IS_MAC_OS_X;

	public static final boolean IS_64_BIT_JVM;

	/** The system's temporary directory. Does not contain backward slashes. */
	public static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

	/** The current directory. Does not contain backward slashes. */
	public static final String USER_DIR_PATH = toForwardSlashes(System.getProperty("user.dir"));

	/** The current directory. */
	public static final File USER_DIR = new File(USER_DIR_PATH);

	/** The user's home directory. Does not contain backward slashes. */
	public static final String USER_HOME_PATH = System.getProperty("user.home");
	
	/** Line separator character ('\r\n' on Windows, '\n' on Linux). */
	public static final String LS = System.getProperty("line.separator");

	/**
	 * File separator character. On Windows, this is '\', and on Linux, it's
	 * '/'.
	 */
	public static final String FS = System.getProperty("file.separator");

	static {
		String osName = System.getProperty("os.name").toLowerCase();
		IS_WINDOWS = osName.contains("windows");
		IS_LINUX = osName.contains("linux");
		IS_UBUNTU_UNITY = isUbuntuUnity(IS_LINUX);
		IS_LINUX_KDE = IS_LINUX && System.getenv("KDE_FULL_SESSION") != null;
		IS_MAC_OS_X = osName.equals("mac os x");

		String arch = System.getProperty("sun.arch.data.model");
		if (arch == null)
			arch = System.getProperty("os.arch").toLowerCase();
		IS_64_BIT_JVM = arch.contains("64");
	}
	
	private Util() {}
	
	private static boolean isUbuntuUnity(boolean isLinux) {
		if (!isLinux)
			return false;
		try {
			String output = getProcessOutput("lsb_release -irs").trim();
			String[] lines = output.split("\n");
			if (lines.length != 2)
				return false;
			if (!lines[0].trim().toLowerCase().equals("ubuntu"))
				return false;
			
			// See: http://askubuntu.com/questions/70296/is-there-an-environment-variable-that-is-set-for-unity
			if (lines[1].trim().equals("11.04"))
				return "gnome".equals(System.getenv("DESKTOP_SESSION"))
						&& "gnome".equals(System.getenv("GDMSESSION"));
			return "Unity".equals(System.getenv("XDG_CURRENT_DESKTOP"));
		}
		catch (IOException e) {
			return false;
		}
	}
	
	@NotNull
	private static String getProcessOutput(@NotNull String command)
			throws IOException {
		Process p = Runtime.getRuntime().exec(command);
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		StringBuilder sb = new StringBuilder();
		boolean firstLine = true;
		while (true) {
			String line = in.readLine();
			if (line == null)
				break;
			if (firstLine)
				firstLine = false;
			else
				sb.append(Util.LS);
			sb.append(line);
		}
		return sb.toString();
	}

	/**
	 * Splits the given string into an integer array. Any characters other than
	 * digits and the 'minus' are treated as separators.
	 * <p>
	 * If the string cannot be parsed, the given array of default values is
	 * returned. If the string contains numbers that are greater than
	 * {@code Integer.MAX_VALUE} or less than {@code Integer.MIN_VALUE}, those
	 * numbers will be clamped.
	 */
	public static int[] toIntArray(String str, int[] defaultValues) {
		if (str.trim().equals(""))
			return new int[0];
		String[] rawValues = str.split("[^-\\d]+");
		int[] array = new int[rawValues.length];
		for (int i = 0; i < rawValues.length; i++) {
			try {
				array[i] = Integer.parseInt(rawValues[i]);
			}
			catch (NumberFormatException e) {
				if (rawValues[i].matches("\\d{10,}"))
					array[i] = Integer.MAX_VALUE;
				else if (rawValues[i].matches("-\\d{10,}"))
					array[i] = Integer.MIN_VALUE;
				else
					return defaultValues;
			}
		}
		return array;
	}

	/**
	 * Returns the given integer string as an {@code int} value. Leading and
	 * trailing whitespaces are ignored. If the string cannot be parsed, the
	 * given default value is returned. If the string is a number, but greater
	 * than {@code Integer.MAX_VALUE} or less than {@code Integer.MIN_VALUE}, a
	 * clamped value is returned.
	 */
	public static int toInt(String value, int defaultValue) {
		value = value.trim();
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			if (value.matches("\\d{10,}"))
				return Integer.MAX_VALUE;
			else if (value.matches("-\\d{10,}"))
				return Integer.MIN_VALUE;
		}
		return defaultValue;
	}

	/**
	 * Encodes the given collection of strings into a single string, using the
	 * specified separator. The resulting string is a concatenation of the
	 * elements of the collection, which are separated by the given separator
	 * and where occurrences of the separator and backslashes are escaped
	 * appropriately.
	 *
	 * @see Util#decodeStrings(String, char)
	 */
	@NotNull
	public static String encodeStrings(	@NotNull String sep,
										@NotNull Collection<String> parts) {
		Util.checkNotNull(sep, parts);
		if (parts.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (String part : parts) {
			if (!isFirst)
				sb.append(sep);
			sb.append(part.replace("\\", "\\\\").replace(sep, "\\" + sep));
			isFirst = false;
		}
		return sb.toString();
	}

	/**
	 * Decodes the given string into a list of strings, using the specified
	 * separator. This method basically splits the given string at those
	 * occurrences of the separator that aren't escaped with a backslash.
	 * <p>
	 * Special case: If the given string is an empty or a blank string, an empty
	 * list is returned.
	 *
	 * @see Util#encodeStrings(String, char)
	 */
	@MutableCopy
	@NotNull
	public static List<String> decodeStrings(char sep, @NotNull String str) {
		Util.checkNotNull(str);
		if (str.trim().isEmpty())
			return new ArrayList<String>(0);
		boolean precedingBackslash = false;
		List<String> parts = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == sep && ! precedingBackslash) {
				parts.add(sb.toString());
				sb.delete(0, sb.length());
			}
			else if (c != '\\' || precedingBackslash)
				sb.append(c);
			if (c == '\\')
				precedingBackslash = ! precedingBackslash;
			else
				precedingBackslash = false;
		}
		parts.add(sb.toString());
		return parts;
	}
	
	@NotNull
	public static String encodeBase64(@NotNull String input) {
		String encodedBytes = Base64.encodeBase64URLSafeString(input.getBytes());
		return new String(encodedBytes);
	}

	/**
	 * Shortens the given string if its length exceeds a fixed limit.
	 */
	@NotNull
	public static String truncate(@NotNull String str) {
		if (str.length() > 32)
			return str.substring(0, 32) + "..."; //$NON-NLS-1$
		return str;
	}
	
	/**
	 * Removes any leading whitespace from the input string and returns the
	 * resulting string.
	 *
	 * @throws IllegalArgumentException
	 *             if the input string is null.
	 * @see String#trim()
	 */
	@NotNull
	public static String trimLeft(@NotNull String input) {
		if (input == null)
			throw new IllegalArgumentException();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (! Character.isWhitespace(c))
				return input.substring(i);
		}
		return "";
	}

	/**
	 * Removes any trailing whitespace from the input string and returns the
	 * resulting string.
	 *
	 * @throws IllegalArgumentException
	 *             if the input string is null.
	 * @see String#trim()
	 */
	@NotNull
	public static String trimRight(@NotNull String input) {
		final int len = input.length();
		for (int i = len - 1; i >= 0; i--) {
			char c = input.charAt(i);
			if (! Character.isWhitespace(c))
				return input.substring(0, i + 1);
		}
		return "";
	}
	
	/**
	 * For a given string and a given array of start-end offset pairs, returns a
	 * map from zero-based line indices to the start-end offset pairs associated
	 * with that particular line.
	 */
	@NotNull
	public static Map<Integer, int[]> createLineMap(@NotNull String string,
													@NotNull int[] spans) {
		Util.checkThat(spans.length % 2 == 0);
		if (spans.length == 0) {
			return Collections.emptyMap();
		}
		List<SpanObject<int[]>> spanObjects = new ArrayList<>();
		for (int i = 0; i < spans.length / 2; i++) {
			int[] span = new int[] {spans[i * 2], spans[i * 2 + 1]};
			spanObjects.add(new SpanObject<int[]>(span, span[0], span[1]));
		}
		final Map<Integer, int[]> lineMap = new HashMap<>();
		LineMap<int[]> lineMapAdder = new LineMap<int[]>() {
			@Override
			public void add(int lineIndex, int[] span) {
				int[] oldSpans = lineMap.get(lineIndex);
				if (oldSpans == null) {
					lineMap.put(lineIndex, span);
				} else {
					int[] newSpans = new int[oldSpans.length + span.length];
					System.arraycopy(oldSpans, 0, newSpans, 0, oldSpans.length);
					System.arraycopy(span, 0, newSpans, oldSpans.length, span.length);
					lineMap.put(lineIndex, newSpans);
				}
			}
		};
		createLineMap(string, spanObjects, lineMapAdder);
		return lineMap;
	}
	
	private static <A> void createLineMap(	String string,
											Collection<SpanObject<A>> spanObjects,
											LineMap<A> lineMap) {
		if (spanObjects.isEmpty()) {
			return;
		}
		int breakCount = 0;
		int lastStart = 0;
		for (SpanObject<A> spanObject : spanObjects) {
			int start = spanObject.start;
			int end = spanObject.end;
			if (0 <= start && end <= string.length()) {
				assert (start <= end);
				breakCount += lineCount(string, lastStart, start) - 1;
				int startLine = breakCount;
				breakCount += lineCount(string, start, end) - 1;
				for (int lineIndex = startLine; lineIndex <= breakCount; lineIndex++) {
					lineMap.add(lineIndex, spanObject.object);
				}
				lastStart = end;
			}
		}
	}

	private  static final class SpanObject<A> {
		public final A object;
		public final int start;
		public final int end;
//		public Iterable<String> parts = Collections.emptyList();

		public SpanObject(A object, int start, int end) {
			this.object = object;
			this.start = start;
			this.end = end;
		}
	}

	private  static interface LineMap<A> {
		public void add(int lineIndex, A object);
	}
	
	/** Returns the number of lines in the given range of the given string. */
	private static int lineCount(String string, int start, int end) {
		int count = 1;
		boolean rState = false;
		int i = start;
		while (i < end) {
			char c = string.charAt(i);
			if (rState) {
				count += 1;
				if (c != '\r') {
					rState = false;
				}
			} else {
				if (c == '\r') {
					rState = true;
				} else if (c == '\n') {
					count += 1;
				}
			}
			i += 1;
		}
		if (rState) {
			count += 1;
		}
		return count;
	}

	public static <T> boolean equals(@NotNull Collection<T> col, @NotNull T[] a) {
		Util.checkNotNull(col, a);
		if (col.size() != a.length)
			return false;
		int i = 0;
		for (T e1 : col) {
			if (!e1.equals(a[i]))
				return false;
			i++;
		}
		return true;
	}

	public static String ensureLinuxLineSep(@NotNull String input) {
		return input.replaceAll("\r\n?", "\n");
	}

	public static String ensureWindowsLineSep(@NotNull String input) {
		// Two replace passes are needed to avoid converting "\r\n" to "\r\r\n".
		return input.replaceAll("\r\n?", "\n").replace("\n", "\r\n");
	}

	/**
	 * Splits the given file path at any path separators, i.e. forward or
	 * backward slashes. Example:
	 *
	 * <pre>
	 * /path/to/file/ -> '', 'path', 'to', 'file'
	 * </pre>
	 *
	 * Note that a leading path separator will produce an empty string at the
	 * beginning of the returned list, while a (single) trailing path separator
	 * won't.
	 */
	@MutableCopy
	@NotNull
	public static List<String> splitPath(@NotNull String path) {
		List<String> parts = new ArrayList<String>();
		int lastStart = 0;
		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			if (c == '/' || c == '\\') {
				parts.add(path.substring(lastStart, i));
				lastStart = i + 1;
			}
		}
		if (lastStart < path.length())
			parts.add(path.substring(lastStart));
		return parts;
	}

	/**
	 * A {@link com.google.common.base.CharMatcher CharMatcher} that matches
	 * forward and backward slashes. See the {@code CharMatcher} Javadocs for
	 * more.
	 */
	public static final CharMatcher fileSepMatcher = CharMatcher.anyOf("/\\").precomputed();

	/**
	 * Creates a file path by joining the given parts. All leading and trailing
	 * forward and backward slashes are stripped from the parts, except for the
	 * first part, where only the trailing slashes are stripped. All backward
	 * slashes are replaced by forward slashes.
	 * <p>
	 * Special case: If only two path parts are given and one of them is empty,
	 * the other path part is returned, without any additional file separators.
	 */
	@NotNull
	public static String joinPath(	@NotNull String first,
									@NotNull String second,
									@NotNull String... more) {
		if (more.length == 0) {
			if (first.isEmpty())
				return second;
			if (second.isEmpty())
				return first;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(fileSepMatcher.trimTrailingFrom(first));
		sb.append('/');
		sb.append(fileSepMatcher.trimFrom(second));
		for (int i = 0; i < more.length; i++) {
			sb.append('/');
			sb.append(fileSepMatcher.trimFrom(more[i]));
		}
		return toForwardSlashes(sb.toString());
	}

	/**
	 * Same as {@link #joinPath(String...)}, but reads the parts from an
	 * <tt>Iterable</tt>.
	 */
	@NotNull
	public static String joinPath(@NotNull Iterable<?> parts) {
		Iterator<?> it = parts.iterator();
		if (! it.hasNext())
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(fileSepMatcher.trimTrailingFrom(it.next().toString()));
		while (it.hasNext()) {
			sb.append('/');
			sb.append(fileSepMatcher.trimFrom(it.next().toString()));
		}
		return toForwardSlashes(sb.toString());
	}

	@NotNull
	public static String join(@NotNull String separator, @NotNull Object... parts) {
		Util.checkNotNull(separator);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i == 0) {
				sb.append(parts[i]);
			} else {
				sb.append(separator);
				sb.append(parts[i]);
			}
		}
		return sb.toString();
	}

	@NotNull
	public static String join(@NotNull String separator, @NotNull Iterable<?> parts) {
		Util.checkNotNull(separator);
		Iterator<?> it = parts.iterator();
		if (! it.hasNext())
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(it.next().toString());
		while (it.hasNext()) {
			sb.append(separator);
			sb.append(it.next().toString());
		}
		return sb.toString();
	}

	/**
	 * For the given file, returns an absolute path in which all backward
	 * slashes have been replaced by forward slashes.
	 * <p>
	 * Exception: If the file's path is a UNC path, the UNC path is returned as
	 * is.
	 */
	@NotNull
	public static String getAbsPath(@NotNull File file) {
		String absPath = file.getAbsolutePath();
		if (absPath.startsWith("\\\\")) // UNC path?
			return absPath;
		/*
		 * We'll replace "//" with "/" here due to a bug in the
		 * File.getAbsolutePath method: On Windows, if the given file has the
		 * path "SOME_PATH" and the current working directory is the root of a
		 * device, e.g. "C:\", then getAbsolutePath will return "C:\\SOME_PATH"
		 * rather than the more sensible value "C:\SOME_PATH".
		 */
		return absPath.replace('\\', '/').replace("//", "/");
	}
	
	/**
	 * Returns whether given file's path is a UNC path.
	 */
	public static boolean isUncPath(@NotNull File file) {
		return file.getPath().startsWith("\\\\");
	}

	/**
	 * For the given path string, returns an absolute path in which all backward
	 * slashes have been replaced by forward slashes.
	 */
	@NotNull
	public static String getAbsPath(@NotNull String path) {
		return getAbsPath(new File(path));
	}

	@NotNull
	public static File getAbsFile(@NotNull File file) {
		return new File(getAbsPath(file));
	}

	/**
	 * Equivalent to {@link java.io.File#getAbsolutePath()}.
	 */
	@NotNull
	public static String getSystemAbsPath(@NotNull File file) {
		return file.getAbsolutePath();
	}

	/**
	 * Equivalent to {@link java.io.File#getAbsolutePath() new
	 * java.io.File(path).getAbsolutePath()}.
	 */
	@NotNull
	public static String getSystemAbsPath(@NotNull String path) {
		return new File(path).getAbsolutePath();
	}

	@NotNull
	public static File getCanonicalFile(@NotNull String path) {
		return getCanonicalFile(new File(path));
	}

	@NotNull
	public static File getCanonicalFile(@NotNull File file) {
		return new File(getCanonicalPath(file));
	}

	@NotNull
	public static String getCanonicalPath(@NotNull File file) {
		if (IS_WINDOWS && isWindowsDevice(file.getPath())) {
			String driveLetter = getDriveLetter(file.getPath());
			assert driveLetter != null;
			return driveLetter + ":\\"; // the trailing slash is important here
		}
		
		// Calling getCanonicalPath leads to performance problems for files
		// located on a network, so it has been disabled. See:
		// https://sourceforge.net/p/docfetcher/discussion/702424/thread/4ed68957/
		
//		try {
//			return file.getCanonicalPath();
//		}
//		catch (IOException e) {
//			return file.getAbsolutePath();
//		}
		return file.getAbsolutePath();
	}
	
	public static boolean isCanonicallyEqual(	@Nullable File file1,
												@Nullable File file2) {
		if (file1 == null || file2 == null)
			return false;
		return getCanonicalFile(file1).equals(getCanonicalFile(file2));
	}

	/**
	 * Returns all files and directories directly underneath the given
	 * directory. This works like {@link File#listFiles()}, except that when
	 * access to the directory is denied, an empty array is returned, not a null
	 * pointer.
	 */
	@NotNull
	public static File[] listFiles(@NotNull File dir) {
		File[] files = dir.listFiles();
		return files == null ? new File[0] : files;
	}

	/**
	 * Returns all files and directories directly underneath the given directory
	 * that are not filtered by the given {@code filter}. This works like
	 * {@link File#listFiles(FilenameFilter)}, except that when access to the
	 * directory is denied, an empty array is returned, not a null pointer.
	 */
	@NotNull
	public static File[] listFiles(	@NotNull File dir,
									@Nullable FilenameFilter filter) {
		File[] files = dir.listFiles(filter);
		return files == null ? new File[0] : files;
	}

	/**
	 * Returns all files and directories directly underneath the given directory
	 * that are not filtered by the given {@code filter}. This works like
	 * {@link File#listFiles(FileFilter)}, except that when access to the
	 * directory is denied, an empty array is returned, not a null pointer.
	 */
	@NotNull
	public static File[] listFiles(	@NotNull File dir,
									@Nullable FileFilter filter) {
		File[] files = dir.listFiles(filter);
		return files == null ? new File[0] : files;
	}

	/**
	 * Returns whether the given file is a symlink. Returns false if the file
	 * doesn't exists or if an IOException occured. The symlink detection is
	 * based on the comparison of the absolute and canonical path of a link: If
	 * those two differ, the given file can be assumed to be a symlink.
	 * <p>
	 * Note: If the given file is an instance of TFile and
	 * represents an archive entry, this method always returns false.
	 */
	public static boolean isSymLink(@NotNull File file) {
		try {
			/*
			 * Earlier versions simply compared the absolute and canonical path
			 * of the given file. This did not work for files with 8.3
			 * filenames, which were incorrectly identified as symlinks.
			 */
			File canon;
			if (file.getParent() == null) {
				canon = file;
			}
			else {
				File canonDir = file.getParentFile().getCanonicalFile();
				canon = new File(canonDir, file.getName());
			}
			return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
		}
		catch (IOException e) {
			return false;
		}
	}

	private interface Kernel32 extends Library {
		public int GetFileAttributesW(WString fileName);
	}

	private static Kernel32 lib = null;

	private static int getWin32FileAttributes(File file) throws IOException {
		if (lib == null) {
			synchronized (Kernel32.class) {
				lib = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
			}
		}
		/*
		 * It's important here to use the canonical path, because: (1)
		 * Non-canonical paths can be made arbitrarily long using "..", like
		 * this: "C:/Test/../Test/../Test/../Test". (2) GetFileAttributesW will
		 * return -1 if the path is as long as or longer than 260 characters.
		 * This can cause directories and files to be incorrectly identified as
		 * NTFS junctions, for example.
		 */
		String path;
		try {
			path = file.getCanonicalPath();
		}
		catch (IOException e) {
			path = file.getAbsolutePath();
		}
		return lib.GetFileAttributesW(new WString(path));
	}

	/**
	 * Returns whether the given file is a Windows junction or symlink. Returns
	 * false if the platform is not Windows, if the file doesn't exists or if an
	 * IOException occured.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>If the given file is an instance of TFile and represents an archive
	 * entry, this method always returns false.</li>
	 * <li>This method requires file system access, which can cause latencies if
	 * the file is accessed over a network.</li>
	 * </ul>
	 */
	public static boolean isJunctionOrSymlink(@NotNull File file) {
		if (! IS_WINDOWS)
			return false;
		try {
			// Wrap in java.io.File to shield against TFile instances
			return new File(file.getPath()).exists()
					&& (0x400 & getWin32FileAttributes(file)) != 0;
		}
		catch (IOException e) {
			return false;
		}
	}

	/**
	 * Returns the parent of the given file. Unlike the standard method
	 * {@link File#getParentFile()}, this method will not return null if the
	 * given file was constructed with a relative path.
	 */
	@NotNull
	public static File getParentFile(@NotNull File file) {
		Util.checkNotNull(file);
		File parent = file.getParentFile();
		if (parent == null)
			parent = file.getAbsoluteFile().getParentFile();
		return parent;
	}
	
	@NotNull
	public static String toForwardSlashes(@NotNull String path) {
		if (path.startsWith("\\\\")) // UNC path?
			return path;
		return path.replace('\\', '/');
	}

	/**
	 * @see #getParentFile(File)
	 */
	@NotNull
	public static File getParentFile(@NotNull String path) {
		Util.checkNotNull(path);
		File file = new File(path);
		File parent = file.getParentFile();
		if (parent == null)
			parent = file.getAbsoluteFile().getParentFile();
		return parent;
	}

	/**
	 * Returns true if <tt>objects</tt> contains an object that is equal to
	 * <tt>object</tt>. Returns false if <tt>objects</tt> is null.
	 */
	public static boolean containsEquality(	@Nullable Object[] objects,
											@Nullable Object object) {
		if (objects == null)
			return false;
		for (Object candidate : objects)
			if (candidate.equals(object))
				return true;
		return false;
	}

	/**
	 * Equivalent to {@link #splitFilename(String)
	 * splitFilename(file.getName())}.
	 *
	 * @see Util#splitFilename(String)
	 */
	public static String[] splitFilename(@NotNull File file) {
		return Util.splitFilename(file.getName());
	}

	/**
	 * Splits the given filename into a base name and the file extension,
	 * omitting the '.' character, e.g. "data.xml" -> ["data", "xml"]. The file
	 * extension is an empty string if the file has no extension (i.e. it
	 * doesn't contain the '.' character). The returned file extension is always
	 * lowercase, even if it wasn't lowercased in the given filename. It is also
	 * guaranteed that the returned array is always of length 2.
	 * <p>
	 * Exception: If the file ends with ".xxx.gz", then the returned file
	 * extension is "xxx.gz", not "gz". Examples:
	 * <ul>
	 * <li>"archive.tar.gz" -> ["archive", "tar.gz"]
	 * <li>"abiword.abw.gz" -> ["abiword", "abw.gz"]
	 * </ul>
	 * <p>
	 * Note: This method also accepts filepaths.
	 *
	 * @throws NullPointerException
	 *             if the given filename is null.
	 */
	@NotNull
	public static String[] splitFilename(@NotNull String filename) {
		int index = filename.lastIndexOf('.');
		if (index == -1)
			return new String[] {filename, ""};
		String ext = filename.substring(index + 1).toLowerCase();
		if (ext.equals("gz")) {
			int index2 = filename.lastIndexOf('.', index - 1);
			if (index2 != -1) {
				return new String[] {
					filename.substring(0, index2),
					filename.substring(index2 + 1).toLowerCase()
				};
			}
		}
		return new String[] {filename.substring(0, index), ext};
	}

	@NotNull
	public static String getExtension(@NotNull String filename) {
		return splitFilename(filename)[1];
	}

	@NotNull
	public static String getExtension(@NotNull File file) {
		return splitFilename(file.getName())[1];
	}

	/**
	 * For the given filename and a list of file extensions, this method returns
	 * true if any of the file extensions match the filename. A match occurs
	 * when the given filename, after being lower-cased, ends with '.' and the
	 * matching lower-cased file extension.
	 * <p>
	 * Example: The filename <code>'some_file.TXT'</code> matches the file
	 * extension <code>'txt'</code>.
	 * <p>
	 * Note: This method also accepts filepaths.
	 */
	public static boolean hasExtension(	@NotNull String filename,
										@NotNull String... extensions) {
		filename = filename.toLowerCase();
		for (String ext : extensions)
			if (filename.endsWith("." + ext.toLowerCase()))
				return true;
		return false;
	}

	/**
	 * @see #hasExtension(String, String...)
	 */
	public static boolean hasExtension(@NotNull String filename,
	                                   @NotNull Collection<String> extensions) {
		filename = filename.toLowerCase();
		for (String ext : extensions)
			if (filename.endsWith("." + ext.toLowerCase()))
				return true;
		return false;
	}

	/**
	 * Deletes all the files within a directory. Does not delete the directory
	 * itself.
	 * <p>
	 * If the file argument is a symbolic link or there is a symbolic link in
	 * the path leading to the directory, this method will do nothing. Symbolic
	 * links within the directory are not followed.
	 */
	public static void deleteContents(@NotNull File directory)
			throws IOException {
		checkThat(directory.isDirectory());
		if (isSymLink(directory))
			return;
		for (File file : listFiles(directory))
			deleteRecursively(file);
	}

	/**
	 * Deletes a file or directory and all contents recursively.
	 * <p>
	 * If the file argument is a symbolic link the link will be deleted but not
	 * the target of the link. If the argument is a directory, symbolic links
	 * within the directory will not be followed.
	 */
	public static void deleteRecursively(@NotNull File file) throws IOException {
		if (file.isDirectory())
			deleteContents(file);
		if (!file.delete())
			throw new IOException("Failed to delete " + file);
	}

	/**
	 * Returns the name of the given file. In contrast to the default
	 * {@link File#getName()} method, this method will return a drive letter
	 * instead of an empty string if the given file is a Windows root such as
	 * "C:". The {@code letterSuffix} argument is a string that will be appended
	 * to the drive letter, if one is returned.
	 */
	@NotNull
	public static String getNameOrLetter(	@NotNull File file,
											@NotNull String letterSuffix) {
		Util.checkNotNull(file, letterSuffix);
		String filename = file.getName();

		/*
		 * Special case: If the file was created as 'new File("")', then its
		 * filename will be an empty string.
		 */
		if (file.getAbsoluteFile().equals(USER_DIR))
			return USER_DIR.getName();

		/*
		 * Note: Do not use absolute files here, because this would turn "C:"
		 * into the working directory! (Strange but true.)
		 */
		if (IS_WINDOWS && filename.length() == 0 && getParentFile(file) == null) {
			String driveLetter = getDriveLetter(file.getPath());
			if (driveLetter != null)
				return driveLetter + letterSuffix;
		}
		return filename;
	}

	private static Pattern drivePattern = Pattern.compile("([a-zA-Z]):.*");
	private static Pattern driveOnlyPattern = Pattern.compile("(?:[a-zA-Z]):(?:\\\\|/)*");

	/**
	 * Returns the drive letter at the beginning of the given Windows path, or
	 * null if the path doesn't start with a drive letter.
	 * <p>
	 * Example: For "C:\Windows" this method returns "C".
	 */
	@Nullable
	public static String getDriveLetter(@NotNull String path) {
		Util.checkNotNull(path);
		Matcher m = Util.drivePattern.matcher(path);
		if (m.matches())
			return m.group(1).toUpperCase();
		return null;
	}

	public static boolean isWindowsDevice(@NotNull String path) {
		return driveOnlyPattern.matcher(path).matches();
	}

	/**
	 * Throws an <code>IllegalArgumentException</code> if the given condition is
	 * false.
	 */
	public static void checkThat(boolean condition) {
		if (!condition)
			throw new IllegalArgumentException();
	}

	/**
	 * Throws an <code>IllegalArgumentException</code> with the given error
	 * message if the given condition is false.
	 */
	public static void checkThat(boolean condition, @NotNull String message) {
		if (!condition)
			throw new IllegalArgumentException(message);
	}

	/**
	 * Throws an <code>IllegalArgumentException</code> if the provided argument
	 * is null. If not, the argument is returned.
	 */
	public static <T> T checkNotNull(T a) {
		/*
		 * Generally, it does not make sense to check that a method argument of
		 * type Boolean is not null - if the Boolean is not allowed to be null,
		 * one could use a primitive boolean instead. If someone does call this
		 * method with a Boolean, he/she might have done so by accident by
		 * confusing checkNotNull with checkThat. To prevent this, we'll throw
		 * an exception.
		 */
		if (a instanceof Boolean)
			throw new UnsupportedOperationException();
		if (a == null)
			throw new IllegalArgumentException();
		return a;
	}

	/**
	 * Throws an <code>IllegalArgumentException</code> if any of the provided
	 * arguments is null.
	 */
	public static void checkNotNull(Object a, Object b) {
		if (a == null || b == null)
			throw new IllegalArgumentException();
	}

	/**
	 * Throws an <code>IllegalArgumentException</code> if any of the provided
	 * arguments is null.
	 */
	public static void checkNotNull(Object a, Object b, Object c) {
		if (a == null || b == null || c == null)
			throw new IllegalArgumentException();
	}

	/**
	 * Throws an <code>IllegalArgumentException</code> if any of the provided
	 * arguments is null.
	 */
	public static void checkNotNull(Object a, Object b, Object c, Object d) {
		if (a == null || b == null || c == null || d == null)
			throw new IllegalArgumentException();
	}

	/**
	 * Throws an <code>IllegalArgumentException</code> if any of the provided
	 * arguments is null.
	 */
	public static void checkNotNull(Object a, Object b, Object c, Object d, Object e) {
		if (a == null || b == null || c == null || d == null || e == null)
			throw new IllegalArgumentException();
	}
	
	/**
	 * Returns the given string if it is not null, otherwise returns an empty
	 * string.
	 */
	@NotNull
	public static String notNull(@Nullable String string) {
		return string == null ? "" : string;
	}

	private static long lastTimeStamp = -1;

	/**
	 * Returns a unique identifier based on {@link System#currentTimeMillis()}.
	 * The returned ID is guaranteed to differ from all previous IDs obtained by
	 * this method.
	 */
	@ThreadSafe
	public static synchronized long getTimestamp() {
		/*
		 * Try to create a timestamp and don't return until the last timestamp
		 * and the current one are unequal.
		 */
		long newTimeStamp = System.currentTimeMillis();
		while (newTimeStamp == lastTimeStamp)
			newTimeStamp = System.currentTimeMillis();
		lastTimeStamp = newTimeStamp;
		return newTimeStamp;
	}

	/**
	 * Returns true if the directory given by <tt>dir</tt> is a direct or
	 * indirect parent directory of the file or directory given by
	 * <tt>fileOrDir</tt>.
	 */
	public static boolean contains(	@NotNull File dir,
									@NotNull File fileOrDir) {
		return contains(getAbsPath(dir), getAbsPath(fileOrDir));
	}

	/**
	 * Returns true if the directory given by the absolute path <tt>dirPath</tt>
	 * is a direct or indirect parent directory of the file or directory given
	 * by the absolute path <tt>fileOrDirPath</tt>.
	 */
	public static boolean contains(	@NotNull String dirPath,
									@NotNull String fileOrDirPath) {
		dirPath = dirPath.replace('\\', '/');
		fileOrDirPath = fileOrDirPath.replace('\\', '/');
		if (dirPath.length() >= fileOrDirPath.length())
			return false;
		char c = fileOrDirPath.charAt(dirPath.length());
		if (c != '/')
			return false;
		if (! fileOrDirPath.startsWith(dirPath))
			return false;
		return true;
	}

	/**
	 * Returns the last element of the given list. Returns null if the given
	 * list is empty or null.
	 */
	@Nullable
	public static <T> T getLast(@Nullable List<T> list) {
		if (list == null) return null;
		int size = list.size();
		if (size == 0) return null;
		return list.get(size - 1);
	}

	@MutableCopy
	@NotNull
	public static <T> List<T> createList(	int extraCapacity,
											@NotNull T... elements) {
		Util.checkNotNull(elements);
		List<T> newList = new ArrayList<T>(elements.length + extraCapacity);
		for (T element : elements)
			newList.add(element);
		return newList;
	}

	/**
	 * Creates a new list from the given collection and elements. The given
	 * collection is added first to the returned list.
	 *
	 * @see #createListReverse(Collection, Object...)
	 */
	@MutableCopy
	@NotNull
	public static <T> List<T> createList(	@NotNull Collection<? extends T> col,
											@NotNull T... elements) {
		Util.checkNotNull(col, elements);
		List<T> newList = new ArrayList<T>(col.size() + elements.length);
		newList.addAll(col);
		for (T element : elements)
			newList.add(element);
		return newList;
	}

	/**
	 * Creates a new list from the given collection and elements. The given
	 * elements are added first to the returned list.
	 *
	 * @see #createList(Collection, Object...)
	 */
	@MutableCopy
	@NotNull
	public static <T> List<T> createListReversed(	@NotNull Collection<? extends T> col,
													@NotNull T... elements) {
		Util.checkNotNull(col, elements);
		List<T> newList = new ArrayList<T>(col.size() + elements.length);
		for (T element : elements)
			newList.add(element);
		newList.addAll(col);
		return newList;
	}

	@NotNull
	public static <T> List<T> createEmptyList(@NotNull Collection<?>... cols) {
		int size = 0;
		for (int i = 0; i < cols.length; i++)
			size += cols[i].size();
		return new ArrayList<T>(size);
	}


	@NotNull
	public static File createTempDir() throws IOException {
		File dir = Files.createTempDir();

		/*
		 * On Windows and Mac OS X, returning a canonical file avoids certain
		 * symlink-related issues:
		 *
		 * On Windows 7, Files.createTempDir() might return a directory that
		 * contains 8.3 filenames, for which the absolute and canonical paths
		 * differ, so that the directory will be incorrectly treated as a
		 * symlink. On Mac OS X, Files.createTempDir() will return an actual
		 * symlink.
		 *
		 * In both cases, returning a file that is or appears to be a symlink
		 * will lead to various problems, e.g. Files.deleteRecursively(File)
		 * failing to delete the temporary directory.
		 */
		return IS_WINDOWS || IS_MAC_OS_X ? dir.getCanonicalFile() : dir;
	}

	/**
	 * Equivalent to {@link #createTempFile(String, String, File)
	 * createTempFile(String, String, null)}.
	 */
	public static File createTempFile(	@NotNull String prefix,
										@Nullable String suffix)
			throws IOException {
		return createTempFile(prefix, suffix, null);
	}

	/**
	 * Equivalent to {@link File#createTempFile(String, String, File)}, except:
	 * <ul>
	 * <li>The returned file will be deleted automatically after JVM shutdown.
	 * <li>Unlike {@link File#createTempFile(String, String, File)}, this method
	 * will not throw an exception if the prefix is shorter than 3 characters.
	 * Instead, the prefix will be right-padded with underscores to make it 3
	 * characters long.
	 * </ul>
	 *
	 * @see {@link File#createTempFile(String, String, File)}
	 */
	public static File createTempFile(	@NotNull String prefix,
										@Nullable String suffix,
										@Nullable File directory)
			throws IOException {
		int prefixLength = prefix.length();
		if (prefixLength < 3)
			prefix += Strings.repeat("_", 3 - prefixLength);
		File file = File.createTempFile(prefix, suffix, directory);

		/*
		 * On Mac OS X, File.createTempFile() will give us a symlink to a file,
		 * which is not what we want, because our file walker will silently
		 * ignore symlinks. The workaround is to return a canonical file on Mac
		 * OS X.
		 */
		if (Util.IS_MAC_OS_X)
			file = file.getCanonicalFile();

		file.deleteOnExit();
		return file;
	}

	@NotNull
	public static File createDerivedTempFile(	@NotNull String filename,
												@NotNull File tempDir)
			throws IOException {
		String[] nameParts = Util.splitFilename(filename);
		if (! nameParts[1].equals(""))
			nameParts[1] = "." + nameParts[1];
		return Util.createTempFile(
				nameParts[0], nameParts[1], tempDir
		);
	}

	public static void println(@NotNull Object... objects) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object object : objects) {
			if (first) {
				first = false;
			} else {
				sb.append("; ");
			}
			sb.append(object);
		}
		System.out.println(sb.toString());
	}

	/**
	 * Equivalent to <code>System.err.println(String)</code>. This method can be
	 * called instead to suppress AspectJ warnings.
	 */
	public static void printErr(@NotNull String message) {
		System.err.println(message);
	}

	/**
	 * Equivalent to {@link Throwable#printStackTrace()}. This method can be
	 * called instead to suppress AspectJ warnings.
	 */
	public static void printErr(@NotNull Throwable t) {
		t.printStackTrace();
	}

	@NotNull
	public static String getLowestMessage(@Nullable Throwable throwable) {
		if (throwable == null)
			return "";
		List<Throwable> chain = Throwables.getCausalChain(throwable);
		for (Throwable t : Lists.reverse(chain)) {
			String msg = t.getMessage();
			if (msg != null && !msg.trim().equals(""))
				return msg;
		}
		return "";
	}

	public static int clamp(int value, int minimum, int maximum) {
		Util.checkThat(minimum <= maximum);
		if (value > maximum) return maximum;
		if (value < minimum) return minimum;
		return value;
	}

	public static boolean isInterrupted() {
		return Thread.currentThread().isInterrupted();
	}

	/**
	 * Launches the given filepath and select the file
	 * explorer.exe /select,F:\docfetcher\DocFetcher\aspectjtools.jar
	 * returning whether the file was successfully launched
     * This process is not working properly in Win7 64bit
	 */
	public static boolean winOpenDir(String fileName) {
        if (IS_WINDOWS) {
            try {
                Runtime.getRuntime().exec("explorer /select, " + fileName);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
	}
	
	/**
	 * Return the original index name, which is based on the
	 * root folder of the index
	 */
	public static String getDefaultIndexName(File rootFile){
		String nameOrLetter = Util.getNameOrLetter(rootFile, ":\\");
		return (Util.truncate(nameOrLetter));
	}

}
