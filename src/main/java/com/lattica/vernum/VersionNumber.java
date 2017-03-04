package com.lattica.vernum;

import java.text.ParseException;
import java.util.Comparator;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@Slf4j
public class VersionNumber implements Comparable<VersionNumber> {
	public static Comparator<VersionNumber> COMPARATOR = new Comparator<VersionNumber>() {
		@Override
		public int compare(VersionNumber o1, VersionNumber o2) {
			return o1.compareTo(o2);
		}
	};

	private int major;
	private int minor;
	private int revision;
	private String suffix;

	public VersionNumber(int major, int minor, int revision) {
		this.major = major;
		this.minor = minor;
		this.revision = revision;
	}

	public VersionNumber(String version) throws ParseException {
		this(version, '.');
	}

	public VersionNumber(String version, char sep) throws ParseException {
		if (version == null)
			throw new NullPointerException("Invalid version");
		if (version.length() == 0)
			throw new IllegalArgumentException("Empty version");
		String[] parts = version.split(Pattern.quote(String.valueOf(sep)));
		if (parts.length > 0) {
			try {
				major = Integer.parseInt(parts[0]);
				if (parts.length > 1) {
					minor = Integer.parseInt(parts[1]);
					if (parts.length > 2) {
						String last = parts[2];
						try {
							if (last.length() > 0) {
								revision = Integer.parseInt(last);
							}
						} catch (NumberFormatException ex) {
							int i = 0;
							while (i < last.length() && Character.isDigit(last.indexOf(i))) i++;
							if (i > 0) {
								revision = Integer.parseInt(last.substring(0, i));
							}
							if (i < last.length()) {
								suffix = last.substring(i);
							}
						}
						if (parts.length > 3) {
							throw new java.text.ParseException("version not well formatted: " + version, 0);
						}
					}
				}
			} catch (NumberFormatException ex) {
				throw new java.text.ParseException("Empty version number: " + version + " (" + ex.getMessage() + ")",
						0);
			}
		} else {
			throw new java.text.ParseException("Empty version number: " + version, 0);
		}
	}

	public VersionNumber nextRevision() {
		return new VersionNumber(major, minor, revision + 1);
	}

	public VersionNumber prevRevision() throws ParseException {
		if (revision > 0)
			return new VersionNumber(major, minor, revision - 1);
		else if (minor > 0)
			return new VersionNumber(major, minor - 1, revision);
		else if (major > 0)
			return new VersionNumber(major - 1, minor, revision);
		else
			throw new java.text.ParseException("Can't set previous version " + toString(), 0);
	}

	@Override
	public int compareTo(VersionNumber o) {
		if (o == null)
			return 1;
		if (major != o.major)
			return major - o.major;
		if (minor != o.minor)
			return minor - o.minor;
		if (revision != o.revision)
			return revision - o.revision;
		return 0;
	}

	@Override
	public String toString() {
		return major + "." + minor + "." + revision + (suffix != null ? suffix : "");
	}

	public static VersionNumber valueOf(String value) throws ParseException {
		if (value == null || value.length() == 0)
			return null;
		value = value.trim();
		if (value.equals("null"))
			return null;
		return value.length() == 0 ? null : new VersionNumber(value);
	}

	public static VersionNumber optValueOf(String value) {
		try {
			return valueOf(value);
		} catch (ParseException ex) {
			log.warn("Unknown version: " + value);
			// carry on
			return null;
		}
	}

	public static VersionNumber fromFilename(String filename) {
		FilenameParts parts = new FilenameParts(filename);
		return parts.getVersion();
	}

}
