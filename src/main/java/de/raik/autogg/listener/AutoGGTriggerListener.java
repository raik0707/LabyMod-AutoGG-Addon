package de.raik.autogg.listener;

import de.raik.autogg.AutoGGAddon;
import net.labymod.api.events.MessageReceiveEvent;

/**
 * Trigger listener to handle the auto gg messages
 *
 * @author Raik
 * @version 1.0
 */
public class AutoGGTriggerListener implements MessageReceiveEvent {

    /**
     * Addon to access the matcher methods
     */
    private final AutoGGAddon addon;

    /**
     * The current state if a gg is invoked
     */
    private boolean invokedGG = false;

    /**
     * Constructor to set variables
     *
     * @param addon The labymod addon
     */
    public AutoGGTriggerListener(AutoGGAddon addon) {
        this.addon = addon;
    }

    /**
     * The method called by the api
     * to handle incoming chat messages
     *
     * @param formattedText The text with color codes
     * @param unformattedText The text without coler codes
     * @return The value if the message will be hidden
     */
    @Override
    public boolean onReceive(String formattedText, String unformattedText) {
        if (this.addon.matchAnti(unformattedText, AutoGGAddon.TriggerType.ANTI_KARMA))
            return true;

        if (this.invokedGG && this.addon.matchAnti(unformattedText, AutoGGAddon.TriggerType.ANTI_GG))
            return true;

        //Cancelling if running to prevent overlapping
        if (invokedGG)
            return false;

        if (this.addon.isCasualAutoGG() && this.addon.matchCasual(unformattedText)) {

        }

        

        return false;
    }
}
