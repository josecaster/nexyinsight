package sr.we.controllers.rest;

import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import sr.we.entity.Integration;
import sr.we.entity.eclipsestore.tables.ApiInventoryLevels;
import sr.we.entity.eclipsestore.tables.ApiItems;
import sr.we.entity.eclipsestore.tables.ApiReceipts;
import sr.we.repository.IntegrationRepository;
import sr.we.schedule.JobbyLauncher;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
@RequestMapping("webhook")
public class ApiRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRestController.class);

    private final IntegrationRepository integrationRepository;
    private final JobbyLauncher jobbyLauncher;

    public ApiRestController(IntegrationRepository integrationRepository, JobbyLauncher jobbyLauncher) {
        this.integrationRepository = integrationRepository;
        this.jobbyLauncher = jobbyLauncher;
    }

    private static String calculateHMAC(String data, String key) {
        try {
            String hmacSHA1 = "HmacSHA1";
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), hmacSHA1);
            Mac mac = Mac.getInstance(hmacSHA1);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            return Hex.encodeHexString(hmacBytes);
        } catch (Exception e) {
            LOGGER.error("Error calculating HMAC", e);
            throw new RuntimeException("Error calculating HMAC", e);
        }
    }

    @PostMapping("items")
    public ResponseEntity<String> items(@RequestHeader MultiValueMap<String, String> headers, @RequestHeader(name = "X-Loyvere-Signature", required = false) String authorization, @RequestBody String payload) {
        ResponseEntity<String> authorize = authorize(headers, authorization, payload);
        if (authorize != null) return authorize;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        ApiItems body = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer(formatter)).create().fromJson(payload, ApiItems.class);
        if (body != null && body.getItems() != null) {
            jobbyLauncher.doForItems(body.getItems(), false);// false means it will not affect the stock, we will leave that for the inventory api
        }

        return ResponseEntity.status(HttpStatus.OK).body("Hooked");
    }

    @PostMapping("inventory")
    public ResponseEntity<String> inventory(@RequestHeader MultiValueMap<String, String> headers, @RequestHeader(name = "X-Loyvere-Signature", required = false) String authorization, @RequestBody String payload) {
        ResponseEntity<String> authorize = authorize(headers, authorization, payload);
        if (authorize != null) return authorize;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        ApiInventoryLevels body = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer(formatter)).create().fromJson(payload, ApiInventoryLevels.class);
        if (body != null && body.getInventory_levels() != null) {
            jobbyLauncher.doForInventoryLevels(body.getInventory_levels());
        }

        return ResponseEntity.status(HttpStatus.OK).body("Hooked");
    }

    @PostMapping("customers")
    public ResponseEntity<String> customers(@RequestHeader MultiValueMap<String, String> headers, @RequestHeader(name = "X-Loyvere-Signature", required = false) String authorization, @RequestBody String payload) {
        ResponseEntity<String> authorize = authorize(headers, authorization, payload);
        if (authorize != null) return authorize;


        return ResponseEntity.status(HttpStatus.OK).body("Hooked");
    }

    @PostMapping("shifts")
    public ResponseEntity<String> shifts(@RequestHeader MultiValueMap<String, String> headers, @RequestHeader(name = "X-Loyvere-Signature", required = false) String authorization, @RequestBody String payload) {
        ResponseEntity<String> authorize = authorize(headers, authorization, payload);
        if (authorize != null) return authorize;


        return ResponseEntity.status(HttpStatus.OK).body("Hooked");
    }

