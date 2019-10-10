package com.github.dreyman.emailsapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    @Autowired private SseService sseService;

    private List<Item> items;
    private AtomicInteger id;

    private static final long TIMEOUT = 240 * 1000L;

    public EmailController() {
        items = new ArrayList<>();
        id = new AtomicInteger(0);
    }

    @PostMapping("new")
    public Item addEmail(@RequestBody String email) {
        Item newItem = new Item(id.incrementAndGet(), email);
        items.add(newItem);
        sseService.pushItem(newItem);
        return newItem;
    }

    @GetMapping("all")
    public List<Item> getEmails() {
        return items;
    }

    @GetMapping("stream")
    public ResponseEntity<SseEmitter> emailsEventSource() {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        sseService.addEmitter(emitter);
        emitter.onCompletion(() -> sseService.removeEmitter(emitter));
        emitter.onTimeout(() -> sseService.removeEmitter(emitter));
        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }
}
