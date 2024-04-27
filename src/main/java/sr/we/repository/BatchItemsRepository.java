package sr.we.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sr.we.entity.Batch;
import sr.we.entity.BatchItems;

import java.util.List;

public interface BatchItemsRepository extends JpaRepository<BatchItems, Long>, JpaSpecificationExecutor<BatchItems> {

    List<BatchItems> findByBatchId(Long batchId);

}
