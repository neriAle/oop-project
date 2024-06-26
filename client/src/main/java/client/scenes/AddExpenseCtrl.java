package client.scenes;

import client.uicomponents.Alerts;
import client.uicomponents.PastDateCell;
import client.utils.Config;
import client.utils.ConfigUtils;
import client.utils.LanguageUtils;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.*;

public class AddExpenseCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private ConfigUtils utils;
    public ToggleGroup split;
    private final LanguageUtils languageUtils;
    private final Config config;
    @FXML
    private ComboBox<Participant> payer;
    @FXML
    private TextField description;
    @FXML
    private TextField amount;
    @FXML
    private DatePicker date;
    @FXML
    private ComboBox<Tag> tag;
    @FXML
    private RadioButton equallySplit;
    @FXML
    private RadioButton partialSplit;
    @FXML
    private ListView<Participant> debtorsList;
    @FXML
    private ListView<Participant> selectedDebtors;
    @FXML
    private Button add;
    @FXML
    private Button cancel;
    @FXML
    private Label whoPaid;
    @FXML
    private Label addEditExpense;
    @FXML
    private Label whatFor;
    @FXML
    private Label howMuch;
    @FXML
    private Label when;
    @FXML
    private Label howToSplit;
    @FXML
    private Label expenseType;
    @FXML
    private Button addTag;
    @FXML
    private Label currency;
    private boolean editMode;
    private Expense toUpdate;

    @Inject
    public AddExpenseCtrl(ServerUtils server, MainCtrl mainCtrl, ConfigUtils utils, Config config, LanguageUtils languageUtils) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.utils = utils;
        this.config = config;
        this.languageUtils = languageUtils;
    }

    public void cancel() {
        clearFields();
        mainCtrl.showOverview();
    }

    public void clearFields() {
        payer.getSelectionModel().clearSelection();
        description.clear();
        amount.clear();
        date.setValue(null);
        tag.getSelectionModel().clearSelection();
        equallySplit.setSelected(false);
        partialSplit.setSelected(false);
        selectedDebtors.getItems().clear();
    }
    public void ok() {
        String valid = validInput();
        if (!valid.equals("valid")) {
            Alerts.invalidExpenseAlert(valid);
            return;
        }
        double amt = Double.parseDouble(amount.getText());
        String desc = description.getText();
        LocalDateTime time = date.getValue().atStartOfDay();
        Participant pay = payer.getValue();
        Collection<Participant> debt;
        if (equallySplit.isSelected()) {
            debt = mainCtrl.getParticipantList();
        } else {
            debt = mainCtrl.getParticipantList().stream()
                    .filter(p -> selectedDebtors.getItems().contains(p))
                    .toList();
        }
        Expense expense;
        if (tag.getSelectionModel().isEmpty()) {
            expense = new Expense(amt, desc, time, pay, debt);
        } else {
            expense = new Expense(amt, desc, time, pay, debt, tag.getValue());
        }
        if (editMode) {
            server.updateExpense(mainCtrl.getEvent().getId(), toUpdate.getId(), expense);
        } else {
            server.addExpense(mainCtrl.getEvent().getId(), expense);
        }

        cancel();
    }

    public String validInput() {
        if (payer.getSelectionModel().isEmpty()) return "Payer can't be empty";
        if (description.getText().isEmpty()) return "Description can't be empty";
        if (amount.getText().isEmpty()) return "Amount can't be empty";
        if (!NumberUtils.isCreatable(amount.getText())) return "Amount must be a number";
        if (Double.parseDouble(amount.getText()) <= 0) return "Amount can't be less than 0";
        if (date == null || date.getValue() == null) return "Date can't be empty";
        if (date.getValue().isAfter(ChronoLocalDate.from(LocalDateTime.now()))) return "Date can't be after now";
        if (!equallySplit.isSelected() && !partialSplit.isSelected()) return "Debtors must be selected";
        if (partialSplit.isSelected() && selectedDebtors.getItems().isEmpty()) return "Debtors must be selected";
        return "valid";
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ENTER:
                ok();
                break;
            case ESCAPE:
                cancel();
                break;
            default:
                break;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        payer.setCellFactory(participantListView -> getParticipantListCell());
        payer.setButtonCell(getParticipantListCell());
        tag.setCellFactory(tagListView -> getTagListCell());
        tag.setButtonCell(getTagListCell());
        debtorsList.setCellFactory(participantListView -> getParticipantListCell());
        selectedDebtors.setCellFactory(participantListView -> getParticipantListCell());
        this.add.textProperty().bind(languageUtils.getBinding("addExpense.addBtn"));
        this.cancel.textProperty().bind(languageUtils.getBinding("addExpense.cancelBtn"));
        this.whoPaid.textProperty().bind(languageUtils.getBinding("addExpense.whoPaidLabel"));
        this.addEditExpense.textProperty().bind(languageUtils.getBinding("addExpense.addExpenseLabel"));
        this.whatFor.textProperty().bind(languageUtils.getBinding("addExpense.whatForLabel"));
        this.howMuch.textProperty().bind(languageUtils.getBinding("addExpense.howMuchLabel"));
        this.when.textProperty().bind(languageUtils.getBinding("addExpense.whenLabel"));
        this.howToSplit.textProperty().bind(languageUtils.getBinding("addExpense.howToSplitLabel"));
        this.expenseType.textProperty().bind(languageUtils.getBinding("addExpense.expenseTypeLabel"));
        this.equallySplit.textProperty().bind(languageUtils.getBinding("addExpense.equallyRbtn"));
        this.partialSplit.textProperty().bind(languageUtils.getBinding("addExpense.partialSplitRbtn"));
        this.date.setDayCellFactory(datePicker -> new PastDateCell());
        this.debtorsList.managedProperty().bind(this.debtorsList.visibleProperty());
        this.selectedDebtors.managedProperty().bind(this.selectedDebtors.visibleProperty());
        this.debtorsList.visibleProperty().bind(partialSplit.selectedProperty());
        this.selectedDebtors.visibleProperty().bind(partialSplit.selectedProperty());
        this.currency.setText("€");

        switch (config.getLocale().getLanguage()) {
            case "nl":
                languageUtils.setLang("nl");
                break;
            case "en":
                languageUtils.setLang("en");
                break;
            case "ro":
                languageUtils.setLang("ro");
                break;
            default:
                languageUtils.setLang("en");
        }
    }

    public void selectDebtor() {
        List<Participant> alreadySelected = new ArrayList<>(selectedDebtors.getItems());
        Participant selected = debtorsList.getSelectionModel().getSelectedItem();
        if (alreadySelected.contains(selected)) {
            alreadySelected.remove(selected);
        } else {
            alreadySelected.add(selected);
        }
        selectedDebtors.setItems(FXCollections.observableArrayList(alreadySelected));
    }

    public void keyPressedDebtors(KeyEvent e) {
        if (e != null && (e.getCode()) == KeyCode.ENTER) {
            selectDebtor();
        }
    }

    private ListCell<Participant> getParticipantListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Participant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getNickname());
                }
            }
        };
    }
    private ListCell<Tag> getTagListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());
                }
            }
        };
    }

    public void addMode() {
        this.editMode = false;
        clearFields();
        this.add.textProperty().bind(languageUtils.getBinding("addExpense.addBtn"));
        this.addEditExpense.textProperty().bind(languageUtils.getBinding("addExpense.addExpenseLabel"));
    }

    public void editMode(Expense expense) {
        this.editMode = true;
        this.toUpdate = expense;
        clearFields();
        this.add.textProperty().bind(languageUtils.getBinding("addExpense.editBtn"));
        this.addEditExpense.textProperty().bind(languageUtils.getBinding("addExpense.editExpenseLabel"));

        payer.getSelectionModel().select(toUpdate.getPayer());
        description.setText(toUpdate.getTitle());
        amount.setText(String.valueOf(toUpdate.getAmount()));
        date.setValue(toUpdate.getDate().toLocalDate());
        if (toUpdate.getTag() != null) {
            tag.getSelectionModel().select(toUpdate.getTag());
        }
        if (toUpdate.getDebtors().containsAll(mainCtrl.getParticipantList())) {
            equallySplit.setSelected(true);
            partialSplit.setSelected(false);
        } else {
            equallySplit.setSelected(false);
            partialSplit.setSelected(true);
            selectedDebtors.getItems().addAll(toUpdate.getDebtors());
        }
    }

    public void startup() {
        tag.setItems(mainCtrl.getTagList());
        payer.setItems(mainCtrl.getParticipantList());
        debtorsList.setItems(mainCtrl.getParticipantList());
    }
}