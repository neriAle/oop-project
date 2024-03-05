package server.services;

import commons.Event;
import commons.Participant;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import server.database.EventRepository;
import server.database.ParticipantRepository;

import java.util.List;
import java.util.UUID;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;

    public EventService(EventRepository eventRepository, ParticipantRepository participantRepository) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    private static void isValidParticipant(Participant participant) {
        if(participant == null)
            throw new NullPointerException("Participant is null");
        if (participant.getId() != null)
            throw new IllegalArgumentException("Id is auto-generated, should not be given as parameter");
        if (isNullOrEmpty(participant.getFirstName()) || isNullOrEmpty(participant.getLastName())
                || isNullOrEmpty(participant.getEmail()))
            throw new IllegalArgumentException("Participant's attributes have null or empty values!");
    }

    private static void isValidEvent(Event event) {
        if(event == null)
            throw new NullPointerException("Event is null");
        if (event.getId() != null)
            throw new IllegalArgumentException("Id is auto-generated, should not be given as parameter");
        if (isNullOrEmpty(event.getName()))
            throw new IllegalArgumentException("Event's name has a null or empty value!");
    }

    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    public Event getById(UUID id) {
        if(!eventRepository.existsById(id))
            throw new EntityNotFoundException("The event you are looking for does not exist!");
        return eventRepository.findEventById(id);
    }

    public Event add(Event event) {
        isValidEvent(event);
        return eventRepository.save(event);
    }

    public Event update(UUID id, Event event) {
        isValidEvent(event);
        if(id == null || !eventRepository.existsById(id))
            throw new EntityNotFoundException("The event you are looking for does not exist!");
        Event repoEvent = eventRepository.findEventById(id);
        repoEvent.setExpenses(event.getExpenses());
        repoEvent.setParticipants(event.getParticipants());
        repoEvent.setName(event.getName());
        repoEvent.setTags(event.getTags());
        return eventRepository.save(repoEvent);
    }

    public Event addParticipant(Participant participant, UUID eventID) {
        isValidParticipant(participant);
        if (eventID == null || !eventRepository.existsById(eventID))
            throw new IllegalArgumentException("The event you are looking for does not exist!");
        Event event = eventRepository.findEventById(eventID);
        participant.setEvent(event);
        participantRepository.save(participant);
        event.addParticipant(participant);
        return eventRepository.save(event);
    }
}
