package sr.we.entity.eclipsestore.tables;

import java.util.List;

public class ListLoyStores {

    private List<LoyStore> stores;


    private List<Error> errors;

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public List<LoyStore> getStores() {
        return stores;
    }

    public void setStores(List<LoyStore> stores) {
        this.stores = stores;
    }
}
