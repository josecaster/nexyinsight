package sr.we.integration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.eclipsestore.tables.ListLoyStores;
import sr.we.entity.eclipsestore.tables.LoyStore;
import sr.we.repository.IntegrationRepository;

import java.io.IOException;
import java.time.LocalDateTime;

@Controller
public class LoyStoresController extends Parent {

    public LoyStoresController(IntegrationRepository integrationRepository) {
        super(integrationRepository);
    }

    public ListLoyStores getList(String loyverseToken, String store_ids, //
                                 LocalDateTime created_at_min, LocalDateTime created_at_max, //
                                 LocalDateTime updated_at_min, LocalDateTime updated_at_max) throws IOException {
        return getListLoyStores(store_ids, created_at_min, created_at_max, updated_at_min, updated_at_max, loyverseToken);


    }

    public ListLoyStores getListLoyStores(String store_ids, LocalDateTime created_at_min, LocalDateTime created_at_max, LocalDateTime updated_at_min, LocalDateTime updated_at_max, String loyverseToken) {
        String url = "https://api.loyverse.com/v1.0/stores?show_deleted=false";
        StringBuilder stringBuilder = new StringBuilder(url);
        if (StringUtils.isNotBlank(store_ids)) {
            stringBuilder.append("&store_ids=").append(store_ids);
        }
        if (created_at_min != null) {
            stringBuilder.append("&created_at_min=").append(created_at_min);
        }
        if (created_at_max != null) {
            stringBuilder.append("&created_at_max=").append(created_at_max);
        }
        if (updated_at_min != null) {
            stringBuilder.append("&updated_at_min=").append(updated_at_min);
        }
        if (updated_at_max != null) {
            stringBuilder.append("&updated_at_max=").append(updated_at_max);
        }
        url = stringBuilder.toString();

        String token = getToken(0L);
        loyverseToken = StringUtils.isNotBlank(token) ? token: loyverseToken;

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, loyverseToken);
        ResponseEntity<ListLoyStores> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ListLoyStores.class);
        return exchange.getBody();
    }

    public LoyStore get(String loyverseToken, String id) throws IOException {
        String url = "https://api.loyverse.com/v1.0/stores/" + id;

        String token = getToken(0L);
        loyverseToken = StringUtils.isNotBlank(token) ? token: loyverseToken;

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, loyverseToken);
        ResponseEntity<LoyStore> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, LoyStore.class);
        return exchange.getBody();
    }
}
