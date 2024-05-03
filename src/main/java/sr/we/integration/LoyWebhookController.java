package sr.we.integration;

import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.Webhook;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.entity.eclipsestore.tables.ListLoyStores;
import sr.we.entity.eclipsestore.tables.ListWebhooks;
import sr.we.entity.eclipsestore.tables.LoyStore;
import sr.we.repository.IntegrationRepository;

import java.io.IOException;
import java.time.LocalDateTime;

@Controller
public class LoyWebhookController extends Parent {

    public LoyWebhookController(IntegrationRepository integrationRepository) {
        super(integrationRepository);
    }

    public ListWebhooks getListLoyStores() {
        String url = "https://api.loyverse.com/v1.0/webhooks?show_deleted=false";

        String token = getToken(0L);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, token);
        ResponseEntity<ListWebhooks> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ListWebhooks.class);
        return exchange.getBody();
    }

    public Webhook get(String id) throws IOException {
        String url = "https://api.loyverse.com/v1.0/webhooks/" + id;

        String token = getToken(0L);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, token);
        ResponseEntity<Webhook> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Webhook.class);
        return exchange.getBody();
    }

    public Webhook add(Webhook webhook) throws IOException {
        String url = "https://api.loyverse.com/v1.0/webhooks";

        String token = getToken(0L);

        RestTemplate restTemplate = new RestTemplate();
        String json = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create().toJson(webhook);
        HttpEntity<String> httpEntity = getAuthHttpEntity(json, token);
        ResponseEntity<Webhook> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Webhook.class);
        return exchange.getBody();
    }
}
