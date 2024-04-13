package server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import commons.BankAccount;
import commons.Tag;

import java.util.List;

public class ImportExportData {
    private List<BankAccount> bankAccounts;
    private List<EventBean> events;
    private List<DebtBean> debts;
    private List<ExpenseBean> expenses;
    private List<ParticipantBean> participants;
    private List<Tag> tags;
    private List<DebtorBean> debtors;

    @JsonCreator
    public ImportExportData(@JsonProperty("bankAccounts") List<BankAccount> bankAccounts,
                            @JsonProperty("events") List<EventBean> events,
                            @JsonProperty("debts") List<DebtBean> debts,
                            @JsonProperty("expenses") List<ExpenseBean> expenses,
                            @JsonProperty("participants") List<ParticipantBean> participants,
                            @JsonProperty("tags") List<Tag> tags,
                            @JsonProperty("debtor") List<DebtorBean> debtorBeans) {
        this.bankAccounts = bankAccounts;
        this.events = events;
        this.debts = debts;
        this.expenses = expenses;
        this.participants = participants;
        this.tags = tags;
        this.debtors = debtorBeans;
    }

    public List<BankAccount> getBankAccounts() {
        return bankAccounts;
    }

    public List<EventBean> getEvents() {
        return events;
    }

    public List<DebtBean> getDebts() {
        return debts;
    }

    public List<ExpenseBean> getExpenses() {
        return expenses;
    }

    public List<ParticipantBean> getParticipants() {
        return participants;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<DebtorBean> getDebtors() {
        return debtors;
    }
}