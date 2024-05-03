package sr.we.controllers.rest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sr.we.entity.Integration;
import sr.we.repository.IntegrationRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@RestController
@RequestMapping("webhook")
public class ApiRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRestController.class);

    @Autowired
    private IntegrationRepository integrationRepository;


    @PostMapping("receipts")
    public ResponseEntity<String> receipts(@RequestHeader(name = "X-Loyvere-Signature", required = false) String authorization, @RequestBody String payload) {
        LOGGER.debug("PAYLOAD["+payload+"]");
        LOGGER.debug("AUTH["+authorization+"]");

        if(StringUtils.isNotBlank(authorization)) {
            Integration byBusinessId = integrationRepository.getByBusinessId(0L);
            if (byBusinessId != null && StringUtils.isNotBlank(byBusinessId.getClientSecret())) {
                String key = byBusinessId.getClientSecret();
                // Extract HMAC from Authorization header
                String providedHmac = authorization.substring("HMAC ".length());
                LOGGER.debug("providedHmac["+providedHmac+"]");

                // Decode base64-encoded HMAC
                byte[] decodedHmacBytes = Base64.getDecoder().decode(providedHmac);
                String providedHmacHex = Hex.encodeHexString(decodedHmacBytes);
                LOGGER.debug("providedHmacHex["+providedHmacHex+"]");

                // Calculate HMAC of the payload
                String calculatedHmac = calculateHmac(payload, key);
                LOGGER.debug("calculatedHmac["+calculatedHmac+"]");

                // Compare provided HMAC with calculated HMAC
                if (!providedHmacHex.equals(calculatedHmac)) {
                    LOGGER.debug("UNAuthorized!!!!");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid HMAC");
                } else {
                    LOGGER.debug("Authorized!");
                }
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body("Hooked");
    }

    private String calculateHmac(String data, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            return Hex.encodeHexString(hmacBytes);
        } catch (Exception e) {
            LOGGER.error("Error calculating HMAC", e);
            throw new RuntimeException("Error calculating HMAC", e);
        }
    }

}
