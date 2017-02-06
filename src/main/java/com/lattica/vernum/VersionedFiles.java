package com.lattica.vernum;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = { "originalName" })
@ToString
public class VersionedFiles implements Comparable<VersionedFiles> {
	private final String baseName;
	private final String ext;

	private final String originalName;
	private final SortedSet<VersionedFile> versionedFiles;

	public VersionedFiles(VersionedFile vf) {
		this.baseName = vf.getParts().getBaseName();
		this.ext = vf.getParts().getExtensions();
		this.originalName = vf.getOriginalName();
		this.versionedFiles = new TreeSet<VersionedFile>();
		this.versionedFiles.add(vf);
	}

	boolean addIfSameOriginal(VersionedFile vf) {
		if (!equalsOnSystem(originalName, vf.getOriginalName())) {
			return false;
		}
		versionedFiles.add(vf);
		return true;
	}

	public VersionedFile getLatest() {
		return versionedFiles != null && versionedFiles.size() > 0 ? versionedFiles.first() : null;
	}

	public VersionNumber getLatestVersion() {
		VersionedFilename latest = getLatest();
		return latest != null ? latest.getParts().getVersion() : null;
	}

	public File getLatestFile() {
		VersionedFile latest = getLatest();
		return latest != null ? latest.getFile() : null;
	}

	public boolean isOriginal() {
		VersionedFilename file = getLatest();
		return file != null && file.isOriginal();
	}

	@Override
	public int compareTo(VersionedFiles o) {
		return this.originalName.compareTo(o.originalName);
	}

	/**
	 * Checks whether two filenames are equal using the case rules of the
	 * system.
	 * <p>
	 * No processing is performed on the filenames other than comparison. The
	 * check is case-sensitive on Unix and case-insensitive on Windows.
	 *
	 * @param filename1
	 *            the first filename to query, may be null
	 * @param filename2
	 *            the second filename to query, may be null
	 * @return true if the filenames are equal, null equals null
	 */
	public static boolean equalsOnSystem(final String filename1, final String filename2) {
		return equals(filename1, filename2, false, null);
	}

	/**
	 * Checks whether two filenames are equal, optionally normalizing and
	 * providing control over the case-sensitivity.
	 *
	 * @param filename1
	 *            the first filename to query, may be null
	 * @param filename2
	 *            the second filename to query, may be null
	 * @param normalized
	 *            whether to normalize the filenames
	 * @param caseSensitivity
	 *            what case sensitivity rule to use, null means case-sensitive
	 * @return true if the filenames are equal, null equals null
	 * @since 1.3
	 */
	public static boolean equals(String filename1, String filename2, final boolean normalized,
			Boolean caseSensitivity) {

		if (filename1 == null || filename2 == null) {
			return filename1 == null && filename2 == null;
		}
		if (normalized) {
			if (filename1 == null || filename2 == null) {
				throw new NullPointerException("Error normalizing one or both of the file names");
			}
		}

		boolean sensitive;
		if (caseSensitivity == null) {
			String OS = System.getProperty("os.name").toLowerCase();
			if (OS.startsWith("windows")) {
				sensitive = false;
			} else if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
				sensitive = false;
			} else {
				sensitive = true;
			}
		} else {
			sensitive = caseSensitivity.booleanValue();
		}

		return sensitive ? filename1.equals(filename2) : filename1.equalsIgnoreCase(filename1);
	}

	public static Map<String, VersionedFiles> listDirWithVersionedFilenames(File dir) {
		return listDirWithVersionedFilenames(dir, null);
	}

	public static Map<String, VersionedFiles> listDirWithVersionedFilenames(File dir, FilenameFilter filter) {
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return Collections.emptyMap();
		}

		SortedMap<String, VersionedFiles> map = new TreeMap<String, VersionedFiles>();
		for (File file : files) {
			VersionedFile vf = new VersionedFile(file);
			if (filter != null && !filter.accept(dir, vf.getOriginalName())) {
				continue;
			}
			VersionedFiles fvs = map.get(vf.getOriginalName());
			if (fvs == null || !fvs.addIfSameOriginal(vf)) {
				map.put(vf.getOriginalName(), new VersionedFiles(vf));
			}
		}
		return map;
	}
}
