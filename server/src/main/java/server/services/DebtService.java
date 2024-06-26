package server.services;

import commons.Debt;
import commons.Event;
import commons.Participant;
import commons.primary_keys.DebtPK;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import server.classes.ParticipantDebtPair;
import server.database.DebtRepository;

import java.util.*;

@Service
public class DebtService {

    private static final double THRESHOLD = 0.0005;

    private final DebtRepository debtRepo;

    public DebtService(DebtRepository debtRepo) {
        this.debtRepo = debtRepo;
    }

    public Optional<Debt> getOptionalById(DebtPK id) {
        Optional<Debt> od = debtRepo.findById(id);
        if (od.isEmpty()) {
            return od;
        }
        Debt newDebt = od.get();
        newDebt.setAmount(-newDebt.getAmount());
        return Optional.of(newDebt);
    }
    public List<Debt> getAll() {
        return debtRepo.findAll();
    }

    public Debt getById(DebtPK id) {
        Debt clone = clone(getByIdInner(id));
        clone.setAmount(-clone.getAmount());
        return clone;
    }

    public Collection<Debt> getByEventId(UUID eventId) {
        Collection<Debt> eventDebts = debtRepo.findDebtsByEventId(eventId);
        return eventDebts.stream().filter(d -> d.getAmount() < 0.0 && !d.getId().getPayerId().equals(d.getId().getDebtorId()))
                .map(d -> new Debt(d.getPayer(), d.getDebtor(), -d.getAmount(), d.getEvent())).toList();
    }

    private Collection<Debt> getByEventIdInner(UUID eventId) {
        return debtRepo.findDebtsByEventId(eventId);
    }

    private Debt getByIdInner(DebtPK id) {
        Optional<Debt> od = debtRepo.findById(id);
        if (od.isEmpty()) {
            throw new EntityNotFoundException("Did not find the specified debt.");
        }
        return od.get();
    }

    public Collection<Debt> getByPayerId(UUID id) {
        return debtRepo.findDebtsByPayerId(id);
    }

    public Collection<Debt> getByDebtorId(UUID id) {
        return debtRepo.findDebtsByDebtorId(id);
    }
    public void deleteAll(UUID eventId) {
        debtRepo.deleteDebtByEventId(eventId);
    }
    public Debt save(UUID eventId, Debt debt) {
        if (debt.getAmount() == null) {
            throw new IllegalArgumentException("Cannot add debt without amount.");
        }
        if (debt.getId().getPayerId().equals(debt.getId().getDebtorId())) {
            throw new IllegalArgumentException("Cannot add debt from participant to itself");
        }
        Event event = new Event();
        event.setId(eventId);
        debt.setEvent(event);
        return debtRepo.save(debt);
    }

    public Debt add(UUID eventId, Debt debt) {
        if (debt.getAmount() == null) {
            throw new IllegalArgumentException("Cannot add debt without amount.");
        }
        if (debtRepo.existsById(debt.getId())) {
            throw new IllegalArgumentException("Cannot add already existing debt.");
        }
        if (debt.getId().getPayerId().equals(debt.getId().getDebtorId())) {
            throw new IllegalArgumentException("Cannot add debt from participant to itself.");
        }
        Debt debtorToPayer = new Debt(debt.getDebtor(), debt.getPayer(), debt.getAmount());

        // Adds the debtor to payer with positive amount
        addOneDirectional(eventId, debtorToPayer);
        // Adds the actual debt
        debt.setAmount(-debt.getAmount());
        Debt returnVal = addOneDirectional(eventId, debt);

        Debt cloneSaved = clone(returnVal);
        cloneSaved.setAmount(-cloneSaved.getAmount());
        return cloneSaved;
    }

    private Debt clone(Debt debt) {
        Debt clone = new Debt();
        clone.setId(debt.getId());
        clone.setPayer(debt.getPayer());
        clone.setDebtor(debt.getDebtor());
        clone.setAmount(debt.getAmount());
        clone.setEvent(debt.getEvent());
        return clone;
    }

    private Debt addOneDirectional(UUID eventId, Debt debt) {
        Event event = new Event();
        event.setId(eventId);
        debt.setEvent(event);
        return debtRepo.save(debt);
    }

