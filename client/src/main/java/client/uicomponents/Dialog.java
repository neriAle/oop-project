package client.uicomponents;

import atlantafx.base.theme.Styles;
import client.Main;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class Dialog extends VBox {
    @FXML
    private ComboBox<Participant> payer;
    @FXML
    private ComboBox<Participant> paidFor;
    @FXML
    private ComboBox<Tag> tags;
    @FXML
    private ComboBox<Boolean> period;
    @FXML
    private DatePicker date;
    @FXML
    private Button resetPayer;
    @FXML
    private Button resetPaidFor;
    @FXML
    private Button resetTags;
    @FXML
    private Button resetDate;

    private ObjectBinding<Predicate<Expense>> predicate;
    private ObjectBinding<Boolean> isEmpty;
    private ObjectBinding<Predicate<Expense>> payerPredicate;
    private ObjectBinding<Predicate<Expense>> paidForPredicate;
    private ObjectBinding<Predicate<Expense>> tagPredicate;
    private ObjectBinding<Predicate<Expense>> datePredicate;

    public Dialog() {
        super();
//        setSpacing(10);
        setMinSize(300, 274);
        setMaxSize(300, 274);
        setStyle("-fx-background-color: -color-bg-default;");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/components/Dialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(Dialog.this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        init();
    }

    public void start(ObservableList<Participant> participants) {
        payer.setItems(participants);
        paidFor.setItems(participants);
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
    private ListCell<Boolean> getPeriodListView() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item ? "After" : "Before" );
                }
            }
        };
    }

    public void init() {
        resetPayer.setOnAction(e -> resetPayer());
        resetPaidFor.setOnAction(e -> resetPaidFor());
        resetTags.setOnAction(e -> resetTags());
        resetDate.setOnAction(e -> resetDate());
        resetPayer.setGraphic(new FontIcon(Feather.X_SQUARE));
        resetPaidFor.setGraphic(new FontIcon(Feather.X_SQUARE));
        resetTags.setGraphic(new FontIcon(Feather.X_SQUARE));
        resetDate.setGraphic(new FontIcon(Feather.X_SQUARE));
        resetPayer.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER);
        resetPaidFor.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER);
        resetTags.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER);
        resetDate.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER);
        date.setDayCellFactory(datePicker1 -> new PastDateCell());
        payer.setCellFactory(participantListView -> getParticipantListCell());
        payer.setButtonCell(getParticipantListCell());
        paidFor.setCellFactory(participantListView -> getParticipantListCell());
        paidFor.setButtonCell(getParticipantListCell());
        period.getItems().add(Boolean.FALSE);
        period.getItems().add(Boolean.TRUE);
        period.setButtonCell(getPeriodListView());
        period.setCellFactory(booleanListView -> getPeriodListView());
        payerPredicate = Bindings.createObjectBinding(
                () -> expense -> {
                    if (payer.getValue() != null)
                        return expense.getPayer().getId().equals(payer.getValue().getId());
                    return true;
                }, payer.valueProperty()
        );
        paidForPredicate = Bindings.createObjectBinding(
                () -> expense -> {
                    if (paidFor.getValue() != null)
                        return expense.getDebtors().contains(paidFor.getValue());
                    return  true;
                }, paidFor.valueProperty()
        );
        tagPredicate = Bindings.createObjectBinding(
                () -> expense -> {
                    if(tags.getValue() != null)
                        return expense.getTags().contains(tags.getValue());
                    return true;
                }, tags.valueProperty()
        );
        datePredicate = Bindings.createObjectBinding(
                () -> expense -> {
                    if(date.getValue() == null)
                        return true;
                    if (period.getValue() == null) {
                        return true;
                    }
                    if (!period.getValue()) {
                        return expense.getDate().isBefore(date.getValue().atStartOfDay());
                    }
                    return expense.getDate().isAfter(date.getValue().atStartOfDay());
                }, date.valueProperty(), period.valueProperty()
        );
        predicate = Bindings.createObjectBinding(() -> payerPredicate.get()
                .and(paidForPredicate.get()
                        .and(tagPredicate.get()
                                .and(datePredicate.get()))),
                payerPredicate, payerPredicate, tagPredicate, datePredicate);
        isEmpty = Bindings.createObjectBinding(
                () -> payer.getValue() != null || paidFor.getValue() != null || tags.getValue() != null
                || period.getValue() != null || date.getValue() != null,
                payer.valueProperty(), paidFor.valueProperty(),
                tags.valueProperty(), period.valueProperty(), date.valueProperty());
    }
    public void resetPayer() {
        payer.setValue(null);
    }
    public void resetPaidFor() {
        paidFor.setValue(null);
    }
    public void resetTags() {
        tags.setValue(null);
    }
    public void resetDate() {
        period.setValue(null);
        date.setValue(null);
    }
    public ObjectBinding<Predicate<Expense>> getPredicate() {
        return predicate;
    }

    public ObjectBinding<Boolean> isEmptyProperty() {
        return isEmpty;
    }
}
