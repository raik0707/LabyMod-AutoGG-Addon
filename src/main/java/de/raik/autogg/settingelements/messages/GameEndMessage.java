package de.raik.autogg.settingelements.messages;

/**
 * Enum that contains the gg messags
 *
 * @author Raik
 * @version 1.0
 */
public enum GameEndMessage {
    GG_UPPER("GG"),
    GG_LOWER("gg"),
    GF("gf"),
    GOOD_GAME("Good Game"),
    GOOD_FIGHT("Good Fight"),
    GOOD_ROUND("Good Round! :D");


    private final String message;

    GameEndMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
