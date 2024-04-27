package sr.we.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sr.we.entity.StockAdjustment;
import sr.we.repository.StockAdjustmentRepository;

import java.io.IOException;
import java.util.Optional;

@Service
public class StockAdjustmentService {

    private final StockAdjustmentRepository repository;

    @Value("${sr.we.loyverse.token}")
    private String loyverseToken;


    @Value("${sr.we.business.id}")
    private Long businessId;

    public StockAdjustmentService(StockAdjustmentRepository repository) {
        this.repository = repository;
    }

    public Optional<StockAdjustment> get(Long id) {
        return repository.findById(id);
    }

    public StockAdjustment update(StockAdjustment entity) throws IOException {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<StockAdjustment> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<StockAdjustment> list(Pageable pageable, Specification<StockAdjustment> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
