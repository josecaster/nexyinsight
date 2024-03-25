package sr.we.entity.eclipsestore.tables;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private String payment_type_id;
    private String name;
    private String type;
    private BigDecimal money_amount;
    private LocalDateTime paid_at;
//    private Object payment_details;
}
