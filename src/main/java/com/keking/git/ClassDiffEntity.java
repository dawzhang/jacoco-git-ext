package com.keking.git;

import java.util.Map;

/**
 * 类的git diff 信息
 */
public class ClassDiffEntity {

	private String fileName;
	private String filePath;
	private String changeType;
	private Map<Integer, Integer> changeDetails;

	public ClassDiffEntity(String fileName, String filePath, String changeType, Map<Integer, Integer> changeDetails) {
		super();
		this.fileName = fileName;
		this.filePath = filePath;
		this.changeType = changeType;
		this.changeDetails = changeDetails;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getChangeType() {
		return changeType;
	}

	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}

	public Map<Integer, Integer> getChangeDetails() {
		return changeDetails;
	}

	public void setChangeDetails(Map<Integer, Integer> changeDetails) {
		this.changeDetails = changeDetails;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public String toString() {
		return "FileDiffEntity [fileName=" + fileName + ", filePath=" + filePath + ", changeType=" + changeType + ", changeDetails=" + changeDetails + "]";
	}

}
