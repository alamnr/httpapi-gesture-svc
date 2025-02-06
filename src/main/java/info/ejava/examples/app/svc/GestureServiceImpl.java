package info.ejava.examples.app.svc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GestureServiceImpl implements GestureService {

    private Map<String,String> gestures = new HashMap<>();

    @Override
    public UpsertResult upsertGesture(String gestureType, String gesture) {
        // data access method
        String previousGesture = gestures.put(gestureType,gesture);
        log.debug("set gesture({}) to {}, returning previous value {}", gestureType, gesture, previousGesture);
        return new UpsertResult(previousGesture==null,previousGesture);
    }

    @Override
    public List<String> getAllGesture(){
        List<String> gestureList = new ArrayList<>(this.gestures.values());
        if(null == gestureList){
            log.debug(" gesture list is empty , size - {}", gestureList.size() );
            throw new ClientErrorException.NotFoundException("gesture liust is empty, size- %s",gestureList.size());
        } else {
            log.debug("gesture list size is - {}", gestureList.size());
            return gestureList;
        }
    }

    @Override
    public String getGesture(String gestureType, String target) {
        // data access method
        String  gesture = gestures.get(gestureType);

        if(null == gesture){
            log.debug("gestureType [{}] not found ", gestureType);
            throw new ClientErrorException.NotFoundException("gesture type [%s] not found", gestureType);
        } else {
            String response = gesture + (target == null ? "": ", "+target);
            log.debug("{} gesture returning {} for {} ", gestureType, response, target);
            return response;
        }


    }

    @Override
    public void deleteGesture(String gestureType) {
        // data access method 
        String gesture = gestures.remove(gestureType);
        log.debug("removed gesture ({}) - was {} ", gestureType, gesture);
    }

    @Override
    public void deleteAllGesture() {
        log.debug("removing all gestures - {}", gestures.size());
        // data access method
        gestures.clear();
    }

}
