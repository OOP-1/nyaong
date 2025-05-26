package org.example.service.refactoring;

import org.example.utils.HashUtil;

public class MessageHashService {
	public String calculateHash(String content) {
		return HashUtil.sha256(content);
	}

	public boolean verifyHash(String content, String expectedHash) {
		return expectedHash.equals(HashUtil.sha256(content));
	}
}
