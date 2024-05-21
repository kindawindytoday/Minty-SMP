package today.kindawindy.smp;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onLoad() {
        SMP.INSTANCE.load(this);
    }

    @Override
    public void onEnable() {
        SMP.INSTANCE.start(this);
    }

    @Override
    public void onDisable() {
        SMP.INSTANCE.stop(this);
    }
}
