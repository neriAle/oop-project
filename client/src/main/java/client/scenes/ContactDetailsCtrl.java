package client.scenes;

import client.utils.ServerUtils;
import commons.BankAccount;
import commons.Event;
import commons.Participant;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ContactDetailsCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField ibanField;
    @FXML
    private TextField bicField;
    @FXML
    private Label topLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label ibanLabel;
    @FXML
    private Label bicLabel;
    @FXML
    private Button addParticipantButton;
    @FXML
    private Button cancelButton;
    @FXML
    private ComboBox<Participant> editSelectorComboBox;

    private Event parentEvent;
    private ObservableList<Participant> participants;

    // Will be used to bind text for translations
    private StringProperty actionBtnText;
    private StringProperty topLabelText;

    @Inject
    public ContactDetailsCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.participants = FXCollections.observableArrayList();
    }

    public void confirmAction() {
        System.out.println("Created Participant"); // To print that it is updated in case of edit
        String name = nameField.getText();
        String email = emailField.getText();
        String iban = ibanField.getText();
        String bic = bicField.getText();
        clearText();
        Participant newParticipant = new Participant(name, email, new BankAccount(iban, bic));
        newParticipant.setEvent(parentEvent);
        server.addParticipant(newParticipant, parentEvent.getId());
        System.out.println(List.of(name, email, iban, bic));
        mainCtrl.closeDialog();
    }

    public void setParentEvent(Event event) {
        this.parentEvent = event;
        this.participants.setAll(parentEvent.getParticipants());
    }

    public void setAddMode() {
        this.editSelectorComboBox.setVisible(false);
        this.setFieldsDisabled(false);
        this.topLabel.setText("Add New Participant");
        this.addParticipantButton.setText("Add Participant");
    }

    public void setEditMode() {
        this.editSelectorComboBox.setVisible(true);
        this.setFieldsDisabled(true);
        this.topLabel.setText("Edit Participant");
        this.addParticipantButton.setText("Save");
    }

    public void selectEditParticipant() {
        Participant selected = this.editSelectorComboBox.getValue();
        if (selected == null) {
            setFieldsDisabled(true);
            return;
        }
        setFieldsDisabled(false);
        setFieldData(selected);
    }

    public void cancel() {
        clearText();
        mainCtrl.closeDialog();
    }

    private void clearText() {
        nameField.setText("");
        emailField.setText("");
        ibanField.setText("");
        bicField.setText("");
    }

    private void setFieldsDisabled(boolean disabled) {
        nameField.setDisable(disabled);
        emailField.setDisable(disabled);
        ibanField.setDisable(disabled);
        bicField.setDisable(disabled);
    }

    private void setFieldData(Participant participant) {
        System.out.println(participant);
        nameField.setText(participant.getNickname());
        emailField.setText(participant.getEmail());
//        ibanField.setText(participant.getBankAccount().getIban());
//        bicField.setText(participant.getBankAccount().getBic());
    }


    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.editSelectorComboBox.setItems(participants);
        this.editSelectorComboBox.setCellFactory(new Callback<ListView<Participant>, ListCell<Participant>>() {
            @Override
            public ListCell<Participant> call(ListView<Participant> param) {
                return getParticipantListCell();
            }
        });
        this.editSelectorComboBox.setButtonCell(getParticipantListCell());
    }

    private static ListCell<Participant> getParticipantListCell() {
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
}
