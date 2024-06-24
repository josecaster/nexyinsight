package sr.we.entity.eclipsestore.tables;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class VariantStore implements Serializable {
    public String store_id;
    public String pricing_type;
    public BigDecimal price;
    public boolean available_for_sale;
    public Object optimal_stock;
    public Object low_stock;

    public String getStore_id() {
        return store_id;
    }

    public void setStore_id(String store_id) {
        this.store_id = store_id;
    }

    public String getPricing_type() {
        return pricing_type;
    }

    public void setPricing_type(String pricing_type) {
        this.pricing_type = pricing_type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isAvailable_for_sale() {
        return available_for_sale;
    }

    public void setAvailable_for_sale(boolean available_for_sale) {
        this.available_for_sale = available_for_sale;
    }

    public Object getOptimal_stock() {
        return optimal_stock;
    }

    public void setOptimal_stock(Object optimal_stock) {
        this.optimal_stock = optimal_stock;
    }

    public Object getLow_stock() {
        return low_stock;
    }

    public void setLow_stock(Object low_stock) {
        this.low_stock = low_stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariantStore that)) return false;
        return Objects.equals(store_id, that.store_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(store_id);
    }
}
