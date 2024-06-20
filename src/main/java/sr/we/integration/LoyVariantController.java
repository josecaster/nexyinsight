package sr.we.integration;

import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.entity.eclipsestore.tables.ListLoyItems;
import sr.we.entity.eclipsestore.tables.Variant;
import sr.we.repository.IntegrationRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class LoyVariantController extends Parent {


    public LoyVariantController(IntegrationRepository integrationRepository) {
        super(integrationRepository);
    }

    public Variant add(String loyverseToken, Variant variant) throws IOException {
        String url = "https://api.loyverse.com/v1.0/variants";

        String token = getToken(0L);
        loyverseToken = StringUtils.isNotBlank(token) ? token : loyverseToken;

        RestTemplate restTemplate = new RestTemplate();
        String json = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create().toJson(variant);
        HttpEntity<String> httpEntity = getAuthHttpEntity(json, loyverseToken);
        ResponseEntity<Variant> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Variant.class);
        return exchange.getBody();
    }
}
