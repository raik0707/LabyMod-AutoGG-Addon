package de.raik.autogg;

import com.google.gson.JsonObject;
import de.raik.autogg.settingelements.DescribedBooleanElement;
import de.raik.autogg.settingelements.MessageDelayElement;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.Settings;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.HeaderElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;

import java.util.List;

/**
 * Addon class
 * to handle the addon and
 * its instance
 *
 * @author Raik
 * @version 1.0
 */
public class AutoGGAddon extends LabyModAddon {

    private boolean enabled = true;

    /**
     * If true, it will also send a auto gg
     * for none Karma things
     */
    private boolean casualAutoGG = false;

    /**
     * If true, gg messages will be hidden
     */
    private boolean antiGG = false;

    /**
     * If true, karma messages will be hidden
     */
    private boolean antiKarma = false;

    /**
     * The delay after gg triggers
     * to say the message
     */
    private int messageDelay = 1000;

    /**
     * If true a second message will be
     * sent after first message
     */
    private boolean secondMessage = false;

    /**
     * Init method called by
     * the addon api to setup the addon
     */
    @Override
    public void onEnable() {

    }

    /**
     * Method called by the addon api
     * to load the settings
     */
    @Override
    public void loadConfig() {
        JsonObject addonConfig = this.getConfig();

        this.enabled = addonConfig.has("enabled") ? addonConfig.get("enabled").getAsBoolean() : this.enabled;
        this.casualAutoGG = addonConfig.has("casualGG") ? addonConfig.get("casualGG").getAsBoolean() : this.casualAutoGG;
        this.antiGG = addonConfig.has("antigg") ? addonConfig.get("antigg").getAsBoolean() : this.antiGG;
        this.antiKarma = addonConfig.has("antikarma") ? addonConfig.get("antikarma").getAsBoolean() : this.antiKarma;
        this.messageDelay = addonConfig.has("messagedelay") ? addonConfig.get("messagedelay").getAsInt() : this.messageDelay;
        this.secondMessage = addonConfig.has("secondmessage") ? addonConfig.get("secondmessage").getAsBoolean() : this.secondMessage;
    }

    /**
     * Method called by the addon api
     * to add settings to the addon
     *
     * @param settings The list of settings for the addon
     */
    @Override
    protected void fillSettings(List<SettingsElement> settings) {
        settings.add(new HeaderElement(ModColor.cl('l') + "General"));

        //Adding settings element for enabled which default callback to change the config
        settings.add(new BooleanElement("Enabled", this
                , new ControlElement.IconData(Material.LEVER), "enabled", this.enabled));

        //Adding described setting element because of complex settings
        settings.add(new DescribedBooleanElement("Casual AutoGG", this, new ControlElement.IconData(Material.MAP)
                , "casualGG", this.casualAutoGG, "AutoGG for non Karma events.", "Such as SkyBlock Events."));

        //Second message
        settings.add(new HeaderElement(ModColor.cl('6') + "Second message"));

        //Described Boolean element with sub setting loading and description
        DescribedBooleanElement secondChatElement = new DescribedBooleanElement("Second message", this,
                new ControlElement.IconData("labymod/textures/settings/category/addons.png"), "secondmessage", this.secondMessage
                , "Sends a second message in the chat", "after the first message.");
        this.fillSecondMessageSettings(secondChatElement.getSubSettings());
        settings.add(secondChatElement);

        //Delay Element because of description. Addon config because of delay for first message
        settings.add(new MessageDelayElement(this, this.getConfig(), this.messageDelay));

        //Anti Category
        settings.add(new HeaderElement(ModColor.cl("cl") + "Hidden Message"));
        //Elements with config setting
        settings.add(new BooleanElement("Hide GG messages", this, new ControlElement.IconData(Material.BARRIER)
                , "antigg", this.antiGG));
        settings.add(new BooleanElement("Hide Karma messages", this, new ControlElement.IconData(Material.EXP_BOTTLE)
                , "antikarma", this.antiKarma));
    }

    /**
     * Fills the subsettings of the second chat message
     *
     * @param subSettings The settings to fill
     */
    private void fillSecondMessageSettings(Settings subSettings) {

    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isCasualAutoGG() {
        return this.casualAutoGG;
    }

    public boolean isAntiGG() {
        return this.antiGG;
    }

    public boolean isAntiKarma() {
        return this.antiKarma;
    }

    public boolean isSecondMessage() {
        return this.secondMessage;
    }
}
