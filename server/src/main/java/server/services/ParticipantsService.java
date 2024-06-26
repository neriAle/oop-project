package server.services;

import commons.BankAccount;
import commons.Event;
import commons.Participant;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import server.database.BankAccountRepository;
import server.database.ParticipantRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParticipantsService {

    private final ParticipantRepository participantRepository;
    private final BankAccountRepository bankAccountRepository;
    public ParticipantsService(ParticipantRepository participantRepository, BankAccountRepository bankAccountRepository) {
        this.participantRepository = participantRepository;
        this.bankAccountRepository = bankAccountRepository;
    }
    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    private static void isValidParticipant(Participant participant) {
        if (isNullOrEmpty(participant.getNickname()))
            throw new IllegalArgumentException("Participant's attributes have null or empty values!");
    }
    public Participant getById(UUID id) throws EntityNotFoundException {
        Optional<Participant> oPa = participantRepository.findById(id);
        if (oPa.isEmpty()) {
            throw new EntityNotFoundException("Did not find the specified participant.");
        }
        return oPa.get();

    }
    public List<Participant> getAll() {
        return participantRepository.findAll();
    }
    public Participant updateParticipant(UUID eventId, UUID id, Participant participant)
            throws IllegalArgumentException, EntityNotFoundException {
        Participant repoParticipant = getById(id);
        if (!repoParticipant.getEvent().getId().equals(eventId))
            throw new IllegalArgumentException("Event and Participant mismatch!");
        if (participant.getNickname() != null) {
            repoParticipant.setNickname(participant.getNickname());
        }
        if (participant.getEmail() != null) {
            repoParticipant.setEmail(participant.getEmail());
        }
        if (participant.getBankAccount() != null && participant.getBankAccount().getIban().isEmpty())
            participant.setBankAccount(null);
        if (participant.getBankAccount() != null) {
            if (repoParticipant.getBankAccount() != null &&
                    repoParticipant.getBankAccount().getIban().equals(participant.getBankAccount().getIban())) {
                repoParticipant.getBankAccount().setBic(participant.getBankAccount().getBic());
            } else {
                repoParticipant.setBankAccount(participant.getBankAccount());
            }
        }
        participantRepository.flush();
        return repoParticipant;
    }
    public void deleteParticipant(UUID id) throws IllegalArgumentException, EntityNotFoundException {
        if (id == null)
            throw new IllegalArgumentException("Id cannot be null!");
        Integer deletedRows = participantRepository.deleteParticipantById(id);
        if (deletedRows != 1) {
            throw new EntityNotFoundException("Could not find the repo");
        }
    }

    public List<Participant> getParticipants(UUID eventId) {
        return participantRepository.findParticipantsByEventId(eventId);
    }
    public Participant addParticipant(UUID eventId, Participant participant) throws IllegalArgumentException {
        isValidParticipant(participant);
        Event event = new Event();
        event.setId(eventId);
        participant.setEvent(event);
        if (participant.getBankAccount() != null && participant.getBankAccount().getIban().isEmpty())
            participant.setBankAccount(null);
        if (participant.getBankAccount() != null) {
            Optional<BankAccount> oBankAccount = bankAccountRepository.findById(participant.getBankAccount().getIban());
            oBankAccount.ifPresent(participant::setBankAccount);
        }
        return participantRepository.save(participant);
    }
}
