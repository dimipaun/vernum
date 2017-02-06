package com.lattica.vernum;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = { "filename" })
public class FilenameParts {
	public static final char EXTENSION_SEPARATOR = '.';
	private static final char UNIX_SEPARATOR = '/';
	private static final char WINDOWS_SEPARATOR = '\\';

	public static final String VERSIONED_FILENAME_REGEX = "(.*)(-([0-9]+[.][0-9]+[.][0-9]+))(([.][a-zA-Z][a-zA-Z0-9]{0,9})*)";
	public static final Pattern VERSIONED_FILENAME_PAT = Pattern.compile(VERSIONED_FILENAME_REGEX);

	private String filename;
	private String baseName;
	private VersionNumber version;
	private String extension;
	private String extensions;

	public FilenameParts(String filename) {
		this.filename = filename;
		try {
			Matcher m = VERSIONED_FILENAME_PAT.matcher(filename);
			if (m.matches()) {
				baseName = m.group(1);
				version = VersionNumber.valueOf(m.group(3));
				extensions = m.group(4);
				if (extensions != null && extensions.startsWith(".")) {
					extensions = extensions.substring(1);
				}
				extension = getExtension(extensions);
				if (extension.length() == 0) {
					extension = extensions;
				}
			} else {
				throw new ParseException("Not a versioned file", 0);
			}
		} catch (ParseException ex) {

			baseName = getBaseName(filename);
			version = null;
			extension = getExtension(filename);
			extensions = extension;
			if (baseName.contains(".")) {
				int i = baseName.indexOf(".");
				extensions = baseName.substring(i + 1);
				baseName = baseName.substring(0, i);
			}
		}
	}

	/**
	 * Returns the index of the last directory separator character.
	 * <p>
	 * This method will handle a file in either Unix or Windows format. The
	 * position of the last forward or backslash is returned.
	 * <p>
	 * The output will be the same irrespective of the machine that the code is
	 * running on.
	 *
	 * @param filename
	 *            the filename to find the last path separator in, null returns
	 *            -1
	 * @return the index of the last separator character, or -1 if there is no
	 *         such character
	 */
	public static int indexOfLastSeparator(final String filename) {
		if (filename == null) {
			return -1;
		}
		final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
		final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
		return Math.max(lastUnixPos, lastWindowsPos);
	}

	/**
	 * Returns the index of the last extension separator character, which is a
	 * dot.
	 * <p>
	 * This method also checks that there is no directory separator after the
	 * last dot. To do this it uses {@link #indexOfLastSeparator(String)} which
	 * will handle a file in either Unix or Windows format.
	 * <p>
	 * The output will be the same irrespective of the machine that the code is
	 * running on.
	 *
	 * @param filename
	 *            the filename to find the last path separator in, null returns
	 *            -1
	 * @return the index of the last separator character, or -1 if there is no
	 *         such character
	 */
	public static int indexOfExtension(final String filename) {
		if (filename == null) {
			return -1;
		}
		final int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
		final int lastSeparator = indexOfLastSeparator(filename);
		return lastSeparator > extensionPos ? -1 : extensionPos;
	}

	// -----------------------------------------------------------------------
	/**
	 * Removes the extension from a filename.
	 * <p>
	 * This method returns the textual part of the filename before the last dot.
	 * There must be no directory separator after the dot.
	 * 
	 * <pre>
	 * foo.txt    --&gt; foo
	 * a\b\c.jpg  --&gt; a\b\c
	 * a\b\c      --&gt; a\b\c
	 * a.b\c      --&gt; a.b\c
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is
	 * running on.
	 *
	 * @param filename
	 *            the filename to query, null returns null
	 * @return the filename minus the extension
	 */
	public static String removeExtension(final String filename) {
		if (filename == null) {
			return null;
		}
		final int index = indexOfExtension(filename);
		if (index == -1) {
			return filename;
		} else {
			return filename.substring(0, index);
		}
	}

	/**
	 * Gets the base name, minus the full path and extension, from a full
	 * filename.
	 * <p>
	 * This method will handle a file in either Unix or Windows format. The text
	 * after the last forward or backslash and before the last dot is returned.
	 * 
	 * <pre>
	 * a/b/c.txt --&gt; c
	 * a.txt     --&gt; a
	 * a/b/c     --&gt; c
	 * a/b/c/    --&gt; ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is
	 * running on.
	 *
	 * @param filename
	 *            the filename to query, null returns null
	 * @return the name of the file without the path, or an empty string if none
	 *         exists
	 */
	public static String getBaseName(final String filename) {
		return removeExtension(getName(filename));
	}

	/**
	 * Gets the name minus the path from a full filename.
	 * <p>
	 * This method will handle a file in either Unix or Windows format. The text
	 * after the last forward or backslash is returned.
	 * 
	 * <pre>
	 * a/b/c.txt --&gt; c.txt
	 * a.txt     --&gt; a.txt
	 * a/b/c     --&gt; c
	 * a/b/c/    --&gt; ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is
	 * running on.
	 *
	 * @param filename
	 *            the filename to query, null returns null
	 * @return the name of the file without the path, or an empty string if none
	 *         exists
	 */
	public static String getName(final String filename) {
		if (filename == null) {
			return null;
		}
		final int index = indexOfLastSeparator(filename);
		return filename.substring(index + 1);
	}

	/**
	 * Gets the extension of a filename.
	 * <p>
	 * This method returns the textual part of the filename after the last dot.
	 * There must be no directory separator after the dot.
	 * 
	 * <pre>
	 * foo.txt      --&gt; "txt"
	 * a/b/c.jpg    --&gt; "jpg"
	 * a/b.txt/c    --&gt; ""
	 * a/b/c        --&gt; ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is
	 * running on.
	 *
	 * @param filename
	 *            the filename to retrieve the extension of.
	 * @return the extension of the file or an empty string if none exists or
	 *         {@code null} if the filename is {@code null}.
	 */
	public static String getExtension(final String filename) {
		if (filename == null) {
			return null;
		}
		final int index = indexOfExtension(filename);
		if (index == -1) {
			return "";
		} else {
			return filename.substring(index + 1);
		}
	}

	public String getOriginalName() {
		return baseName + (extensions != null ? "." + extensions : "");
	}
}