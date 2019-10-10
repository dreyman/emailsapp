package com.github.dreyman.emailsapp;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SseService {

    private List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public void addEmitter(final SseEmitter emitter) {
        emitters.add(emitter);
    }

    public void removeEmitter(final SseEmitter emitter) {
        emitters.remove(emitter);
    }

    public void pushItem(Item item) {
        ListIterator<SseEmitter> it = emitters.listIterator();
        while (it.hasNext()) {
            SseEmitter emitter = it.next();
            executorService.submit(() -> {
                try {
                    emitter.send(item, MediaType.APPLICATION_JSON);
                } catch (Exception e) {
                    it.remove();
                }
            });
        }
    }

    private void sendItem(SseEmitter emitter, Item item) throws IOException {
        emitter.send(item, MediaType.APPLICATION_JSON);
    }
}
