package com.rodneyg.tunesshare;

import java.io.Serializable;

public abstract class Entry implements Serializable {

	private static final long serialVersionUID = 20130521L;
	private final String mEntryName;
	private final String mPath;

	public Entry(String entryName, String path) {
		mEntryName = entryName;
		mPath = path;
	}

	public abstract boolean isDirectory();

	public String getEntryName() {
		return mEntryName;
	}

	public String getPath() {
		return mPath;
	}

}
