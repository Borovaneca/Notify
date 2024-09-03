package bg.notify.listeners;

import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.InputStream;

@Component
public class WelcomeListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (event.getMember().getUser().isBot() || event.getMember().getUser().isSystem()) return;

        PrivateChannel privateChannel = event.getMember().getUser().openPrivateChannel().complete();

        try {
            InputStream gifInputStream = getClass().getClassLoader().getResourceAsStream("files/pick-a-role.gif");
            if (gifInputStream == null) {
                throw new FileNotFoundException("Resource not found: files/pick-a-role.gif");
            }

            privateChannel.sendMessageEmbeds(EmbeddedMessages.getWelcomeMessage())
                    .addFiles(FileUpload.fromData(gifInputStream, "pick-a-role.gif"))
                    .queue();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
