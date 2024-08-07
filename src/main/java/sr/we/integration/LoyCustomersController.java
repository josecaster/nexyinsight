package sr.we.integration;

import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.eclipsestore.tables.Customer;
import sr.we.entity.eclipsestore.tables.CollectCustomers;
import sr.we.repository.IntegrationRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class LoyCustomersController extends Parent {


    public LoyCustomersController(IntegrationRepository integrationRepository) {
        super(integrationRepository);
    }

    public CollectCustomers getList(String loyverseToken, String customer_ids, //
                                LocalDateTime created_at_min, LocalDateTime created_at_max, //
                                LocalDateTime updated_at_min, LocalDateTime updated_at_max,//
                                Integer limit, String cursor//
    ) throws IOException {


        CollectCustomers body = getCollectCustomers(customer_ids, created_at_min, created_at_max, updated_at_min, updated_at_max, limit, cursor, loyverseToken);
        return body;


    }

    public CollectCustomers getCollectCustomers(String customer_ids, LocalDateTime created_at_min, LocalDateTime created_at_max, LocalDateTime updated_at_min, LocalDateTime updated_at_max, Integer limit, String cursor, String loyverseToken) {
        String url = "https://api.loyverse.com/v1.0/customers?show_deleted=true";
        StringBuilder stringBuilder = new StringBuilder(url);
        if (StringUtils.isNotBlank(customer_ids)) {
            stringBuilder.append("&customer_ids=").append(customer_ids);
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

        String token = getToken(0L);
        loyverseToken = StringUtils.isNotBlank(token) ? token: loyverseToken;

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, loyverseToken);
        ResponseEntity<CollectCustomers> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, CollectCustomers.class);
        CollectCustomers body = exchange.getBody();
        return body;
    }

    public Customer get(String loyverseToken, String id) throws IOException {
        String url = "https://api.loyverse.com/v1.0/customers/" + id;

        String token = getToken(0L);
        loyverseToken = StringUtils.isNotBlank(token) ? token: loyverseToken;

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, loyverseToken);
        ResponseEntity<Customer> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Customer.class);
        return exchange.getBody();
    }

    public Customer add(String loyverseToken, Customer customer) throws IOException {
        String url = "https://api.loyverse.com/v1.0/customers";

        String token = getToken(0L);
        loyverseToken = StringUtils.isNotBlank(token) ? token: loyverseToken;

        RestTemplate restTemplate = new RestTemplate();
        String json = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create().toJson(customer);
        HttpEntity<String> httpEntity = getAuthHttpEntity(json, loyverseToken);
        ResponseEntity<Customer> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Customer.class);
        return exchange.getBody();
    }
}
