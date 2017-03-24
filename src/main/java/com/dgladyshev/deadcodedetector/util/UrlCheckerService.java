package com.dgladyshev.deadcodedetector.util;

import com.dgladyshev.deadcodedetector.exceptions.MalformedRepositoryUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class UrlCheckerService {

    @Autowired
    private RestTemplate restTemplate;

    public void checkAccessibility(String url) throws MalformedRepositoryUrl {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return;
            }
        } catch (RestClientException e) {
            log.error("Exception occurred while checking accessibility of given URL. Error: {}", e);
        }
        throw new MalformedRepositoryUrl("Required URL is not accessible");
    }

}
