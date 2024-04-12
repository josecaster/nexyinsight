package sr.we.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sr.we.data.SyncTimeRepository;
import sr.we.entity.SyncTime;

import java.util.Optional;

@Service
public class SyncTimeService {

    private final SyncTimeRepository repository;

    public SyncTimeService(SyncTimeRepository repository) {
        this.repository = repository;
    }

    public Optional<SyncTime> get(Long id) {
        return repository.findById(id);
    }

    public SyncTime update(SyncTime entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<SyncTime> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<SyncTime> list(Pageable pageable, Specification<SyncTime> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
