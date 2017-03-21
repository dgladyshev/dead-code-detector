package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.entity.Check;
import com.dgladyshev.deadcodedetector.entity.CheckStatus;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchCheckException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CheckCodeService {

	private ConcurrentHashMap<String, Check> checks = new ConcurrentHashMap<>();

	@Async
	public void checkCode(String checkId) {
		Check check = checks.get(checkId);
		//TODO implement
	}

	public Check createCheck(String url, String language) {
		String checkId = java.util.UUID.randomUUID().toString(); //TODO generate unique id
		checks.put(
				checkId,
				Check.builder()
						.checkId(checkId)
						.repoUrl(url)
						.repoLanguage(language)
						.timeAdded(System.currentTimeMillis())
						.checkStatus(CheckStatus.ADDED)
						.build()
		);
		return checks.get(checkId);
	}

	public Map<String, Check> getChecks() {
		return checks;
	}

	public Check getCheckById(String id) throws NoSuchCheckException {
		if (checks.containsKey(id)) {
			return checks.get(id);
		} else {
			throw new NoSuchCheckException();
		}
	}

}
