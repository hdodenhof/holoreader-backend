package de.hdodenhof.holoreader.backend;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

public class GCMService {

    private static final String APIKEY = "";

    public void sendMessage(String receipient, String data) {
        try {
            Sender sender = new Sender(APIKEY);

            Message.Builder messageBuilder = new Message.Builder();
            messageBuilder.addData("messageId", data);
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
                    // application has been removed from device - unregister database
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
