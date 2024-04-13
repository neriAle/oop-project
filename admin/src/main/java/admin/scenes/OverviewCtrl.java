package admin.scenes;

import admin.uicomponents.DownloadButtonCell;
import admin.uicomponents.RemoveButtonCell;
import admin.utils.Config;
import admin.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class OverviewCtrl implements Initializable {
    private ServerUtils serverUtils;
    private Config config;
    @FXML
    private TableView<Event> tableView;
    @Inject
    public OverviewCtrl(ServerUtils serverUtils, Config config) {
        this.serverUtils = serverUtils;
        this.config = config;
    }

    public void fillEvents() {
        List<Event> events = serverUtils.getEvents();
        ObservableList<Event> eventList = FXCollections.observableArrayList(events);
        tableView.setItems(eventList);
    }

    public void download() throws InterruptedException {
        Thread thread = Thread.ofVirtual().start(() -> {
            var json = serverUtils.getExportResult();
            createJsonDumpRepo();
            var file = new File(config.getJsonPath(), "export.json");
            if (file.exists()) file.delete();

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(json);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(new File(config.getJsonPath()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.join();
    }

    private void createJsonDumpRepo() {
        var dir = new File(config.getJsonPath());
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void upload() {
        // Create the UI components for uploading (e.g., FileChooser)
        createJsonDumpRepo();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a JSON file to upload to the database");
        fileChooser.setInitialDirectory(new File(config.getJsonPath()));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                String jsonData = Files.readString(selectedFile.toPath());
                serverUtils.importDatabase(jsonData);
            } catch (IOException e) {
                throw new RuntimeException("Error reading the selected file", e);
            }
        } else {
            // User cancelled the file selection
            System.out.println("No file selected.");
        }
    }


    public void addEvent(Event event) {
        tableView.getItems().add(event);
    }

    public void removeEvent(UUID eventId) {
        tableView.getItems().removeIf(event -> event.getId().equals(eventId));
    }

    public void updateEvent(Event updatedEvent) {
        ObservableList<Event> items = tableView.getItems();
        for (int i = 0; i < items.size(); i++) {
            Event event = items.get(i);
            if (event.getId().equals(updatedEvent.getId())) {
                items.set(i, updatedEvent);
                System.out.println("Event: " + updatedEvent.getName() + " updated");
                return;
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final int secondsInAMinute = 60;
        TableColumn<Event, String> eventNameColumn = new TableColumn<>("Event Name");
        eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Event, String> eventIdColumn = new TableColumn<>("Event ID");
        eventIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Event, LocalDateTime> creationDateColumn = new TableColumn<>("Creation Date");
        creationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        creationDateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        TableColumn<Event, LocalDateTime> lastActionColumn = new TableColumn<>("Last Action");
        lastActionColumn.setCellValueFactory(new PropertyValueFactory<>("lastActivityDate"));
        lastActionColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Duration duration = Duration.between(item, LocalDateTime.now());
                    long hours = duration.toHours();
                    long minutes = duration.toMinutes() % secondsInAMinute;
                    setText(hours + "h " + minutes + "m ago");
                }
            }
        });

        TableColumn<Event, Void> removeColumn = new TableColumn<>("Remove");
        removeColumn.setCellFactory(param -> new RemoveButtonCell(tableView, serverUtils));

        TableColumn<Event, Void> downloadColumn = new TableColumn<>("Download");
        downloadColumn.setCellFactory(param -> new DownloadButtonCell(tableView, serverUtils, config));


        tableView.getColumns().addAll(eventNameColumn, eventIdColumn, creationDateColumn, lastActionColumn, removeColumn, downloadColumn);

    }
}