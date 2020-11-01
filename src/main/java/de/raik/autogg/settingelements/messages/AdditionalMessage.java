package de.raik.autogg.settingelements.messages;

/**
 * Enum that contains the additional messags
 *
 * @author Raik
 * @version 1.0
 */
public enum AdditionalMessage {
    GOOD_DAY("Have a good day!"),
    HEART("<3"),
    CREATOR("AutoGG by Sk1er (ported by MineFlash07)");


    private final String message;

    AdditionalMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
