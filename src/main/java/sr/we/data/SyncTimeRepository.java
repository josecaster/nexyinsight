package sr.we.data;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sr.we.entity.SyncTime;

public interface SyncTimeRepository extends JpaRepository<SyncTime, Long>, JpaSpecificationExecutor<SyncTime> {

    SyncTime getByTypeAndBusinessId(SyncTime.SyncType type, Long businessId);

}
