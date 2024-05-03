package sr.we.controllers.rest;

import sr.we.entity.eclipsestore.tables.Receipt;

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
