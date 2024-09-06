package bg.notify.schedulers;

import bg.notify.config.GuildProperties;
import bg.notify.listeners.WelcomeListener;
import bg.notify.utils.EmbeddedMessages;
import bg.notify.utils.MenuBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserRoleReminderScheduler {

    private final JDA jda;
    private final GuildProperties guildProperties;

    @Autowired
    public UserRoleReminderScheduler(JDA jda, GuildProperties guildProperties) {
        this.jda = jda;
        this.guildProperties = guildProperties;
    }


    @Scheduled(cron = "0 0 13 * * SAT")
    public void getRoleMessageSender() {
        jda.getGuilds().forEach(guild -> {

            List<Member> membersWithoutRoles = guild.getMembers()
                    .stream()
                    .filter(member -> {
                        List<Role> roles = member.getRoles();
                        return roles.isEmpty();
                    })
                    .toList();

            if (membersWithoutRoles.isEmpty()) return;

            List<SelectOption> options = WelcomeListener.createSelectOptions(guild.getName(), guildProperties);

            StringSelectMenu menu = MenuBuilder.getRolesMenu(options);

            membersWithoutRoles.forEach(member ->
                    member.getUser().openPrivateChannel()
                            .flatMap(channel -> channel.sendMessageEmbeds(EmbeddedMessages.getMessageForUsersWithoutRoles())
                                    .addActionRow(menu))
                            .queue()
            );

            guild.getTextChannelById(guildProperties.getLogsChannels().get(guild.getId()))
                    .sendMessageEmbeds(EmbeddedMessages.getInvitationGettingRoleLogMessage(membersWithoutRoles.size()))
                    .queue();

        });
    }
}
