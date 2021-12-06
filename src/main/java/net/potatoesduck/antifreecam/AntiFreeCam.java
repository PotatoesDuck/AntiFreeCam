package net.potatoesduck.antifreecam;

import net.potatoesduck.antifreecam.listener.AntiFreeCamListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * <h1>AntiFreeCam</h1>
 * This is the main class of the AntiFreeCam plugin.
 * This class initializes and calls all needed components
 * of the program.
 *
 * @author Potatoes_Duck
 * @version 1.0
 * @since 2021-12-05
 */
public class AntiFreeCam extends JavaPlugin {

    /**
     * Server startup logic
     */
    @Override
    public void onEnable() {
        getLogger().info("AntiFreeCam by Potatoes_Duck");
        getServer().getPluginManager().registerEvents(new AntiFreeCamListener(), this);
    }
}
