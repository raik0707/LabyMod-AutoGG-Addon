package de.raik.autogg;

import com.google.gson.JsonObject;
import de.raik.autogg.settingelements.DescribedBooleanElement;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

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
     }

    /**
     * Method called by the addon api
     * to add settings to the addon
     *
     * @param settings The list of settings for the addon
     */
    @Override
    protected void fillSettings(List<SettingsElement> settings) {
        //Adding settings element for enabled which default callback to change the config
        settings.add(new BooleanElement("Enabled", this
                , new ControlElement.IconData(Material.LEVER), "enabled", this.enabled));

        //Adding described setting element because of complex settings
        settings.add(new DescribedBooleanElement("Casual AutoGG", this, new ControlElement.IconData(Material.MAP)
                , "casualGG", this.casualAutoGG, "AutoGG for non Karma events.", "Such as SkyBlock Events."));
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}
