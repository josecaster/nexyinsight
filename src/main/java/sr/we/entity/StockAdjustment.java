package sr.we.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;
import java.util.List;

@Entity
public class StockAdjustment extends AbstractEntity {

    private String note;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String sectionId;
    private transient List<StockAdjustmentItems> items;

    public enum Type {
        RI("Receive items"),IC("Inventory count"),LS("Loss"),DM("Damage");
        private String caption;

        Type(String caption) {
            this.caption = caption;
        }

        public String getCaption() {
            return caption;
        }

    }

    public StockAdjustment() {
    }

    public StockAdjustment(String note, LocalDate date, Type type, String sectionId) {
        this.note = note;
        this.date = date;
        this.type = type;
        this.sectionId = sectionId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public List<StockAdjustmentItems> getItems() {
        return items;
    }

    public void setItems(List<StockAdjustmentItems> items) {
        this.items = items;
    }
}
