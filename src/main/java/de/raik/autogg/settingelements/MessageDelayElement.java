package de.raik.autogg.settingelements;

import com.google.gson.JsonObject;
import de.raik.autogg.AutoGGAddon;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SliderElement;
import net.labymod.utils.Material;

/**
 * Element for the delay of the messages
 * directly setting displayName,
 * description, maxValue, iconData,
 * Config handling for different JsonObjects
 *
 * @author Raik
 * @version 1.0
 */
public class MessageDelayElement extends SliderElement {

    //Default values for every Delay Element
    private static final String[] DESCRIPTION_LINES = {"The delay after which the message is sent."};
    private static final String DISPLAY_NAME = "Message Delay";
    private static final IconData ICON = new IconData(Material.WATCH);
    private static final int MAX_VALUE = 5000;
    private static final String ATTRIBUTE_NAME = "messagedelay";


    /**
     * Constructor for creating the element
     * adding max value
     *
     * @param addon The LabyMod Addon for the config
     * @param configElement The config json Element
     * @param currentValue The current Value of the slider
     */
    public MessageDelayElement(final AutoGGAddon addon, final JsonObject configElement, int currentValue) {
        //Creating element with super with default values
        super(DISPLAY_NAME, ICON, currentValue);
        this.setMaxValue(MAX_VALUE);

        //Setting config save callback
        this.addCallback(sliderValue -> {
            configElement.addProperty(ATTRIBUTE_NAME, sliderValue);
            addon.saveConfig();
        });
    }

    /**
     * Modifying drawing to implement the drawing of description
     *
     * @param x x position of element
     * @param y y position of element
     * @param maxX maximum x of element
     * @param maxY maximum y of element
     * @param mouseX x position of mouse
     * @param mouseY y position of mouse
     */
    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        //Calling super that the element will still be drawn
        super.draw(x, y, maxX, maxY, mouseX, mouseY);

        LabyMod.getInstance().getDrawUtils().drawHoveringText(mouseX, mouseY, DESCRIPTION_LINES);
    }
}
