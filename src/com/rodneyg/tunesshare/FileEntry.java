package com.rodneyg.tunesshare;

public class FileEntry extends Entry {

	private static final long serialVersionUID = 20130521L;

	public FileEntry(String entryName, String path) {
		super(entryName, path);
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

}
