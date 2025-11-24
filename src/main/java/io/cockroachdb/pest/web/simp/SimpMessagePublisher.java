package io.cockroachdb.pest.web.simp;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Profile("!offline")
public class SimpMessagePublisher {
    private static final int SEND_DELAY_MS = 1500;

    private static final int QUEUE_SIZE = 10;

    private static final class Message {
        TopicName topic;

        Object payload;

        int priority = Ordered.HIGHEST_PRECEDENCE;
    }

    // There's still no bounded priority blocking queue in the JDK
    private final PriorityBlockingQueue<Message> queue = new PriorityBlockingQueue<>(QUEUE_SIZE,
            Comparator.comparingInt(o -> o.priority));

    private final Semaphore enqueueSemaphore = new Semaphore(QUEUE_SIZE);

    private final Semaphore dequeueSemaphore = new Semaphore(0);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (dequeueSemaphore.tryAcquire()) {
                Message message = queue.poll();
                if (message != null) {
                    convertAndSendNow(message.topic, message.payload);
                }
                enqueueSemaphore.release();
            }
        }, SEND_DELAY_MS, SEND_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    public <T> void convertAndSendNow(TopicName topic) {
        convertAndSendNow(topic, "");
    }

    public <T> void convertAndSendNow(TopicName topic, Object payload) {
        simpMessagingTemplate.convertAndSend(topic.value, payload);
    }

    public <T> void convertAndSendLater(TopicName topic, Object payload) {
        convertAndSendLater(topic, payload, Ordered.LOWEST_PRECEDENCE);
    }

    public <T> void convertAndSendLater(TopicName topic, Object payload, int priority) {
        try {
            enqueueSemaphore.acquire();

            Message message = new Message();
            message.topic = topic;
            message.payload = payload;
            message.priority = priority;
            queue.add(message);

            dequeueSemaphore.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
