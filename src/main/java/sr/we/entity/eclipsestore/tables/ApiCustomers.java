package sr.we.entity.eclipsestore.tables;


import java.util.List;

public class ApiCustomers extends ApiCommunication {

    private List<Customer> customers;

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
