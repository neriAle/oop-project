package server.api.rest;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonView;
import commons.views.View;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import commons.Event;
import server.services.EventService;
import server.services.WebSocketUpdateService;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final WebSocketUpdateService updateService;

    public EventController(EventService eventService, WebSocketUpdateService updateService) {
        this.eventService = eventService;
        this.updateService = updateService;
    }

    @GetMapping(path = {"", "/"})
    @JsonView(View.CommonsView.class)
    public List<Event> getAll() {
        return eventService.getAll();
    }

    @GetMapping("/{id}/basic")
    @JsonView(View.CommonsView.class)
    public ResponseEntity<Event> getByIdBasic(@PathVariable("id") UUID id) throws EntityNotFoundException {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @GetMapping("/{id}")
    @JsonView(View.OverviewView.class)
    @Cacheable(value = "events", key = "#id")
    public ResponseEntity<Event> getById(@PathVariable("id") UUID id) throws EntityNotFoundException {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @PostMapping(path = {"", "/"})
    @JsonView(View.CommonsView.class)
    public ResponseEntity<Event> create(@RequestBody Event event) {
        Event saved = eventService.add(event);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @JsonView(View.CommonsView.class)
    @CachePut(value = "events", key = "#id")
    public ResponseEntity<Event> update(@PathVariable("id") UUID id, @RequestBody Event event)
            throws EntityNotFoundException, IllegalArgumentException, NullPointerException {
        Event updated = eventService.update(id, event);
        updateService.sendUpdatedEvent(updated);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    @CacheEvict(value = "events", key = "#id")
    public ResponseEntity<Void> update(@PathVariable("id") UUID id)
            throws EntityNotFoundException, IllegalArgumentException {
        eventService.delete(id);
        return ResponseEntity.ok().build();
    }
}