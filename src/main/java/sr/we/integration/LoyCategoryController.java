package sr.we.integration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.Integration;
import sr.we.entity.eclipsestore.tables.CollectCategories;
import sr.we.repository.IntegrationRepository;

import java.io.IOException;

@Controller
public class LoyCategoryController extends Parent {


    public LoyCategoryController(IntegrationRepository integrationRepository) {
        super(integrationRepository);
    }

    public CollectCategories getList(String loyverseToken, String store_id) throws IOException {

        return getSwicthList(store_id, loyverseToken);


    }



    public CollectCategories getSwicthList(String store_ids, String loyverseToken) {
        String url = "https://api.loyverse.com/v1.0/categories?";
        StringBuilder stringBuilder = new StringBuilder(url);
        if (StringUtils.isNotBlank(store_ids)) {
            stringBuilder.append("&store_id=").append(store_ids);
        }
        String pattern = "YYYY-MM-dd'T'HH:mm:ss.SSS'Z'";


        url = stringBuilder.toString();

        String token = getToken(0L);
        loyverseToken = StringUtils.isNotBlank(token) ? token: loyverseToken;

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(null, loyverseToken);
        ResponseEntity<CollectCategories> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, CollectCategories.class);
        return exchange.getBody();
    }


}
