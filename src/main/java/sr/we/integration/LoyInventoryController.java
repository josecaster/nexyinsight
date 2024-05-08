package sr.we.integration;

import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import sr.we.controllers.rest.LocalDateTimeDeserializer;
import sr.we.entity.eclipsestore.tables.InventoryLevels;
import sr.we.repository.IntegrationRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class LoyInventoryController extends Parent {
    public LoyInventoryController(IntegrationRepository integrationRepository) {
        super(integrationRepository);
    }

    public InventoryLevels getList(String loyverseToken, String store_ids, //
                                   String variant_ids,//
                                   LocalDateTime updated_at_min, LocalDateTime updated_at_max,//
                                   Integer limit, String cursor//
    ) throws IOException {


        return getListLoyItems(store_ids, variant_ids, updated_at_min, updated_at_max, limit, cursor, loyverseToken);


    }

    public InventoryLevels getListLoyItems(String store_ids, String variant_ids, LocalDateTime updated_at_min, LocalDateTime updated_at_max, Integer limit, String cursor, String loyverseToken) {
        String url = "https://api.loyverse.com/v1.0/inventory?";
        StringBuilder stringBuilder = new StringBuilder(url);
        if (StringUtils.isNotBlank(store_ids)) {
            stringBuilder.append("&store_ids=").append(store_ids);
        }
        if (StringUtils.isNotBlank(variant_ids)) {
            stringBuilder.append("&variant_ids=").append(variant_ids);
        }
        String pattern = "YYYY-MM-dd'T'HH:mm:ss.SSS'Z'";

        if (updated_at_min != null) {
            stringBuilder.append("&updated_at_min=").append(DateTimeFormatter.ofPattern(pattern).format(updated_at_min));
        }
        if (updated_at_max != null) {
            stringBuilder.append("&updated_at_max=").append(DateTimeFormatter.ofPattern(pattern).format(updated_at_max));
        }
        if (limit != null) {
            stringBuilder.append("&limit=").append(limit);
        }
        if (cursor != null) {
            stringBuilder.append("&cursor=").append(cursor);
        }
        url = stringBuilder.toString();

        String token = getToken(0L);
        loyverseToken = StringUtils.isNotBlank(token) ? token: loyverseToken;

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, loyverseToken);
        ResponseEntity<InventoryLevels> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, InventoryLevels.class);
        return exchange.getBody();
    }


    public void add(String loyverseToken, InventoryLevels inventoryLevels) {
        String url = "https://api.loyverse.com/v1.0/inventory";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(formatter);
        String json = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create().toJson(inventoryLevels);
        RestTemplate restTemplate = new RestTemplate();

        String token = getToken(0L);
        loyverseToken = StringUtils.isNotBlank(token) ? token: loyverseToken;

        HttpEntity<String> httpEntity = getAuthHttpEntity(json, loyverseToken);
        ResponseEntity<InventoryLevels> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, InventoryLevels.class);
    }
}
