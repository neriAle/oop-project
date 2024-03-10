package client.utils;

import commons.Event;
import commons.Participant;

import java.io.*;
import java.util.*;

public class ConfigUtils {
    private Reader recentsFile;
    private Reader participantsFile;

    public ArrayList<Event> readRecents() {
        try (BufferedReader reader = new BufferedReader(recentsFile)) {
            ArrayList<Event> events = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                Event newEvent = new Event();
                newEvent.setName(parts[0]);
                newEvent.setId(UUID.fromString(parts[1]));
                events.add(newEvent);
            }
            return events;
        }
        catch (IOException e) {
            //TODO Log error and handle
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Participant> readParticipants() {
        try (BufferedReader reader = new BufferedReader(participantsFile)) {
            ArrayList<Participant> participants = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                Participant newParticipant = new Participant(parts[0], parts[1], parts[2]);
                participants.add(newParticipant);
            }
            return participants;
        }
        catch (IOException e) {
            //TODO error handling
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> readLanguage(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Map<String, String> labelList = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] mapElements = line.split("@");
                labelList.put(mapElements[0], mapElements[1]);
            }
            return labelList;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public void setRecentsFile(Reader recentsFile) {
        this.recentsFile = recentsFile;
    }

    public void setParticipantsFile(Reader participantsFile) {
        this.participantsFile = participantsFile;
    }
}
