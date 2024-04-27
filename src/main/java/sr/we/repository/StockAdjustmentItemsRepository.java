package sr.we.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sr.we.entity.StockAdjustmentItems;

import java.util.List;

public interface StockAdjustmentItemsRepository extends JpaRepository<StockAdjustmentItems, Long>, JpaSpecificationExecutor<StockAdjustmentItems> {

    List<StockAdjustmentItems> findByStockAdjustmentId(Long stockAdjustmentId);

}
