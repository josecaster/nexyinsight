package sr.we.schedule;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import sr.we.entity.Task;
import sr.we.repository.TaskRepository;
import sr.we.services.TaskService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class MainTaskTrigger implements Trigger {

    private final Long businessId;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public MainTaskTrigger(Long businessId, TaskRepository taskRepository, TaskService taskService) {
        this.businessId = businessId;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    public Long getBusinessId() {
        return businessId;
    }

    @Override
    public Instant nextExecution(TriggerContext triggerContext) {
        Task byTypeAndBusinessId = taskRepository.getByTypeAndBusinessId(Task.Type.MAIN, getBusinessId());
        if (byTypeAndBusinessId == null) {
            byTypeAndBusinessId = new Task();
            byTypeAndBusinessId.setType(Task.Type.MAIN);
            byTypeAndBusinessId.setEnabled(true);
            byTypeAndBusinessId.setBusinessId(getBusinessId()); // creating a new scheduler so the app starts by syncing
        }
        if (byTypeAndBusinessId.getEnabled()) {
            Instant now = null;
            if (triggerContext.lastActualExecution() == null) {
                now = Instant.now();
                byTypeAndBusinessId.setMaxTime(LocalDateTime.ofInstant(Objects.requireNonNull(now), ZoneOffset.UTC).withHour(0).withSecond(0).withMinute(0).withNano(0).plusHours(24));
            } else {
                now = Instant.from(LocalDateTime.ofInstant(Objects.requireNonNull(triggerContext.lastActualExecution()), ZoneOffset.UTC).withHour(0).withSecond(0).withMinute(0).withNano(0).plusHours(24));
                byTypeAndBusinessId.setMaxTime(LocalDateTime.ofInstant(now, ZoneOffset.UTC));
            }

            taskService.update(byTypeAndBusinessId);
            return now;
        }
        return null;// this will stop the thread
    }
}
