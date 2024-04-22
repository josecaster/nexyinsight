package sr.we.entity.eclipsestore.tables;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Item extends SuperDao implements Cloneable{
    private String id;
    private Long businessId;
    private String seaqnsUuId;
    private String handle;
    private String item_name, description;
    private String reference_id;
    private String category_id;
    private boolean track_stock;
    private boolean sold_by_weight;
    private String is_composite;
    private boolean use_production;
    private List<Component> components;
    private String primary_supplier_id;
    private List<String> tax_ids;
    private List<String> modifiers_ids;
    private String form;
    private String color;
    private String image_url;
    private String option1_name;
    private String option2_name;
    private String option3_name;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private LocalDateTime deleted_at;
    private List<Variant> variants;
    private Variant variant;
    private VariantStore variantStore;
    private int stock_level;
    private Map<String, BigDecimal> storeCountMap;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getSeaqnsUuId() {
        return seaqnsUuId;
    }

    public void setSeaqnsUuId(String seaqnsUuId) {
        this.seaqnsUuId = seaqnsUuId;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getReference_id() {
        return reference_id;
    }

    public void setReference_id(String reference_id) {
        this.reference_id = reference_id;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public boolean isTrack_stock() {
        return track_stock;
    }

    public void setTrack_stock(boolean track_stock) {
        this.track_stock = track_stock;
    }

    public boolean isSold_by_weight() {
        return sold_by_weight;
    }

    public void setSold_by_weight(boolean sold_by_weight) {
        this.sold_by_weight = sold_by_weight;
    }

    public String getIs_composite() {
        return is_composite;
    }

    public void setIs_composite(String is_composite) {
        this.is_composite = is_composite;
    }

    public boolean isUse_production() {
        return use_production;
    }

    public void setUse_production(boolean use_production) {
        this.use_production = use_production;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public String getPrimary_supplier_id() {
        return primary_supplier_id;
    }

    public void setPrimary_supplier_id(String primary_supplier_id) {
        this.primary_supplier_id = primary_supplier_id;
    }

    public List<String> getTax_ids() {
        return tax_ids;
    }

    public void setTax_ids(List<String> tax_ids) {
        this.tax_ids = tax_ids;
    }

    public List<String> getModifiers_ids() {
        return modifiers_ids;
    }

    public void setModifiers_ids(List<String> modifiers_ids) {
        this.modifiers_ids = modifiers_ids;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getOption1_name() {
        return option1_name;
    }

    public void setOption1_name(String option1_name) {
        this.option1_name = option1_name;
    }

    public String getOption2_name() {
        return option2_name;
    }

    public void setOption2_name(String option2_name) {
        this.option2_name = option2_name;
    }

    public String getOption3_name() {
        return option3_name;
    }

    public void setOption3_name(String option3_name) {
        this.option3_name = option3_name;
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

    public LocalDateTime getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(LocalDateTime deleted_at) {
        this.deleted_at = deleted_at;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public VariantStore getVariantStore() {
        return variantStore;
    }

    public void setVariantStore(VariantStore variantStore) {
        this.variantStore = variantStore;
    }

    public int getStock_level() {
        return stock_level;
    }

    public void setStock_level(int stock_level) {
        this.stock_level = stock_level;
    }

    public Map<String, BigDecimal> getStoreCountMap() {
        return storeCountMap;
    }

    public void setStoreCountMap(Map<String, BigDecimal> storeCountMap) {
        this.storeCountMap = storeCountMap;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addStoreCount(String uuId, BigDecimal value) {
        if(this.getStoreCountMap() == null){
            setStoreCountMap(new HashMap<>());
        }
        if(value != null) {
            this.storeCountMap.put(uuId, value);
        } else {
            this.storeCountMap.remove(uuId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(id, item.id) && Objects.equals(businessId, item.businessId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, businessId);
    }

    @Override
    public Item clone() {
        try {
            Item clone = (Item) super.clone();
            clone.setUuId(null);
            clone.setId(null);
            clone.setVariantStore(null);
            clone.setVariant(null);
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    private LocalDateTime lastUpdateStockLevel;

    public LocalDateTime getLastUpdateStockLevel() {
        return lastUpdateStockLevel;
    }

    public void setLastUpdateStockLevel(LocalDateTime lastUpdateStockLevel) {
        this.lastUpdateStockLevel = lastUpdateStockLevel;
    }
}
