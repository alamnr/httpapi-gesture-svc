package info.ejava.examples.app.svc;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface GestureService {

    

    @Getter
    @RequiredArgsConstructor
    public static class UpsertResult {
        private final boolean created;
        private final String previousValue;
    }

    public UpsertResult upsertGesture(String gestureType, String gesture);
    public String getGesture(String gestureType, String target);
    public void deleteGesture(String gestureType);
    public void deleteAllGesture();
    public List<String> getAllGesture();
}
