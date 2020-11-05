package de.raik.autogg.settingelements.messages;

import net.labymod.gui.elements.DropDownMenu;
import net.labymod.main.LabyMod;

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

    public static DropDownMenu<GameEndMessage> createDropDownMenu(GameEndMessage currentValue) {
        DropDownMenu<GameEndMessage> dropDownMenu = new DropDownMenu<GameEndMessage>(null, 0, 0, 0, 0)
                .fill(values());
        dropDownMenu.setSelected(currentValue);
        dropDownMenu.setEntryDrawer((entry, x, y, trimmed) ->
            LabyMod.getInstance().getDrawUtils().drawString(((GameEndMessage)entry).getMessage(), x ,y));

        return dropDownMenu;
    }
}
