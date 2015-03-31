package com.rodneyg.tunesshare;

public class DirEntry extends Entry {

	private static final long serialVersionUID = 20130521L;

	public DirEntry(String entryName, String path) {
		super(entryName, path);
	}

	@Override
	public boolean isDirectory() {
		return true;
	}
}
