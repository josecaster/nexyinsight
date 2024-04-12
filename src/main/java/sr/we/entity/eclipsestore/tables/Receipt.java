package sr.we.entity.eclipsestore.tables;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Receipt extends SuperDao implements Cloneable{
    private Long businessId;
    private String receipt_number;
    private String note;
    private String receipt_type;
    private String refund_for;
//    private Object order;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private String source;
    private LocalDateTime receipt_date;
    private LocalDateTime cancelled_at;
    private BigDecimal total_money;
    private BigDecimal total_tax;
    private BigDecimal points_earned;
    private BigDecimal points_deducted;
    private BigDecimal points_balance;
    private Object customer_id;
    private BigDecimal total_discount;
    private String employee_id;
    private String store_id;
    private String pos_device_id;
//    private Object dining_option;
//    private ArrayList<Object> total_discounts;
//    private ArrayList<Object> total_taxes;
    private BigDecimal tip;
    private BigDecimal surcharge;
    private List<LineItem> line_items;
    private LineItem line_item;
    private List<Payment> payments;
    private String category_id;

    public String getReceipt_number() {
        return receipt_number;
    }

    public void setReceipt_number(String receipt_number) {
        this.receipt_number = receipt_number;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getReceipt_type() {
        return receipt_type;
    }

    public void setReceipt_type(String receipt_type) {
        this.receipt_type = receipt_type;
    }

    public String getRefund_for() {
        return refund_for;
    }

    public void setRefund_for(String refund_for) {
        this.refund_for = refund_for;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getReceipt_date() {
        return receipt_date;
    }

    public void setReceipt_date(LocalDateTime receipt_date) {
        this.receipt_date = receipt_date;
    }

    public LocalDateTime getCancelled_at() {
        return cancelled_at;
    }

    public void setCancelled_at(LocalDateTime cancelled_at) {
        this.cancelled_at = cancelled_at;
    }

    public BigDecimal getTotal_money() {
        return total_money;
    }

    public void setTotal_money(BigDecimal total_money) {
        this.total_money = total_money;
    }

    public BigDecimal getTotal_tax() {
        return total_tax;
    }

    public void setTotal_tax(BigDecimal total_tax) {
        this.total_tax = total_tax;
    }

    public BigDecimal getPoints_earned() {
        return points_earned;
    }

    public void setPoints_earned(BigDecimal points_earned) {
        this.points_earned = points_earned;
    }

    public BigDecimal getPoints_deducted() {
        return points_deducted;
    }

    public void setPoints_deducted(BigDecimal points_deducted) {
        this.points_deducted = points_deducted;
    }

    public BigDecimal getPoints_balance() {
        return points_balance;
    }

    public void setPoints_balance(BigDecimal points_balance) {
        this.points_balance = points_balance;
    }

    public Object getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(Object customer_id) {
        this.customer_id = customer_id;
    }

    public BigDecimal getTotal_discount() {
        return total_discount;
    }

    public void setTotal_discount(BigDecimal total_discount) {
        this.total_discount = total_discount;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public String getStore_id() {
        return store_id;
    }

    public void setStore_id(String store_id) {
        this.store_id = store_id;
    }

    public String getPos_device_id() {
        return pos_device_id;
    }

    public void setPos_device_id(String pos_device_id) {
        this.pos_device_id = pos_device_id;
    }

    public BigDecimal getTip() {
        return tip;
    }

    public void setTip(BigDecimal tip) {
        this.tip = tip;
    }

    public BigDecimal getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(BigDecimal surcharge) {
        this.surcharge = surcharge;
    }

    public List<LineItem> getLine_items() {
        return line_items;
    }

    public void setLine_items(List<LineItem> line_items) {
        this.line_items = line_items;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public LineItem getLine_item() {
        return line_item;
    }

    public void setLine_item(LineItem line_item) {
        this.line_item = line_item;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    @Override
    public Receipt clone() {
        try {
            Receipt clone = (Receipt) super.clone();
            clone.setUuId(null);
            clone.setReceipt_number(null);
            clone.setLine_items(null);
            clone.setLine_item(null);
            clone.setCategory_id(null);
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
