package sr.we.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sr.we.data.BatchRepository;
import sr.we.entity.Batch;

import java.util.Optional;

@Service
public class BatchService {

    private final BatchRepository repository;

    public BatchService(BatchRepository repository) {
        this.repository = repository;
    }

    public Optional<Batch> get(Long id) {
        return repository.findById(id);
    }

    public Batch update(Batch entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Batch> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Batch> list(Pageable pageable, Specification<Batch> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
