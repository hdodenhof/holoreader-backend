package de.hdodenhof.holoreader.backend.gcm;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import de.hdodenhof.holoreader.backend.exception.GCMException;

public class GCMService {

    private static final String APIKEY = "";

    public void sendMessage(String receipient, String data) throws GCMException {
        try {
            Sender sender = new Sender(APIKEY);

            Message.Builder messageBuilder = new Message.Builder();
            messageBuilder.addData("type", "addfeed");
            messageBuilder.addData("data", data);
            Message message = messageBuilder.build();

            Result result = sender.send(message, receipient, 5);
            if (result.getMessageId() != null) {
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    // same device has more than on registration ID: update database
                }
            } else {
                // see http://developer.android.com/reference/com/google/android/gcm/server/Constants.html
                String error = result.getErrorCodeName();
                if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                    throw new GCMException();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
