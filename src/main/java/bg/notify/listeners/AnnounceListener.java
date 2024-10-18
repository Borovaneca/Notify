package bg.notify.listeners;

import bg.notify.config.GuildProperties;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AnnounceListener extends ListenerAdapter {

    private final String ANNOUNCE_MESSAGE_ID = "announce-msg";
    private final GuildProperties guildProperties;

    @Autowired
    public AnnounceListener(GuildProperties guildProperties) {
        this.guildProperties = guildProperties;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getButton().getId().equals("announce")) return;

        String placeholder = "Announce message";
        event.replyModal(Modal.create("announce", "Announce")
                .addActionRow(TextInput.create(ANNOUNCE_MESSAGE_ID, "Announce message", TextInputStyle.PARAGRAPH)
                        .setPlaceholder(placeholder).build())
                .build()).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("announce")) return;

        String message = event.getValue(ANNOUNCE_MESSAGE_ID).getAsString();
        TextChannel channel = event.getGuild().getTextChannelById(guildProperties.getAnnouncementChannels().get(event.getGuild().getId()));
        if (channel != null) {
            channel.sendMessage("@everyone\n\n" + message).queue();
            event.reply("Announce message sent!").setEphemeral(true).queue();
        } else {
            event.reply("Could not find the announcement channel!").setEphemeral(true).queue();
        }
    }
}