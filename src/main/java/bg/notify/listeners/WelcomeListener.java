package bg.notify.listeners;

import bg.notify.config.GuildProperties;
import bg.notify.config.RolesProperties;
import bg.notify.enums.GuildNames;
import bg.notify.enums.GuildRoleNames;
import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WelcomeListener extends ListenerAdapter {

    private final RolesProperties rolesProperties;
    private final GuildProperties guildProperties;

    @Autowired
    public WelcomeListener(RolesProperties rolesProperties, GuildProperties guildProperties) {
        this.rolesProperties = rolesProperties;
        this.guildProperties = guildProperties;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getMember().getUser();
        if (user.isBot() || user.isSystem()) return;

        List<SelectOption> options = createSelectOptions(event.getGuild().getName(), guildProperties);

        StringSelectMenu menu = StringSelectMenu.create("role_select")
                .setPlaceholder("Select your programming language")
                .addOptions(options)
                .build();

        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(EmbeddedMessages.getWelcomeMessage())
                        .addActionRow(menu)).queue();
    }

    public static List<SelectOption> createSelectOptions(String guildName, GuildProperties guildProperties) {
        List<SelectOption> options = new ArrayList<>();
        options.add(SelectOption.of("Java", "java_role"));
        options.add(SelectOption.of("JavaScript", "javascript_role"));
        options.add(SelectOption.of("Python", "python_role"));
        options.add(SelectOption.of("C#", "csharp_role"));

        if (!guildName.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
            options.add(SelectOption.of("C++", "cpp_role"));
        }

        return options;
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("role_select")) {
            String selectedRole = event.getValues().get(0);
            Guild guild = event.getGuild();
            Member member = guild.getMemberById(event.getUser().getId());

            if (member != null) {
                handleRoleAssignment(event, guild, selectedRole);
            }
        }
    }

    private void handleRoleAssignment(StringSelectInteractionEvent event, Guild guild, String selectedRole) {
        Role role = getRoleForGuild(guild, selectedRole);

        if (role != null) {
            Member member = guild.getMemberById(event.getUser().getId());

            if (member.getRoles().contains(role)) {
                guild.removeRoleFromMember(member, role).queue();
                event.reply(role.getName() + " role has been removed!").setEphemeral(true).queue();
            } else {
                guild.addRoleToMember(member, role).queue();
                event.reply("Congratulations! You have been assigned the " + role.getName() + " role!").setEphemeral(true).queue();
            }
        } else {
            event.reply("Sorry, something went wrong. Please try again.").setEphemeral(true).queue();
        }
    }

    private Role getRoleForGuild(Guild guild, String selectedRole) {
        Map<GuildRoleNames, String> roleMap = null;

        if (guild.getName().contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {
            roleMap = rolesProperties.getBasicsRoles();
        } else if (guild.getName().contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
            roleMap = rolesProperties.getFundamentalsRoles();
        } else if (guild.getName().contains(guildProperties.getGuildNames().get(GuildNames.TEST))) {
            roleMap = rolesProperties.getTestRoles();
        }

        if (roleMap != null) {
            return switch (selectedRole) {
                case "java_role" -> guild.getRoleById(roleMap.get(GuildRoleNames.JAVA));
                case "javascript_role" -> guild.getRoleById(roleMap.get(GuildRoleNames.JAVASCRIPT));
                case "python_role" -> guild.getRoleById(roleMap.get(GuildRoleNames.PYTHON));
                case "csharp_role" -> guild.getRoleById(roleMap.get(GuildRoleNames.CSHARP));
                case "cpp_role" -> guild.getRoleById(roleMap.get(GuildRoleNames.CPLUSPLUS));
                default -> null;
            };
        }

        return null;
    }
}
