package sr.we.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sr.we.data.BatchItemsRepository;
import sr.we.entity.BatchItems;

import java.util.List;
import java.util.Optional;

@Service
public class BatchItemsService {

    private final BatchItemsRepository repository;

    public BatchItemsService(BatchItemsRepository repository) {
        this.repository = repository;
    }

    public Optional<BatchItems> get(Long id) {
        return repository.findById(id);
    }

    public List<BatchItems> findByBatchId(Long batchId) {
        return repository.findByBatchId(batchId);
    }

    public BatchItems update(BatchItems entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<BatchItems> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<BatchItems> list(Pageable pageable, Specification<BatchItems> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public void update(Long id, List<BatchItems> list) {
        for(BatchItems batchItems : list){
            update(batchItems);
        }
    }
}
