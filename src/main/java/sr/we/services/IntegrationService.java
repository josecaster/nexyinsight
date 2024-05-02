package sr.we.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sr.we.entity.Integration;
import sr.we.repository.IntegrationRepository;

import java.util.Optional;

@Service
public class IntegrationService {

    private final IntegrationRepository repository;

    public IntegrationService(IntegrationRepository repository) {
        this.repository = repository;
    }

    public Optional<Integration> get(Long id) {
        return repository.findById(id);
    }

    public Integration update(Integration entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Integration> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Integration> list(Pageable pageable, Specification<Integration> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
