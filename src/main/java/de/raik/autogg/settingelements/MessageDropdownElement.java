package de.raik.autogg.settingelements;

import com.google.gson.JsonObject;
import de.raik.autogg.AutoGGAddon;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.DropDownElement;
import net.labymod.utils.Material;

/**
 * DropDownElement with config setting
 * to set the callback changing the config
 * for both configs
 * Also includes description and default date
 *
 * @param <T> The enum for containing the messages
 */
public class MessageDropdownElement<T extends Enum<T>> extends DropDownElement<T> {

    //Default values for every element
    private static final String[] DESCRIPTION_LINES = {"The message to send"};
    private static final String DISPLAY_NAME = "Message";
    private static final IconData ICON = new IconData(Material.PAPER);
    private static final String ATTRIBUTE_NAME = "message";

    /**
     * Constructor for creating the element
     * adding callback
     *
     * @param addon The LabyMod Addon for the config
     * @param configElement The config json Element
     * @param dropDown The dropDown to render in the element
     */
    public MessageDropdownElement(final AutoGGAddon addon, final JsonObject configElement, DropDownMenu<T> dropDown) {
        super(DISPLAY_NAME, dropDown);
        this.iconData = ICON;

        //Setting config save callback
        this.setCallback(value -> {
            configElement.addProperty(ATTRIBUTE_NAME, value.name());
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
        //Calling super that the element will draw
        super.draw(x, y, maxX, maxY, mouseX, mouseY);

        LabyMod.getInstance().getDrawUtils().drawHoveringText(mouseX, mouseY, DESCRIPTION_LINES);
    }
}
