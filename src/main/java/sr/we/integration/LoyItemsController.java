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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class LoyItemsController extends Parent {


    public ListLoyItems getList(String loyverseToken, String items_ids, //
                                LocalDateTime created_at_min, LocalDateTime created_at_max, //
                                LocalDateTime updated_at_min, LocalDateTime updated_at_max,//
                                Integer limit, String cursor//
    ) throws IOException {


        ListLoyItems body = getListLoyItems(items_ids, created_at_min, created_at_max, updated_at_min, updated_at_max, limit, cursor, loyverseToken);
        return body;


    }

    public ListLoyItems getListLoyItems(String items_ids, LocalDateTime created_at_min, LocalDateTime created_at_max, LocalDateTime updated_at_min, LocalDateTime updated_at_max, Integer limit, String cursor, String loyverseToken) {
        String url = "https://api.loyverse.com/v1.0/items?show_deleted=true";
        StringBuilder stringBuilder = new StringBuilder(url);
        if (StringUtils.isNotBlank(items_ids)) {
            stringBuilder.append("&items_ids=").append(items_ids);
        }
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
        ResponseEntity<ListLoyItems> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ListLoyItems.class);
        ListLoyItems body = exchange.getBody();
        return body;
    }

    public Item get(String loyverseToken, String id) throws IOException {
        String url = "https://api.loyverse.com/v1.0/items/" + id;


        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, loyverseToken);
        ResponseEntity<Item> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Item.class);
        return exchange.getBody();
    }

    public Item add(String loyverseToken, Item item) throws IOException {
        String url = "https://api.loyverse.com/v1.0/items";

        RestTemplate restTemplate = new RestTemplate();
        String json = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create().toJson(item);
        HttpEntity<String> httpEntity = getAuthHttpEntity(json, loyverseToken);
        ResponseEntity<Item> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Item.class);
        return exchange.getBody();
    }
}
