package de.raik.autogg.settingelements;

import com.google.gson.JsonObject;
import de.raik.autogg.AutoGGAddon;
import net.labymod.gui.elements.DropDownMenu;
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
    private static final String DESCRIPTION = "The message to send";
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

        this.setDescriptionText(DESCRIPTION);

        //Setting config save callback
        this.setChangeListener(value -> {
            configElement.addProperty(ATTRIBUTE_NAME, value.name());
            addon.saveConfig();
            addon.loadConfig();
        });
    }
}
