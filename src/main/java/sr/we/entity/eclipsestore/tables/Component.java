package sr.we.entity.eclipsestore.tables;

public class Component {
    public String variant_id;
    public String quantity;

    public String getVariant_id() {
        return variant_id;
    }

    public void setVariant_id(String variant_id) {
        this.variant_id = variant_id;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Component{" +
                "variant_id='" + variant_id + '\'' +
                ", quantity='" + quantity + '\'' +
                '}';
    }
}
