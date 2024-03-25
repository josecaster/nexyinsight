package sr.we.entity.eclipsestore.tables;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class Variant  implements Serializable {
    public String variant_id;
    public String item_id;
    public String sku;
    public String reference_variant_id;
    public String option1_value;
    public String option2_value;
    public String option3_value;
    public String barcode;
    public BigDecimal cost;
    public BigDecimal purchase_cost;
    public String default_pricing_type;
    public BigDecimal default_price;
    public List<VariantStore> stores;
    public String created_at;
    public String updated_at;
    public String deleted_at;

    public String getVariant_id() {
        return variant_id;
    }

    public void setVariant_id(String variant_id) {
        this.variant_id = variant_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getReference_variant_id() {
        return reference_variant_id;
    }

    public void setReference_variant_id(String reference_variant_id) {
        this.reference_variant_id = reference_variant_id;
    }

    public String getOption1_value() {
        return option1_value;
    }

    public void setOption1_value(String option1_value) {
        this.option1_value = option1_value;
    }

    public String getOption2_value() {
        return option2_value;
    }

    public void setOption2_value(String option2_value) {
        this.option2_value = option2_value;
    }

    public String getOption3_value() {
        return option3_value;
    }

    public void setOption3_value(String option3_value) {
        this.option3_value = option3_value;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getPurchase_cost() {
        return purchase_cost;
    }

    public void setPurchase_cost(BigDecimal purchase_cost) {
        this.purchase_cost = purchase_cost;
    }

    public String getDefault_pricing_type() {
        return default_pricing_type;
    }

    public void setDefault_pricing_type(String default_pricing_type) {
        this.default_pricing_type = default_pricing_type;
    }

    public BigDecimal getDefault_price() {
        return default_price;
    }

    public void setDefault_price(BigDecimal default_price) {
        this.default_price = default_price;
    }

    public List<VariantStore> getStores() {
        return stores;
    }

    public void setStores(List<VariantStore> stores) {
        this.stores = stores;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(String deleted_at) {
        this.deleted_at = deleted_at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variant variant)) return false;
        return Objects.equals(variant_id, variant.variant_id) && Objects.equals(item_id, variant.item_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant_id, item_id);
    }

    @Override
    public String toString() {
        return "Variant{" +
                "variant_id='" + variant_id + '\'' +
                ", item_id='" + item_id + '\'' +
                ", sku='" + sku + '\'' +
                ", reference_variant_id='" + reference_variant_id + '\'' +
                ", option1_value='" + option1_value + '\'' +
                ", option2_value='" + option2_value + '\'' +
                ", option3_value='" + option3_value + '\'' +
                ", barcode='" + barcode + '\'' +
                ", cost=" + cost +
                ", purchase_cost=" + purchase_cost +
                ", default_pricing_type='" + default_pricing_type + '\'' +
                ", default_price=" + default_price +
                ", stores=" + stores +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", deleted_at='" + deleted_at + '\'' +
                '}';
    }
}
