package sr.we.schedule;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.SyncTime;
import sr.we.entity.eclipsestore.tables.*;
import sr.we.integration.*;
import sr.we.repository.IntegrationRepository;
import sr.we.repository.SyncTimeRepository;
import sr.we.repository.TaskRepository;
import sr.we.services.IntegrationService;
import sr.we.services.TaskService;
import sr.we.storage.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class RefreshTokenLauncher implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenLauncher.class);
    private final TaskScheduler taskScheduler;
    private final AuthController integrationService;
    private final IntegrationRepository integrationRepository;
    @Value("${sr.we.loyverse.token}")
    private String loyverseToken;
    @Value("${sr.we.business.id}")
    private Long businessId;

    public RefreshTokenLauncher(TaskScheduler taskScheduler, AuthController integrationService, IntegrationRepository integrationRepository) {
        this.taskScheduler = taskScheduler;
        this.integrationService = integrationService;
        this.integrationRepository = integrationRepository;
    }

    @PostConstruct
    public void init() {
        taskScheduler.schedule(this, new RefreshTokenTaskTrigger(businessId, integrationRepository));
    }

    @Override
    public void run() {
        integrationService.authorize(businessId, true);// this will refresh the token
    }

}
