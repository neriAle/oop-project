package client.scenes;

import client.uicomponents.Alerts;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.BankAccount;
import commons.Participant;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.net.URL;
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
    @FXML
    private HBox actionBtnHBox;
    private Button deleteButton;

    private Participant toBeUpdatedParticipant;
    private boolean editMode;

    // Will be used to bind text for translations
    private final StringProperty addLabelText = new SimpleStringProperty();
    private final StringProperty editLabelText = new SimpleStringProperty();
    private final StringProperty addBtnText = new SimpleStringProperty();
    private final StringProperty saveBtnText = new SimpleStringProperty();

    @Inject
    public ContactDetailsCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    public void confirmAction() {
        if (!dataCheck().equals("valid")) {
            Alerts.invalidParticipantAlert(dataCheck());
            return;
        }
        Participant newParticipant = this.createParticipantFromFields();
        this.clearText();
        newParticipant.setEvent(mainCtrl.getEvent());
        if (!editMode) { // add mode
            server.addParticipant(newParticipant, mainCtrl.getEvent().getId());
        } else { // edit mode
            server.updateParticipant(newParticipant, mainCtrl.getEvent().getId(), toBeUpdatedParticipant.getId());
        }
        clearText();
        editSelectorComboBox.setValue(null);
        mainCtrl.closeDialog();
    }

    private String dataCheck() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) return "Name is required";
        if ((ibanField.getText() == null || ibanField.getText().trim().isEmpty())
            && (bicField.getText() != null && !bicField.getText().trim().isEmpty()))
                return "Both IBAN and BIC are required to save a Bank Account";
        if ((bicField.getText() == null || bicField.getText().trim().isEmpty())
            && (ibanField.getText() != null && !ibanField.getText().trim().isEmpty()))
            return "Both IBAN and BIC are required to save a Bank Account";
        return "valid";
    }

    private Participant createParticipantFromFields() {
        String name = nameField.getText();
        String email = emailField.getText();
        String iban = ibanField.getText();
        String bic = bicField.getText();
        return new Participant(name, email, new BankAccount(iban, bic));
    }

    public void setAddMode() {
        this.clearText();
        this.editMode = false;
        this.editSelectorComboBox.setVisible(false);
        this.setFieldsDisabled(false);
        this.topLabel.textProperty().bind(addLabelText);
        this.addParticipantButton.textProperty().bind(addBtnText);
        this.actionBtnHBox.getChildren().remove(deleteButton);
    }

    public void setEditMode() {
        this.clearText();
        this.editMode = true;
        this.editSelectorComboBox.setVisible(true);
        this.setFieldsDisabled(true);
        this.topLabel.textProperty().bind(editLabelText);
        this.addParticipantButton.textProperty().bind(saveBtnText);
        if (!this.actionBtnHBox.getChildren().contains(deleteButton)) {
            this.actionBtnHBox.getChildren().add(1, deleteButton);
        }
    }

    public void selectEditParticipant() {
        if (!editMode) return;
        Participant selected = this.editSelectorComboBox.getValue();
        if (selected == null) {
            setFieldsDisabled(true);
            return;
        }
        selected = server.getParticipant(mainCtrl.getEvent().getId(), selected.getId());
        this.toBeUpdatedParticipant = selected;
        setFieldsDisabled(false);
        setFieldData(selected);
    }

    public void cancel() {
        clearText();
        editSelectorComboBox.setValue(null);
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
        nameField.setText(participant.getNickname());
        emailField.setText(participant.getEmail());
        BankAccount bankAccount = participant.getBankAccount();
        if (bankAccount == null) {
            ibanField.setText("");
            bicField.setText("");
            return;
        }
        ibanField.setText(bankAccount.getIban());
        bicField.setText(bankAccount.getBic());
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
        this.editSelectorComboBox.setCellFactory(new Callback<ListView<Participant>, ListCell<Participant>>() {
            @Override
            public ListCell<Participant> call(ListView<Participant> param) {
                return getParticipantListCell();
            }
        });
        this.editSelectorComboBox.setButtonCell(getParticipantListCell());

        this.addLabelText.bind(mainCtrl.getLanguageUtils().getBinding("contact.addParticipantLabel"));
        this.editLabelText.bind(mainCtrl.getLanguageUtils().getBinding("contact.editParticipantLabel"));
        this.saveBtnText.bind(mainCtrl.getLanguageUtils().getBinding("contact.saveBtnText"));
        this.addBtnText.bind(mainCtrl.getLanguageUtils().getBinding("contact.addBtnText"));
        this.cancelButton.textProperty().bind(mainCtrl.getLanguageUtils().getBinding("contact.cancelBtn"));
        this.nameLabel.textProperty().bind(mainCtrl.getLanguageUtils().getBinding("contact.nameLabel"));
        this.emailLabel.textProperty().bind(mainCtrl.getLanguageUtils().getBinding("contact.emailLabel"));
        this.ibanLabel.textProperty().bind(mainCtrl.getLanguageUtils().getBinding("contact.ibanLabel"));
        this.bicLabel.textProperty().bind(mainCtrl.getLanguageUtils().getBinding("contact.bicLabel"));

        this.deleteButton = new Button();
        this.deleteButton.textProperty().bind(mainCtrl.getLanguageUtils().getBinding("contact.deleteBtnText"));
        this.actionBtnHBox.getChildren().add(1, deleteButton);
        this.deleteButton.setOnAction(eventClick -> {
            if (toBeUpdatedParticipant == null) return;
            boolean choice = Alerts.deleteParticipantAlert(toBeUpdatedParticipant);
            if (!choice) return;
            server.deleteParticipant(mainCtrl.getEvent().getId(), toBeUpdatedParticipant.getId());
            clearText();
            mainCtrl.closeDialog();
        });
        this.deleteButton.setTextFill(Color.RED);
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
    public void startup() {
        this.editSelectorComboBox.setItems(mainCtrl.getParticipantList());
    }
}
