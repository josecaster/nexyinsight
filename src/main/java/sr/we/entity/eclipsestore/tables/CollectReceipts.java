package sr.we.entity.eclipsestore.tables;

import java.util.List;

public class CollectReceipts {
    private List<Receipt> receipts;
    private String cursor;


    private List<Error> errors;

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    @Override
    public String toString() {
        return "CollectReceipts{" +
                "receipts=" + receipts +
                ", cursor='" + cursor + '\'' +
                ", errors=" + errors +
                '}';
    }
}
