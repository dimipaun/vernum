package com.lattica.vernum;

import java.text.ParseException;
import java.util.Comparator;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Getter @Slf4j
@EqualsAndHashCode(of = { "major", "minor", "revision", "suffix" })
public class VersionNumber implements Comparable<VersionNumber> {
	public static Comparator<VersionNumber> COMPARATOR = VersionNumber::compareTo;

	private int major;
	private int minor;
	private Integer revision;
	private String suffix;

	public VersionNumber(int major, int minor, Integer revision, String suffix) {
		this.major = major;
		this.minor = minor;
		this.revision = revision;
		this.suffix = suffix;
	}

	public VersionNumber(@NonNull String version) throws ParseException {
		this(version, '.');
	}

	public VersionNumber(@NonNull String version, char sep) throws ParseException {
		if (version.length() == 0)
			throw new ParseException("Empty version", 0);
		String[] parts = version.split(Pattern.quote(String.valueOf(sep)), 3);
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
							} else {
								revision = 0;
							}
						} catch (NumberFormatException ex) {
							int i = 0;
							for (; i < last.length(); i++) {
								char c = last.charAt(i);
								if (!Character.isDigit(c)) {
									break;
								}
							}
							if (i > 0) {
								revision = Integer.parseInt(last.substring(0, i));
							}
							if (i < last.length()) {
								suffix = last.substring(i);
							}
						}
					}
				}
			} catch (NumberFormatException ex) {
				throw new ParseException("Empty version number: " + version + " (" + ex.getMessage() + ")", 0);
			}
		} else {
			throw new java.text.ParseException("Empty version number: " + version, 0);
		}
	}

	@NonNull
	public VersionNumber nextRevision() {
		if (revision != null) {
			return new VersionNumber(major, minor, revision + 1, suffix);
		} else {
			return new VersionNumber(major, minor + 1, null, suffix);
		}
	}

	@NonNull
	public VersionNumber prevRevision() throws ParseException {
		if (revision != null && revision > 0) {
			return new VersionNumber(major, minor, revision - 1, suffix);
		} else if (minor > 0) {
			return new VersionNumber(major, minor - 1, revision, suffix);
		} else if (major > 0) {
			return new VersionNumber(major - 1, minor, revision, suffix);
		} else {
			throw new ParseException("Can't set previous version " + toString(), 0);
		}
	}

	@Override
	public int compareTo(@NonNull VersionNumber o) {
		if (major != o.major)
			return major - o.major;
		if (minor != o.minor)
			return minor - o.minor;
		if (revision != null) {
			if (o.revision != null) {
				if (revision != o.revision)
					return revision - o.revision;
			} else {
				return 1;
			}
		} else if (o.revision != null) {
			return -1;
		}
		if (suffix != null) {
			return o.suffix != null ? suffix.compareTo(o.suffix) : 1;
		}
		return 0;
	}

	@Override
	@NonNull
	public String toString() {
		return major + "." + minor + (revision != null ? "." + revision : "") + (suffix != null ? suffix : "");
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
}