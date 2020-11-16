package de.raik.autogg;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.raik.autogg.listener.AutoGGTriggerListener;
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
import net.labymod.utils.JsonParse;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;
import net.labymod.utils.ServerData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

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
     * If true the second message will be sent
     * if a casual gg triggers
     */
    private boolean sendSecondOnCasual = true;

    /**
     * ExecutorService for handeling
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * Init method called by
     * the addon api to setup the addon
     */
    @Override
    public void onEnable() {
        //Loading regex
        this.executorService.execute(this::loadRegex);

        //Shutdown executor service on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(this.executorService::shutdown));

        //Registering switching events
        this.getApi().getEventManager().registerOnJoin(this::updateServer);
        this.getApi().getEventManager().registerOnQuit(serverData -> this.updateServer(null));

        this.getApi().getEventManager().register(new AutoGGTriggerListener(this));
    }

    /**
     * The gg triggers
     *
     * First pattern for the server
     * HashMap containing triggers
     * Trigger type to differentiate triggers
     * ArrayList Containing pattern
     */
    private final HashMap<Pattern, HashMap<TriggerType, HashSet<Pattern>>> triggers = new HashMap<>();

    /**
     * AntiGG and AntiKarma triggers
     *
     * First pattern for server
     * HashMap containing the pattern
     * depending on trigger type
     */
    private final HashMap<Pattern, HashMap<TriggerType, Pattern>> antiTriggers = new HashMap<>();

    /**
     * Containing the string to add before the message on the different servers.
     */
    private final HashMap<Pattern, String> messageAdditions = new HashMap<>();

    /**
     * The pattern of the current server
     * to handle the pattern matching
     */
    private Pattern currentServerPattern;

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
        this.sendSecondOnCasual = secondMessageOptions.has("sendcasual") ? secondMessageOptions.get("sendcasual").getAsBoolean() : this.sendSecondOnCasual;
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
        subSettings.add(new DescribedBooleanElement("Send on Casual", this, new ControlElement.IconData(Material.MAP)
                , "sendcasual", this.sendSecondOnCasual, "Send the second message also on casual triggers"));
    }

    /**
     * Download regex from Sk1er for handling gg
     */
    private void loadRegex() {
        JsonObject requestResult = this.downloadTriggerJson();

        //Breaking when error in fetching
        if (requestResult == null) {
            this.getApi().displayMessageInChat(ModColor.cl('c') + "Error while fetching AutoGG data.");
            return;
        }

        this.triggers.clear();
        this.antiTriggers.clear();
        this.messageAdditions.clear();

        for (Map.Entry<String, JsonElement> entry: requestResult.entrySet()) {
            //General for every trigger
            Pattern serverPattern = Pattern.compile(entry.getKey().replaceAll("\\\\{2}", "\\\\"));

            HashMap<TriggerType, HashSet<Pattern>> serverTriggers = new HashMap<>();
            JsonObject triggerObject = (JsonObject) entry.getValue();

            //Adding gg triggers
            JsonObject ggTriggerObject = triggerObject.getAsJsonObject("gg_triggers");

            HashSet<Pattern> normalTriggers = new HashSet<>();
            for (JsonElement triggerEntry: ggTriggerObject.getAsJsonArray("triggers")) {
                normalTriggers.add(Pattern.compile(triggerEntry.getAsString().replaceAll("\\\\{2}", "\\\\")));
            }
            serverTriggers.put(TriggerType.NORMAL, normalTriggers);

            HashSet<Pattern> casualTriggers = new HashSet<>();
            for (JsonElement triggerEntry: ggTriggerObject.getAsJsonArray("casual_triggers")) {
                casualTriggers.add(Pattern.compile(triggerEntry.getAsString().replaceAll("\\\\{2}", "\\\\")));
            }
            serverTriggers.put(TriggerType.CASUAL, casualTriggers);

            this.triggers.put(serverPattern , serverTriggers);

            //Adding anti Triggers
            JsonObject antiObject = triggerObject.getAsJsonObject("other_patterns");

            HashMap<TriggerType, Pattern> antiTriggers = new HashMap<>();
            antiTriggers.put(TriggerType.ANTI_GG, Pattern.compile(reformatAntiString(antiObject.get("antigg")
                    .getAsString()).replaceAll("(?<!\\\\)\\$\\{antigg_strings}", String.join("|", this.getAntiTriggers()))));
            antiTriggers.put(TriggerType.ANTI_KARMA, Pattern.compile(reformatAntiString(antiObject.get("anti_karma").getAsString())));

            this.antiTriggers.put(serverPattern, antiTriggers);

            //Adding additional message
            this.messageAdditions.put(serverPattern, triggerObject.getAsJsonObject("other").get("msg").getAsString());
        }
    }

    /**
     * Reformatting the strings of the anti pattern
     *
     * @param stringToReformat The string to reformat
     * @return The formatted string
     */
    private String reformatAntiString(String stringToReformat) {
        return stringToReformat.substring(1, stringToReformat.length() - 1).replaceAll("\\\\{2}", "\\\\");
    }

    /**
     * Merge both message types to get messages
     * that should be hidden
     *
     * @return The merged array
     */
    private String[] getAntiTriggers() {
        HashSet<String> messages = new HashSet<>();

        for (GameEndMessage gameEndTrigger: GameEndMessage.values()) {
            messages.add(gameEndTrigger.getMessage());
        }
        for (AdditionalMessage additionalTrigger: AdditionalMessage.values()) {
            messages.add(additionalTrigger.getMessage());
        }

        String[] result = new String[messages.size()];
        messages.toArray(result);

        return result;
    }

    /**
     * Downloading the json form Sk1ers Website
     *
     * @return The json object got from the request
     */
    private JsonObject downloadTriggerJson() {
        JsonObject requestResult;

        try {
            //Http Request
            HttpURLConnection connection = (HttpURLConnection) new URL("https://static.sk1er.club/autogg/regex_triggers_new.json").openConnection();
            connection.addRequestProperty("User-Agent", "java 8 HttpURLConnection (LabyMod AutoGG Addon by MineFlash07)");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");

            StringBuilder resultBuilder = new StringBuilder();

            //Transform to string
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    resultBuilder.append(line);
                }
            }

            requestResult = (JsonObject) JsonParse.parse(resultBuilder.toString());
        } catch (IOException | ClassCastException exception) {
            return null;
        }

        return requestResult.getAsJsonObject("servers");
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isCasualAutoGG() {
        return this.casualAutoGG;
    }

    public boolean isSecondMessage() {
        return this.secondMessage;
    }

    public int getMessageDelay() {
        return this.messageDelay;
    }

    public int getSecondMessageDelay() {
        return this.secondMessageDelay;
    }

    public GameEndMessage getGameEndMessage() {
        return this.gameEndMessage;
    }

    public AdditionalMessage getAdditionalMessage() {
        return this.additionalMessage;
    }

    public boolean isSendSecondOnCasual() {
        return this.sendSecondOnCasual;
    }

    /**
     * Updating pattern variable
     * to set serverdata
     *
     * @param serverData The server data to check
     */
    public void updateServer(ServerData serverData) {
        //Resetting on leave
        if (serverData == null) {
            this.currentServerPattern = null;
            return;
        }
        String serverIP = serverData.getIp().replaceAll("^(.*):\\d{1,5}$", "$1").toLowerCase(Locale.ENGLISH);

        //Setting pattern if it matches
        for (Pattern keyPattern: this.triggers.keySet()) {
            if (keyPattern.matcher(serverIP).matches()) {
                this.currentServerPattern = keyPattern;
                return;
            }
        }

        //Resetting if nothing matches
        this.currentServerPattern = null;
    }

    /**
     * Get if the message
     * is a anti message
     *
     * @param message The message to check
     * @param antiType The type of the anti message
     * @return The result
     */
    public boolean matchAnti(String message, TriggerType antiType) {
        boolean matchPattern = this.antiTriggers.get(this.currentServerPattern).get(antiType).matcher(message).matches();

        if (antiType == TriggerType.ANTI_KARMA)
            return this.antiKarma && matchPattern;

        if (!antiGG)
            return false;

        //Normally returns false when others send those messages
        //Reason for this construct
        if (matchPattern)
            return true;

        String[] messageSplits = message.split(" ");

        for (String antiTrigger: this.getAntiTriggers()) {
            for (String messageSplit: messageSplits) {
                if (messageSplit.equalsIgnoreCase(antiTrigger))
                    return true;
            }
        }

        return false;
    }

    /**
     * Check if a messages validation is possible
     *
     * @return The result
     */
    public boolean canNotMatch() {
        return this.currentServerPattern == null;
    }

    /**
     * Check if message matches casual triggers
     *
     * @param message The message to check
     * @return The result
     */
    public boolean matchCasual(String message) {
        for (Pattern casualPattern: this.triggers.get(this.currentServerPattern).get(TriggerType.CASUAL)) {
            if (casualPattern.matcher(message).matches())
                return true;
        }
        return false;
    }

    /**
     * Check if a message matches a gg trigger
     *
     * @param message The message to check
     * @return The result
     */
    public boolean match(String message) {
        for (Pattern pattern: this.triggers.get(this.currentServerPattern).get(TriggerType.NORMAL)) {
            if (pattern.matcher(message).matches())
                return true;
        }
        return false;
    }

    /**
     * Returns the additional message
     * for the gg message
     *
     * @return The message
     */
    public String getMessageAddition() {
        return this.messageAdditions.get(this.currentServerPattern);
    }

    /**
     * Enum to differentiate patterns
     * Enum contains Types
     */
    public enum TriggerType {
        NORMAL,
        CASUAL,
        ANTI_GG,
        ANTI_KARMA
    }
}
