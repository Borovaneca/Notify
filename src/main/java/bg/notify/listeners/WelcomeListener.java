package bg.notify.listeners;

import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class WelcomeListener extends ListenerAdapter {



    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (event.getMember().getUser().isBot() || event.getMember().getUser().isSystem()) return;

        PrivateChannel privateChannel = event.getMember().getUser().openPrivateChannel().complete();
        Path gifPath = Paths.get("src/main/resources/files/pick-a-role.gif");
        File gifFile = gifPath.toFile();

        privateChannel.sendMessageEmbeds(EmbeddedMessages.getWelcomeMessage())
                .addFiles(FileUpload.fromData(gifFile, "pick-a-role.gif"))
                .queue();
    }
}
