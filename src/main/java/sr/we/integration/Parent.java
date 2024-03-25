package sr.we.integration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public abstract class Parent {

    protected HttpEntity<String> getAuthHttpEntity(String body, String accessToken) {
        HttpHeaders headers = getAuthHttpHeaders(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }


    protected HttpHeaders getAuthHttpHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "BEARER "+accessToken);
        return headers;
    }

}
