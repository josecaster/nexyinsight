package sr.we.integration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.eclipsestore.tables.CollectReceipts;
import sr.we.entity.eclipsestore.tables.Receipt;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class LoyReceiptsController extends Parent {

    public CollectReceipts getList(String loyverseToken, Long businessId, //
                                   LocalDateTime created_at_min, LocalDateTime created_at_max, //
                                   LocalDateTime updated_at_min, LocalDateTime updated_at_max,//
                                   Integer limit, String cursor

    ) throws IOException {
        return getListLoyStores(created_at_min, created_at_max, updated_at_min, updated_at_max, limit, cursor, loyverseToken);


    }

    public CollectReceipts getListLoyStores(LocalDateTime created_at_min, LocalDateTime created_at_max, LocalDateTime updated_at_min, LocalDateTime updated_at_max, Integer limit, String cursor, String loyverseToken) {
        String url = "https://api.loyverse.com/v1.0/receipts?show_deleted=false";
        StringBuilder stringBuilder = new StringBuilder(url);

        String pattern = "YYYY-MM-dd'T'HH:mm:ss.SSS'Z'";
        if (created_at_min != null) {
            stringBuilder.append("&created_at_min=").append(DateTimeFormatter.ofPattern(pattern).format(created_at_min));
        }
        if (created_at_max != null) {
            stringBuilder.append("&created_at_max=").append(DateTimeFormatter.ofPattern(pattern).format(created_at_max));
        }
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


        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, loyverseToken);
        ResponseEntity<CollectReceipts> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, CollectReceipts.class);
        return exchange.getBody();
    }

    public Receipt get(String loyverseToken, Long businessId, String id) throws IOException {
        String url = "https://api.loyverse.com/v1.0/receipts/" + id;

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, loyverseToken);
        ResponseEntity<Receipt> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Receipt.class);
        return exchange.getBody();
    }
}
