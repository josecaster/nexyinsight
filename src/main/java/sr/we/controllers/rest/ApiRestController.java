package sr.we.controllers.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import sr.we.entity.eclipsestore.tables.CollectReceipts;

@RestController("webhook")
public class ApiRestController {


    @PostMapping("receipts")
    public void receipts(@RequestBody CollectReceipts vo) {

    }

}
