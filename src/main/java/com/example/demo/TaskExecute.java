package com.example.demo;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TaskExecute {

    private final Logger logger = LoggerFactory.getLogger(TaskExecute.class);

    private final ThreadPoolTaskExecutor taskExecutor;
    private final BlockingQueue<Runnable> userTaskQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger submittedTaskCount = new AtomicInteger(0);

    public static final int MAX_CONCURRENT_REQUESTS = 1;
    public static final int QUEUE_CAPACITY = 1; // Set your desired queue capacity

    public TaskExecute() {
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(1); // Set your desired core pool size
        this.taskExecutor.setMaxPoolSize(1); // Set your desired max pool size
        this.taskExecutor.setQueueCapacity(MAX_CONCURRENT_REQUESTS); // Set your desired max pool size
        this.taskExecutor.initialize();
    }

    @PostConstruct
    public void initialize() {
        // Start a background thread to process the task queue
        new Thread(this::processTaskQueue).start();
    }
    public void submitTask(Runnable task, HttpSession session) {
        if (!isServerBusy()) {
            userTaskQueue.offer(task);
            logger.info("Task submitted successfully");
        } else {
            userTaskQueue.offer(task);
            logger.warn("Task queue is full. Task is on hold.");
            session.setAttribute("error", "Your task is on hold. Please try again later.");
        }
    }
    
    
    
    public BlockingQueue<Runnable> getTaskQueue() {
        return userTaskQueue;
    }

    public int getActiveTaskCount() {
        return taskExecutor.getActiveCount();
    }

    public boolean isServerBusy() {
        return getActiveTaskCount() >= MAX_CONCURRENT_REQUESTS;
    }

    public void processTaskQueue() {
        while (true) {
            try {
                Runnable task = userTaskQueue.take();
                logger.info("Executing task from the main queue");
                taskExecutor.execute(task);
                submittedTaskCount.decrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Error processing task queue", e);
                break;
            }
        }
    }
}
