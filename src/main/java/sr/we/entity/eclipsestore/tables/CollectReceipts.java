package sr.we.entity.eclipsestore.tables;

import java.util.List;

public class CollectReceipts {
    private List<Receipt> receipts;
    private String cursor;

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
}
