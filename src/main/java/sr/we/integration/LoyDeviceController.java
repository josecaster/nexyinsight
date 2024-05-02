package sr.we.integration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.eclipsestore.tables.CollectDevices;
import sr.we.repository.IntegrationRepository;

import java.io.IOException;

@Controller
public class LoyDeviceController extends Parent {

    public LoyDeviceController(IntegrationRepository integrationRepository) {
        super(integrationRepository);
    }

    public CollectDevices getList(String loyverseToken, String store_id) throws IOException {
        return getList(store_id, loyverseToken);
    }


    public CollectDevices getSwitchList(String store_ids, String loyverseToken) {
        String url = "https://api.loyverse.com/v1.0/pos_devices?";
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
        ResponseEntity<CollectDevices> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, CollectDevices.class);
        return exchange.getBody();
    }


}
