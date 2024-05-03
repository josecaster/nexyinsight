package sr.we.entity.eclipsestore.tables;

import sr.we.entity.Webhook;

import java.util.List;

public class ListWebhooks {

    private List<Webhook> webhooks;


    private List<Error> errors;

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public List<Webhook> getWebhooks() {
        return webhooks;
    }

    public void setWebhooks(List<Webhook> webhooks) {
        this.webhooks = webhooks;
    }
}
