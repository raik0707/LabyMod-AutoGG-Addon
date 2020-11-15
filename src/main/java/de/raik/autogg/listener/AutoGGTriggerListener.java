package de.raik.autogg.listener;

import de.raik.autogg.AutoGGAddon;
import net.labymod.api.events.MessageReceiveEvent;
import net.minecraft.client.Minecraft;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
     * Executor service for executing the message async
     * scheduled because of message delay
     */
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    /**
     * Minecraft instance to access the player entity
     */
    private final Minecraft minecraft = Minecraft.getMinecraft();

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
            this.sendGGMessage(true);
            return false;
        }

        if (this.addon.isEnabled() && this.addon.match(unformattedText))
            this.sendGGMessage(false);

        return false;
    }

    /**
     * Send the gg message
     *
     * @param casual If the message is triggered by a casual trigger
     */
    private void sendGGMessage(boolean casual) {
        this.invokedGG = true;
        this.executorService.schedule(() -> {
            String messageAddition = this.addon.getMessageAddition();

            this.minecraft.thePlayer.sendChatMessage(messageAddition + this.addon.getGameEndMessage().getMessage());

            //Send second message
            if (this.addon.isSecondMessage() && (!casual || this.addon.isSendSecondOnCasual()))
                this.executorService.schedule(() -> this.minecraft.thePlayer.sendChatMessage(messageAddition + this.addon.getAdditionalMessage().getMessage())
                        , this.addon.getSecondMessageDelay(), TimeUnit.MILLISECONDS);

            //Waiting for ending
            try {
                Thread.sleep(2000);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
            this.invokedGG = false;
        }, this.addon.getMessageDelay(), TimeUnit.MILLISECONDS);
    }
}
