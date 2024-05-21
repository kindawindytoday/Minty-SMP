package today.kindawindy.smp.util;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

@UtilityClass
public class ChatUtil {

    public String getFormat(Object... args) {
        return getFormat(true, args);
    }

    public String getFormat(boolean minecraft, Object... args) {
        return minecraft ? ChatUtil.colored("%s &f%s&7: &f%s", args) :
                ChatUtil.uncolored("```%s %s: %s```", args);
    }

    public String colored(String text, Object... args) {
        text = String.format(text, args).replaceAll(
                "&#([a-f-A-F\\d])([a-f-A-F\\d])([a-f-A-F\\d])([a-f-A-F\\d])([a-f-A-F\\d])([a-f-A-F\\d])",
                "&x&$1&$2&$3&$4&$5&$6");
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String uncolored(String text, Object... args) {
        return ChatColor.stripColor(colored(String.format(text, args)));
    }
}
