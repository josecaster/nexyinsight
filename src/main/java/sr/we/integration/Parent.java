package sr.we.integration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import sr.we.entity.Integration;
import sr.we.repository.IntegrationRepository;

public abstract class Parent {

//    protected HttpEntity<String> getHttpEntity(String body, String accessToken) {
//        HttpHeaders headers = getAuthHttpHeaders(accessToken);
//        headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
//        return new HttpEntity<>(body, headers);
//    }

    private final IntegrationRepository integrationRepository;

    public Parent(IntegrationRepository integrationRepository) {
        this.integrationRepository = integrationRepository;
    }

    protected String getToken(Long businessId) {
        Integration byBusinessId = integrationRepository.getByBusinessId(businessId);
        return byBusinessId == null ? null : byBusinessId.getAccessToken();
    }

    protected HttpEntity<String> getAuthHttpEntity(String body, String accessToken) {
        HttpHeaders headers = getAuthHttpHeaders(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }


    protected HttpHeaders getAuthHttpHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "BEARER " + accessToken);
        return headers;
    }

}
