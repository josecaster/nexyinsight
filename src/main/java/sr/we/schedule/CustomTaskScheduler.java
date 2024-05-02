package sr.we.schedule;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Component
public class CustomTaskScheduler extends ThreadPoolTaskScheduler {
    private final Map<Object, ScheduledFuture<?>> scheduledTasks = new IdentityHashMap<>();

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        ScheduledFuture<?> future = super.schedule(task, trigger);
        int i = getI(task);
        cancelSchedule(i);
        scheduledTasks.put(i, future);
        System.out.println("Schedules["+scheduledTasks+"]");
        return future;
    }

    private static int getI(Runnable task) {
        int i = 0;
        if(task instanceof JobbyLauncher){
            i = 1;
        } else if (task instanceof RefreshTokenLauncher){// given the app is run per business you dont have to send additional id
            i = 2;
        }
        return i;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
        ScheduledFuture<?> future = super.schedule(task, startTime);
        int i = getI(task);
        cancelSchedule(i);
        scheduledTasks.put(i, future);
        System.out.println("Schedules["+scheduledTasks+"]");
        return future;
    }

    public void cancelSchedule(int i) {
        ScheduledFuture<?> scheduledFuture = scheduledTasks.get(i);
        if(scheduledFuture != null){
            while(!scheduledFuture.isCancelled()){
                scheduledFuture.cancel(false);
                System.out.println("Waiting on task to cancel");
            }
        }
    }
}