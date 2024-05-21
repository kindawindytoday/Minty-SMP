package today.kindawindy.smp.registry;

import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import today.kindawindy.smp.SMP;
import today.kindawindy.smp.manager.DiscordManager;

@Getter
public class DiscordRegistry {

    private JDA jda;
    private Guild guild;

    public DiscordRegistry(SMP instance) {
        init(instance);

        Bukkit.getScheduler().runTaskTimer(instance.getPlugin(), this::updatePresence, 20L, 20L);
    }

    @SneakyThrows
    private void init(SMP instance) {
        FileConfiguration config = instance.getPlugin().getConfig();
        jda = JDABuilder.createDefault(config.getString("discord.token"))
                .addEventListeners(new DiscordManager(instance))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build().awaitReady();
        guild = jda.getGuildById(config.getString("discord.id.guild"));

        guild.loadMembers();

        jda.updateCommands().addCommands(
                Commands.slash("verify", "Verify your account in the game")
                        .addOption(OptionType.STRING, "code",
                                "Verification code", true),
                Commands.slash("reset", "Reset the session in the game")
        ).queue();
    }

    private void updatePresence() {
        jda.getPresence().setPresence(OnlineStatus.ONLINE,
                Activity.playing("Online: " + Bukkit.getOnlinePlayers().size()));
    }
}
