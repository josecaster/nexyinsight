package sr.we.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sr.we.entity.StockAdjustmentItems;
import sr.we.repository.StockAdjustmentItemsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class StockAdjustmentItemsService {

    private final StockAdjustmentItemsRepository repository;

    public StockAdjustmentItemsService(StockAdjustmentItemsRepository repository) {
        this.repository = repository;
    }

    public Optional<StockAdjustmentItems> get(Long id) {
        return repository.findById(id);
    }

    public List<StockAdjustmentItems> findByStockAdjustmentId(Long StockAdjustmentId) {
        return repository.findByStockAdjustmentId(StockAdjustmentId);
    }

    public StockAdjustmentItems update(StockAdjustmentItems entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<StockAdjustmentItems> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<StockAdjustmentItems> list(Pageable pageable, Specification<StockAdjustmentItems> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public void update(Long id, List<StockAdjustmentItems> list) {
        for (StockAdjustmentItems StockAdjustmentItems : list) {
            update(StockAdjustmentItems);
        }
    }
}
