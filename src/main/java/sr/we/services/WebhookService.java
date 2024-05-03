package sr.we.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sr.we.entity.Webhook;
import sr.we.entity.eclipsestore.tables.ListWebhooks;
import sr.we.integration.LoyWebhookController;
import sr.we.repository.WebhookRepository;

import java.io.IOException;
import java.util.Optional;

@Service
public class WebhookService {

    private final WebhookRepository repository;
    private final LoyWebhookController loyWebhookController;

    public WebhookService(WebhookRepository repository, LoyWebhookController loyWebhookController) {
        this.repository = repository;
        this.loyWebhookController = loyWebhookController;
    }

    public Optional<Webhook> get(Long id) {
        return repository.findById(id);
    }

    @Value("${sr.we.webhook.url}")
    private String url;

    public Webhook update(Webhook entity) throws IOException {

        // here we will write some code to communicate with loyverse
        if(entity == null){
            return null;
        }
        Long businessId = entity.getBusinessId();
        if(StringUtils.isBlank(entity.getId())){

            entity.setUrl(entity.getTypee().getUrl(url));

            entity = loyWebhookController.add(entity);
            entity.setBusinessId(businessId);
        } else {
            Webhook webhook = loyWebhookController.get(entity.getId());
            if(webhook == null){
                // has been deleted
                entity.setId(null);
            }
            entity = loyWebhookController.add(entity);
            entity.setBusinessId(businessId);
        }

        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Webhook> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Webhook> list(Pageable pageable, Specification<Webhook> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
