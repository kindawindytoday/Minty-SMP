package today.kindawindy.smp.manager;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import today.kindawindy.smp.SMP;
import today.kindawindy.smp.objects.GamePlayer;
import today.kindawindy.smp.registry.PlayerRegistry;
import today.kindawindy.smp.util.ChatUtil;

public class DiscordManager extends ListenerAdapter {

    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final PlayerRegistry playerRegistry;

    public DiscordManager(SMP instance) {
        this.plugin = instance.getPlugin();
        this.config = plugin.getConfig();
        this.playerRegistry = instance.getPlayerRegistry();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        if (event.getAuthor().isBot() || message.isEmpty() || message.isBlank() ||
                event.getChannelType() != ChannelType.TEXT ||
                event.getChannel().asTextChannel().getIdLong() != config.getLong("discord.id.chat"))
            return;

        GamePlayer gamePlayer = playerRegistry.getByDiscordId(event.getMember().getIdLong());

        if (gamePlayer == null)
            return;

        Object[] args = {gamePlayer.getPrefix(), gamePlayer.getName(), message};

        Bukkit.broadcastMessage(ChatUtil.getFormat(args));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        long memberId = member.getIdLong();

        switch (event.getName().toLowerCase()) {
            case "verify" -> {
                GamePlayer gamePlayer = playerRegistry.getByDiscordId(memberId);

                if (gamePlayer != null) {
                    event.reply("ERROR: You have already verified your account!")
                            .setEphemeral(true).queue();
                    return;
                }

                String code = event.getOption("code", OptionMapping::getAsString);
                gamePlayer = playerRegistry.getByCode(code);

                if (gamePlayer == null || gamePlayer.getDiscordId() != 0) {
                    event.reply("ERROR: Couldn't find a player with this code!")
                            .setEphemeral(true).queue();
                    return;
                }

                event.reply("Verification was successful!")
                        .setEphemeral(true).queue();
                guild.addRoleToMember(member, guild.getRoleById(config.getLong("discord.id.role")))
                        .queue();
                gamePlayer.setDiscordId(memberId);
            }
            case "reset" -> {
                GamePlayer gamePlayer = playerRegistry.getByDiscordId(memberId);

                if (gamePlayer == null) {
                    event.reply("ERROR: You haven't registered or logged in to the server yet!")
                            .setEphemeral(true).queue();
                    return;
                }

                event.reply("Session successfully reset!")
                        .setEphemeral(true).queue();
                gamePlayer.setIp("");
            }
        }
    }
}