    public Debt update(UUID eventId, DebtPK id, Debt debt) {
        debt.setId(id);
        Debt repoDebt = getByIdInner(id);
        if (!repoDebt.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Event and Debt mismatch!");
        }
        if (debt.getAmount() == null) {
            throw new IllegalArgumentException("Cannot update without amount.");
        }
        if (debt.getId().getPayerId().equals(debt.getId().getDebtorId())) {
            throw new IllegalArgumentException("Cannot add debt from participant to itself.");
        }
        Optional<Debt> odtp = debtRepo.findById(new DebtPK(id.getDebtorId(), id.getPayerId()));
        Debt debtorToPayer;
        if (odtp.isEmpty()) {
            Debt newodtp = new Debt(repoDebt.getDebtor(), repoDebt.getPayer(), -repoDebt.getAmount(), repoDebt.getEvent());
            debtRepo.save(newodtp);
            debtorToPayer = newodtp;
        } else {
            debtorToPayer = getByIdInner(new DebtPK(id.getDebtorId(), id.getPayerId()));
        }
        debtorToPayer.setAmount(debt.getAmount());
        repoDebt.setAmount(-debt.getAmount());
        debtRepo.flush();
        Debt ret = clone(repoDebt);
        ret.setAmount(-ret.getAmount());
        return ret;
    }

    public Integer delete(DebtPK id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        DebtPK reverseDebt = new DebtPK(id.getDebtorId(), id.getPayerId());
        debtRepo.deleteDebtById(reverseDebt);
        Integer deletedRows = debtRepo.deleteDebtById(id);
        if (deletedRows != 1) {
            throw new EntityNotFoundException("Could not find the debt");
        }
        return deletedRows;
    }

    public void recalculate(UUID eventId) {
        Collection<Debt> debts = debtRepo.findDebtsByEventId(eventId);
        Map<Participant, Double> participantDebt = new HashMap<>();

        // Calculate individual debts
        for (Debt debt : debts) {
            Participant payer = debt.getPayer();
            if (!participantDebt.containsKey(payer)) {
                participantDebt.put(payer, 0.0);
            }
            Double amount = participantDebt.get(payer) + debt.getAmount();
            participantDebt.put(payer, amount);
        }

        // Clean up database
        debtRepo.deleteDebtByEventId(eventId);

        // Construct participant-debt array
        List<ParticipantDebtPair> participantDebtList = new ArrayList<>();
        for (var entry : participantDebt.entrySet()) {
            participantDebtList.add(new ParticipantDebtPair(entry.getKey(), entry.getValue()));
        }
        Collections.sort(participantDebtList);

        // Algorithm
        int left = 0;
        int right = participantDebtList.size() - 1;
        while (left < right) {
            ParticipantDebtPair debtorPair = participantDebtList.get(left);
            ParticipantDebtPair payerPair = participantDebtList.get(right);
            // Skip zero debts
            if (compareAmounts(debtorPair.getDebt(), 0.0) == 0) {
                left++;
                continue;
            }
            if (compareAmounts(payerPair.getDebt(), 0.0) == 0) {
                right--;
                continue;
            }
            Double debtorAmount = debtorPair.getDebt();
            Double payerAmount = Math.abs(payerPair.getDebt());
            Event event = new Event();
            event.setId(eventId);

            int comparison = compareAmounts(debtorAmount, payerAmount);
            if (comparison <= 0) {
                debtorPair.setDebt(0.0);
                payerPair.setDebt(payerAmount - debtorAmount);
                // Payer to Debtor
                addOneDirectional(eventId, new Debt(payerPair.getParticipant(), debtorPair.getParticipant(), -debtorAmount, event));
                // Debtor to Payer
                addOneDirectional(eventId, new Debt(debtorPair.getParticipant(), payerPair.getParticipant(), debtorAmount, event));
            } else {
                payerPair.setDebt(0.0);
                debtorPair.setDebt(debtorAmount - payerAmount);
                // Payer to Debtor
                addOneDirectional(eventId, new Debt(payerPair.getParticipant(), debtorPair.getParticipant(), -payerAmount, event));
                // Debtor to Payer
                addOneDirectional(eventId, new Debt(debtorPair.getParticipant(), payerPair.getParticipant(), payerAmount, event));
            }

            // Go to next payer/debtor pair
            if (compareAmounts(debtorPair.getDebt(), 0.0) == 0) left++;
            if (compareAmounts(payerPair.getDebt(), 0.0) == 0) right--;
        }
    }

    public Debt settle(UUID eventId, UUID payerId, UUID debtorId, Double amount) {
        Debt payerToDebtor = getByIdInner(new DebtPK(payerId, debtorId));
        Debt debtorToPayer = getByIdInner(new DebtPK(debtorId, payerId));
        if (!payerToDebtor.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Event and Debt mismatch!");
        }
        if (amount > debtorToPayer.getAmount()) {
            throw new IllegalArgumentException("Cannot settle for more than the amount");
        }
        Double newAmount = debtorToPayer.getAmount() - amount;
        payerToDebtor.setAmount(-newAmount);
        debtorToPayer.setAmount(newAmount);
        debtRepo.flush();
        return payerToDebtor;
    }

    private int compareAmounts(Double a, Double b) {
        if (Math.abs(a - b) <= THRESHOLD) {
            return 0;
        }
        return Double.compare(a, b);
    }

}
