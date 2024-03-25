package sr.we.entity.eclipsestore.tables;

import java.math.BigDecimal;

public class LineItem {
    private String id;
    private String item_id;
    private String variant_id;
    private String item_name;
    private Object variant_name;
    private String sku;
    private int quantity;
    private BigDecimal price;
    private BigDecimal gross_total_money;
    private BigDecimal total_money;
    private BigDecimal cost;
    private BigDecimal cost_total;
    private Object line_note;
//    private ArrayList<Object> line_taxes;
    private BigDecimal total_discount;
//    private ArrayList<Object> line_discounts;
//    private ArrayList<Object> line_modifiers;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getVariant_id() {
        return variant_id;
    }

    public void setVariant_id(String variant_id) {
        this.variant_id = variant_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public Object getVariant_name() {
        return variant_name;
    }

    public void setVariant_name(Object variant_name) {
        this.variant_name = variant_name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getGross_total_money() {
        return gross_total_money;
    }

    public void setGross_total_money(BigDecimal gross_total_money) {
        this.gross_total_money = gross_total_money;
    }

    public BigDecimal getTotal_money() {
        return total_money;
    }

    public void setTotal_money(BigDecimal total_money) {
        this.total_money = total_money;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getCost_total() {
        return cost_total;
    }

    public void setCost_total(BigDecimal cost_total) {
        this.cost_total = cost_total;
    }

    public Object getLine_note() {
        return line_note;
    }

    public void setLine_note(Object line_note) {
        this.line_note = line_note;
    }

    public BigDecimal getTotal_discount() {
        return total_discount;
    }

    public void setTotal_discount(BigDecimal total_discount) {
        this.total_discount = total_discount;
    }
}
