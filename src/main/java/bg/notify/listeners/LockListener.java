package bg.notify.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class LockListener extends ListenerAdapter {


    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getButton().getId().equals("lock")) return;

        Guild guild = event.getGuild();
        TextChannel textChannel = guild.getTextChannelById("1187900803588034620");
        textChannel.getManager().putPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.MESSAGE_SEND)).queue();
        textChannel.getManager().putPermissionOverride(guild.getPublicRole(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND)).queue();

        event.reply("Channel is locked!").queue();
    }
}
