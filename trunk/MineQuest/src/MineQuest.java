import java.util.logging.Logger;

public class MineQuest extends Plugin {
    static final MineQuestListener listener = new MineQuestListener();
    private Logger log;
    private String name = "MineQuest";
    private String version = "0.20";

    public void enable() {
    }
    
    public void disable() {
    }

    public void initialize() {
        log = Logger.getLogger("Minecraft");
        listener.setup();
        log.info(name + " " + version + " initialized");
        etc.getLoader().addListener(
            PluginLoader.Hook.DAMAGE,
            listener,
            this,
            PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(
                PluginLoader.Hook.LOGIN,
                listener,
                this,
                PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(
                PluginLoader.Hook.COMMAND,
                listener,
                this,
                PluginListener.Priority.MEDIUM);
    }
}