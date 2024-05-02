package sr.we.integration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.Integration;
import sr.we.repository.IntegrationRepository;
import sr.we.services.IntegrationService;

@Controller
public class AuthController extends Parent {

    private final IntegrationRepository integrationRepository;
    private final IntegrationService integrationService;

    public AuthController(IntegrationRepository integrationRepository, IntegrationService integrationService) {
        super(integrationRepository);
        this.integrationRepository = integrationRepository;
        this.integrationService = integrationService;
    }

    public Token authorize(Long businessId, boolean refresh) {

        Integration integration = integrationRepository.getByBusinessId(businessId);
        if (integration == null || StringUtils.isBlank(integration.getClientId()) || StringUtils.isBlank(integration.getClientSecret())) {
            return null;
        }

        String url = "https://api.loyverse.com/oauth/token";


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("PRIVATE-TOKEN", "xyz");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (!refresh) {
            map.add("grant_type", "authorization_code");
            map.add("code", integration.getCode());
            map.add("redirect_uri", integration.getRedirectUri());
            map.add("client_id", integration.getClientId());
            map.add("client_secret", integration.getClientSecret());
        } else {
            map.add("grant_type", "refresh_token");
            map.add("refresh_token", integration.getRefreshToken());
            map.add("client_id", integration.getClientId());
            map.add("client_secret", integration.getClientSecret());
        }

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<Token> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, Token.class);
        Token body = exchange.getBody();
        if (body != null) {
            integration.setAccessToken(body.access_token);
            integration.setRefreshToken(body.refresh_token);
            integration.setExpires(body.expires_in);
            integrationService.update(integration);
        }
        return body;
    }

    public String authorize(Long businessId) {
        Integration integration = integrationRepository.getByBusinessId(businessId);
        if (integration == null || StringUtils.isBlank(integration.getClientId()) || StringUtils.isBlank(integration.getClientSecret())) {
            return null;
        }
        return "https://api.loyverse.com/oauth/authorize?client_id=" + integration.getClientId() +//
                "&scope=CUSTOMERS_READ%20CUSTOMERS_WRITE%20EMPLOYEES_READ%20ITEMS_READ%20INVENTORY_READ%20INVENTORY_WRITE%20ITEMS_WRITE%20MERCHANT_READ%20PAYMENT_TYPES_READ%20POS_DEVICES_READ%20POS_DEVICES_WRITE%20RECEIPTS_READ%20RECEIPTS_WRITE%20SHIFTS_READ%20STORES_READ%20SUPPLIERS_READ%20SUPPLIERS_WRITE%20TAXES_READ%20TAXES_WRITE" +//
                "&response_type=code" +//
                "&redirect_uri=" + integration.getRedirectUri() +//
                "&state=" + integration.getState();
    }


    private class Token {
        private String access_token, token_type, refresh_token, scope;
        private Integer expires_in;

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public String getRefresh_token() {
            return refresh_token;
        }

        public void setRefresh_token(String refresh_token) {
            this.refresh_token = refresh_token;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public Integer getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Integer expires_in) {
            this.expires_in = expires_in;
        }
    }
}