//    public static void main(String[] args) {
//
//        String payload = "{\"merchant_id\":\"72649fd0-32bd-11ed-9532-06d9d369adca\",\"type\":\"receipts.update\",\"created_at\":\"2024-05-03T20:05:41.717Z\",\"receipts\":[{\"receipt_number\":\"7-1005\",\"note\":null,\"receipt_type\":\"REFUND\",\"refund_for\":\"7-1004\",\"order\":null,\"created_at\":\"2024-05-03T20:05:37.000Z\",\"updated_at\":\"2024-05-03T20:05:37.000Z\",\"source\":\"point of sale\",\"receipt_date\":\"2024-05-03T20:05:36.000Z\",\"cancelled_at\":null,\"total_money\":0,\"total_tax\":0,\"points_earned\":0,\"points_deducted\":0,\"points_balance\":0,\"customer_id\":null,\"total_discount\":0,\"employee_id\":\"1d7890e0-6bed-4fa6-be65-f4d3dd2630ed\",\"store_id\":\"62824228-b3c6-454f-8625-0db95a8cfcab\",\"pos_device_id\":\"b58b6adc-6d8c-4157-b253-f46416b8db06\",\"dining_option\":null,\"total_discounts\":[],\"total_taxes\":[],\"tip\":0,\"surcharge\":0,\"line_items\":[{\"id\":\"9553152a-9d8c-0644-f6a3-2abcd917516c\",\"item_id\":\"26ce784e-8699-46eb-b8d7-c385e2597c4b\",\"variant_id\":\"f7b2acc4-1da9-49d2-96ef-4dc82674704e\",\"item_name\":\"TestForJose\",\"variant_name\":null,\"sku\":\"Test1\",\"quantity\":1,\"price\":0,\"gross_total_money\":0,\"total_money\":0,\"cost\":0,\"cost_total\":0,\"line_note\":null,\"line_taxes\":[],\"total_discount\":0,\"line_discounts\":[],\"line_modifiers\":[]}],\"payments\":[{\"payment_type_id\":\"8785b698-6b33-4c9b-8603-db592c7fa409\",\"name\":\"Cash\",\"type\":\"CASH\",\"money_amount\":0,\"paid_at\":\"2024-05-03T20:05:36.000Z\",\"payment_details\":null}]}]}";
//        ApiReceipts body = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer(formatter)).create().fromJson(payload, ApiReceipts.class);
//        System.out.println(body);
//    }

    @PostMapping("receipts")
    public ResponseEntity<String> receipts(@RequestHeader MultiValueMap<String, String> headers, @RequestHeader(name = "X-Loyvere-Signature", required = false) String authorization, @RequestBody String payload) {
//        String payload = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create().toJson(ApiReceipts.class);
        ResponseEntity<String> authorize = authorize(headers, authorization, payload);
        if (authorize != null) return authorize;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        ApiReceipts body = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer(formatter)).create().fromJson(payload, ApiReceipts.class);
        if (body != null && body.getReceipts() != null) {
            jobbyLauncher.doForReceipt(body.getReceipts());
        }

        return ResponseEntity.status(HttpStatus.OK).body("Hooked");
    }

    private ResponseEntity<String> authorize(MultiValueMap<String, String> headers, String authorization, String payload) {
        LOGGER.debug("HEADERS[" + headers + "]");
        LOGGER.debug("PAYLOAD[" + payload + "]");

//        if (StringUtils.isBlank(authorization)) {
//            List<String> authorization1 = headers.get("x-loyverse-signature");
//            if (authorization1 == null || authorization1.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No x-loyverse-signature");
//            }
//            authorization = authorization1.get(0);
//        }
//
//        LOGGER.debug("AUTH[" + authorization + "]");
//
//        if (StringUtils.isNotBlank(authorization)) {
//            Integration byBusinessId = integrationRepository.getByBusinessId(0L);
//            if (byBusinessId != null && StringUtils.isNotBlank(byBusinessId.getClientSecret())) {
//                String key = byBusinessId.getClientSecret();
//                // Extract HMAC from Authorization header
//                LOGGER.debug("providedHmac[" + authorization + "]");
//
//                // Calculate HMAC of the payload
//                String calculatedHmac = calculateHMAC(payload, key);
//                LOGGER.debug("calculatedHmac[" + calculatedHmac + "]");
//
//                // Compare provided HMAC with calculated HMAC
//                if (!authorization.equals(calculatedHmac)) {
//                    LOGGER.debug("UNAuthorized!!!!");
//                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid HMAC");
//                } else {
//                    LOGGER.debug("Authorized!");
//                }
//            }
//        }
        return null;
    }

}
