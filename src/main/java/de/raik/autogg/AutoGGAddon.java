package de.raik.autogg;

import com.google.gson.JsonObject;
import de.raik.autogg.settingelements.ButtonElement;
import de.raik.autogg.settingelements.DescribedBooleanElement;
import de.raik.autogg.settingelements.MessageDelayElement;
import de.raik.autogg.settingelements.MessageDropdownElement;
import de.raik.autogg.settingelements.messages.AdditionalMessage;
import de.raik.autogg.settingelements.messages.GameEndMessage;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.Settings;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.HeaderElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
     * The message send at the end of the game
     */
    private GameEndMessage gameEndMessage = GameEndMessage.GG_UPPER;

    /**
     * If true a second message will be
     * sent after first message
     */
    private boolean secondMessage = false;

    /**
     * The delay after first message to send
     * second message
     */
    private int secondMessageDelay = 1000;

    /**
     * The second message
     */
    private AdditionalMessage additionalMessage = AdditionalMessage.HEART;

    /**
     * ExecutorService for handeling
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

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

        //Try catch for Enum valuing
        try {
            this.gameEndMessage = addonConfig.has("message") ? GameEndMessage.valueOf(addonConfig.get("message").getAsString()) : this.gameEndMessage;
        } catch (IllegalArgumentException ignored) {}

        this.secondMessage = addonConfig.has("secondmessage") ? addonConfig.get("secondmessage").getAsBoolean() : this.secondMessage;

        //Second message
        if (!addonConfig.has("secondmessagesettings"))
            addonConfig.add("secondmessagesettings", new JsonObject());

        JsonObject secondMessageOptions = addonConfig.getAsJsonObject("secondmessagesettings");
        this.secondMessageDelay = secondMessageOptions.has("messagedelay") ? secondMessageOptions.get("messagedelay").getAsInt() : this.secondMessageDelay;
        //Try catch to check the enum again
        try {
            this.additionalMessage = secondMessageOptions.has("message") ?
                    AdditionalMessage.valueOf(secondMessageOptions.get("message").getAsString()) : this.additionalMessage;
        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Method called by the addon api
     * to add settings to the addon
     *
     * @param settings The list of settings for the addon
     */
    @Override
    protected void fillSettings(List<SettingsElement> settings) {
        //Button element to refresh regex
        settings.add(new ButtonElement("Refresh Cache"
                , new ControlElement.IconData("labymod/textures/settings/settings/serverlistliveview.png")
                , buttonElement -> executorService.execute(() -> {
                    buttonElement.setEnabled(false);
                    this.loadRegex();
                    buttonElement.setEnabled(true);
            }), "Refresh", "Reloads the triggers to send the gg. Clears Cache before."));

        //Begin of settings
        settings.add(new HeaderElement(ModColor.cl('l') + "General"));

        //Adding settings element for enabled which default callback to change the config
        settings.add(new BooleanElement("Enabled", this
                , new ControlElement.IconData(Material.LEVER), "enabled", this.enabled));

        //Adding described setting element because of complex settings
        settings.add(new DescribedBooleanElement("Casual AutoGG", this, new ControlElement.IconData(Material.MAP)
                , "casualGG", this.casualAutoGG, "AutoGG for non Karma events. Such as SkyBlock Events."));

        //Delay Element because of description. Addon config because of delay for first message
        settings.add(new MessageDelayElement(this, this.getConfig(), this.messageDelay));

        //Dropdown element with description and dynamic addon config
        settings.add(new MessageDropdownElement<>(this, this.getConfig(), GameEndMessage.createDropDownMenu(this.gameEndMessage)));

        //Second message
        settings.add(new HeaderElement(ModColor.cl('6') + "Second message"));

        //Described Boolean element with sub setting loading and description
        DescribedBooleanElement secondChatElement = new DescribedBooleanElement("Second message", this,
                new ControlElement.IconData("labymod/textures/settings/category/addons.png"), "secondmessage", this.secondMessage
                , "Sends a second message in the chat after the first message.");
        this.fillSecondMessageSettings(secondChatElement.getSubSettings());
        settings.add(secondChatElement);

        //Anti Category
        settings.add(new HeaderElement(ModColor.cl("c") + ModColor.cl("l") + "Hidden Messages"));
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
        JsonObject config = this.getConfig().getAsJsonObject("secondmessagesettings");
        subSettings.add(new MessageDelayElement(this, config, this.secondMessageDelay));
        subSettings.add(new MessageDropdownElement<>(this, config, AdditionalMessage.createDropDownMenu(this.additionalMessage)));
    }

    /**
     * Download regex from Sk1er for handling gg
     */
    private void loadRegex() {

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
