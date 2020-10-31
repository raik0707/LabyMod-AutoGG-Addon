package de.raik.autogg.settingelements;

import net.labymod.api.LabyModAddon;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.BooleanElement;

/**
 * Boolean element with a description
 * Used for more complex settings
 * that may need a description
 *
 * @author Raik
 * @version 1.0
 */
public class DescribedBooleanElement extends BooleanElement {

    private final String[] descriptionLines;

    /**
     * Constructor for creating such element.
     * Equal to normal boolean element with attribute callback
     * Extra parameter to set description
     *
     * @param displayName The displayName of the element
     * @param addon The reference addon for the config attribute
     * @param iconData The element's icon
     * @param attributeName The name of the config attribute
     * @param defaultValue The default value of the element
     * @param descriptionLines The lines of the setting description
     */
    public DescribedBooleanElement(String displayName, final LabyModAddon addon, IconData iconData
            , final String attributeName, boolean defaultValue, final String... descriptionLines) {
        super(displayName, addon, iconData, attributeName, defaultValue);

        this.descriptionLines = descriptionLines;
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

        LabyMod.getInstance().getDrawUtils().drawHoveringText(mouseX, mouseY, this.descriptionLines);
    }
}
