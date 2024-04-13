package sr.we.entity.eclipsestore.tables;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class InventoryValuation extends SuperDao {
    private Long businessId;
    private BigDecimal inventoryValue, retailValue, potentialProfit,margin;
    private LocalDate localDate;
    private Type type;

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public enum Type {
        MANUAL,AUTOMATIC
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public BigDecimal getInventoryValue() {
        return inventoryValue;
    }

    public void setInventoryValue(BigDecimal inventoryValue) {
        this.inventoryValue = inventoryValue;
    }

    public BigDecimal getRetailValue() {
        return retailValue;
    }

    public void setRetailValue(BigDecimal retailValue) {
        this.retailValue = retailValue;
    }

    public BigDecimal getPotentialProfit() {
        return potentialProfit;
    }

    public void setPotentialProfit(BigDecimal potentialProfit) {
        this.potentialProfit = potentialProfit;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "InventoryValuation{" +
                "businessId=" + businessId +
                ", inventoryValue=" + inventoryValue +
                ", retailValue=" + retailValue +
                ", potentialProfit=" + potentialProfit +
                ", margin=" + margin +
                ", localDate=" + localDate +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryValuation item)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(uuId, item.uuId) && Objects.equals(businessId, item.businessId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuId, businessId);
    }
}
