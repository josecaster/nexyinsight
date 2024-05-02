package sr.we.controllers.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import sr.we.entity.eclipsestore.tables.CollectReceipts;
import sr.we.schedule.JobbyLauncher;

@RestController("webhook")
public class ApiRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRestController.class);


    @PostMapping("receipts")
    public void receipts(@RequestBody CollectReceipts vo) {
        LOGGER.debug(vo.toString());
    }

}
