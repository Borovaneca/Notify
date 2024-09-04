package bg.notify.schedulers;

import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserRoleReminderScheduler {

    private final JDA jda;

    @Autowired
    public UserRoleReminderScheduler(JDA jda) {
        this.jda = jda;
    }


    @Scheduled(cron = "0 0 13 * * SAT")
    public void getRoleMessageSender() {
        jda.getGuilds().forEach(guild -> {
            Role everyoneRole = guild.getPublicRole();
            List<Member> membersWithoutRoles = guild.getMembers()
                    .stream()
                    .filter(member -> {
                        List<Role> roles = member.getRoles();
                        return roles.isEmpty();
                    })
                    .collect(Collectors.toList());

            if (membersWithoutRoles.isEmpty()) return;

            List<SelectOption> options = new ArrayList<>();
            options.add(SelectOption.of("Java", "java_role"));
            options.add(SelectOption.of("JavaScript", "javascript_role"));
            options.add(SelectOption.of("Python", "python_role"));
            options.add(SelectOption.of("C#", "csharp_role"));

            if (!guild.getName().contains("Fundamentals")) {
                options.add(SelectOption.of("C++", "cpp_role"));
            }

            StringSelectMenu menu = StringSelectMenu.create("role_select")
                    .setPlaceholder("Select your programming language")
                    .addOptions(options)
                    .build();

            for (Member membersWithoutRole : membersWithoutRoles) {
                membersWithoutRole.getUser().openPrivateChannel()
                        .flatMap(channel -> channel.sendMessageEmbeds(EmbeddedMessages.getWelcomeMessage())
                                .addActionRow(menu)).queue();
            }

        });
    }
}
