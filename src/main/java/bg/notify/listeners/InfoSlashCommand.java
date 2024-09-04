package bg.notify.listeners;

import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class InfoSlashCommand extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("info")) return;

        event.replyEmbeds(EmbeddedMessages.getBotInfoMessage()).queue();
    }
}
