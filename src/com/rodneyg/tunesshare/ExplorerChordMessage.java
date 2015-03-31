package com.rodneyg.tunesshare;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ExplorerChordMessage implements Serializable {

	public static final String DIRECTORY = "DIRECTORY";
	public static final String LISTING = "LISTING";
	public static final String ERROR_MSG = "ERROR_MSG";
	public static final String REQUESTED_FILE = "REQUESTED_FILE";

	private static final long serialVersionUID = 20130520L;

	private final MessageType mType;
	private final Map<String, Object> mPayload;

	public ExplorerChordMessage(MessageType type) {
		mType = type;
		mPayload = new HashMap<String, Object>();
	}

	byte[] getBytes() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final ObjectOutputStream os;

		try {
			os = new ObjectOutputStream(out);
			os.writeObject(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return out.toByteArray();
	}

	public MessageType getType() {
		return mType;
	}

	public void putString(String key, String value) {
		mPayload.put(key, value);
	}

	public void putObject(String key, Object value) {
		mPayload.put(key, value);
	}

	public String getString(String key) {
		return (String) mPayload.get(key);
	}

	public Object getObject(String key) {
		return mPayload.get(key);
	}

	public static ExplorerChordMessage obtainChordMessage(byte[] data) {
		final ByteArrayInputStream in = new ByteArrayInputStream(data);
		final ObjectInputStream is;
		ExplorerChordMessage message = null;

		try {
			is = new ObjectInputStream(in);
			message = (ExplorerChordMessage) is.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return message;
	}

	public enum MessageType {
		// @formatter:off
		LISTING_REQUEST,
		LISTING,
		FILE_REQUEST,
		ERROR;
		// @formatter:on

		@Override
		public String toString() {
			return name();
		}
	}

}
