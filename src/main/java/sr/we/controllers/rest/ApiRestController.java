package sr.we.controllers.rest;

import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import sr.we.entity.Integration;
import sr.we.entity.eclipsestore.tables.CollectReceipts;
import sr.we.integration.LocalDateTimeAdapter;
import sr.we.repository.IntegrationRepository;
import sr.we.schedule.JobbyLauncher;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
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
            String hmacSHA256 = "HmacSHA1";
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), hmacSHA256);
            Mac mac = Mac.getInstance(hmacSHA256);
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


        return ResponseEntity.status(HttpStatus.OK).body("Hooked");
    }

    @PostMapping("inventory")
    public ResponseEntity<String> inventory(@RequestHeader MultiValueMap<String, String> headers, @RequestHeader(name = "X-Loyvere-Signature", required = false) String authorization, @RequestBody String payload) {
        ResponseEntity<String> authorize = authorize(headers, authorization, payload);
        if (authorize != null) return authorize;


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

    @PostMapping("receipts")
    public ResponseEntity<String> receipts(@RequestHeader MultiValueMap<String, String> headers, @RequestHeader(name = "X-Loyvere-Signature", required = false) String authorization, @RequestBody ApiReceipts body) {
        String payload = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create().toJson(ApiReceipts.class);
        ResponseEntity<String> authorize = authorize(headers, authorization, payload);
        if (authorize != null) return authorize;


        if(body != null && body.getReceipts() != null){
            jobbyLauncher.doForReceipt(body.getReceipts());
        }

        return ResponseEntity.status(HttpStatus.OK).body("Hooked");
    }

    private ResponseEntity<String> authorize(MultiValueMap<String, String> headers, String authorization, String payload) {
        LOGGER.debug("HEADERS[" + headers + "]");
        LOGGER.debug("PAYLOAD[" + payload + "]");

        if (StringUtils.isBlank(authorization)) {
            List<String> authorization1 = headers.get("x-loyverse-signature");
            if (authorization1 == null || authorization1.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No x-loyverse-signature");
            }
            authorization = authorization1.get(0);
        }

        LOGGER.debug("AUTH[" + authorization + "]");

        if (StringUtils.isNotBlank(authorization)) {
            Integration byBusinessId = integrationRepository.getByBusinessId(0L);
            if (byBusinessId != null && StringUtils.isNotBlank(byBusinessId.getClientSecret())) {
                String key = byBusinessId.getClientSecret();
                // Extract HMAC from Authorization header
                LOGGER.debug("providedHmac[" + authorization + "]");

                // Calculate HMAC of the payload
                String calculatedHmac = calculateHMAC(payload, key);
                LOGGER.debug("calculatedHmac[" + calculatedHmac + "]");

                // Compare provided HMAC with calculated HMAC
                if (!authorization.equals(calculatedHmac)) {
                    LOGGER.debug("UNAuthorized!!!!");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid HMAC");
                } else {
                    LOGGER.debug("Authorized!");
                }
            }
        }
        return null;
    }

}
