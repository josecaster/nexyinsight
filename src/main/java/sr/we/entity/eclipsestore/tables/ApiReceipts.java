package sr.we.entity.eclipsestore.tables;


import java.util.List;

public class ApiReceipts extends ApiCommunication{

    private List<Receipt> receipts;

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
    }
}
