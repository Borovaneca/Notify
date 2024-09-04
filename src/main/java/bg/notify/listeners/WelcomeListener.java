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

        List<SelectOption> options = new ArrayList<>();
        options.add(SelectOption.of("Java", "java_role"));
        options.add(SelectOption.of("JavaScript", "javascript_role"));
        options.add(SelectOption.of("Python", "python_role"));
        options.add(SelectOption.of("C#", "csharp_role"));

        if (!event.getGuild().getName().contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
            options.add(SelectOption.of("C++", "cpp_role"));
        }

        StringSelectMenu menu = StringSelectMenu.create("role_select")
                .setPlaceholder("Select your programming language")
                .addOptions(options)
                .build();

        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(EmbeddedMessages.getWelcomeMessage())
                        .addActionRow(menu)).queue();

    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("role_select")) {
            String selectedRole = event.getValues().get(0);
            for (Guild guild : event.getJDA().getGuilds()) {
                User user = event.getUser();
                Member member = guild.getMemberById(user.getId());
                if (member != null) {
                    setRoleOnUser(event, guild.getName(), selectedRole);
                }
            }
        }
    }

    private void setRoleOnUser(StringSelectInteractionEvent event, String guildName, String selectedRole) {
        Role role = null;
        String guildRoleId;
        Guild guild;
        if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {
            guild = event.getJDA().getGuildById(guildProperties.getGuildIds().get(GuildNames.BASICS));
            role = switch (selectedRole) {
                case "java_role" ->
                        guild.getRoleById(rolesProperties.getBasicsRoles().get(GuildRoleNames.JAVA));
                case "javascript_role" ->
                        guild.getRoleById(rolesProperties.getBasicsRoles().get(GuildRoleNames.JAVASCRIPT));
                case "python_role" ->
                        guild.getRoleById(rolesProperties.getBasicsRoles().get(GuildRoleNames.PYTHON));
                case "csharp_role" ->
                        guild.getRoleById(rolesProperties.getBasicsRoles().get(GuildRoleNames.CSHARP));
                case "cpp_role" ->
                        guild.getRoleById(rolesProperties.getBasicsRoles().get(GuildRoleNames.CPLUSPLUS));
                default -> role;
            };

        } else if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
            guild = event.getJDA().getGuildById(guildProperties.getGuildIds().get(GuildNames.FUNDAMENTALS));
            role = switch (selectedRole) {
                case "java_role" ->
                        guild.getRoleById(rolesProperties.getFundamentalsRoles().get(GuildRoleNames.JAVA));
                case "javascript_role" ->
                        guild.getRoleById(rolesProperties.getFundamentalsRoles().get(GuildRoleNames.JAVASCRIPT));
                case "python_role" ->
                        guild.getRoleById(rolesProperties.getFundamentalsRoles().get(GuildRoleNames.PYTHON));
                case "csharp_role" ->
                        guild.getRoleById(rolesProperties.getFundamentalsRoles().get(GuildRoleNames.CSHARP));
                default -> role;
            };
        } else {
            guild = event.getJDA().getGuildById(guildProperties.getGuildIds().get(GuildNames.TEST));
            role = switch (selectedRole) {
                case "java_role" ->
                        guild.getRoleById(rolesProperties.getTestRoles().get(GuildRoleNames.JAVA));
                case "javascript_role" ->
                        guild.getRoleById(rolesProperties.getTestRoles().get(GuildRoleNames.JAVASCRIPT));
                case "python_role" ->
                        guild.getRoleById(rolesProperties.getTestRoles().get(GuildRoleNames.PYTHON));
                case "csharp_role" ->
                        guild.getRoleById(rolesProperties.getTestRoles().get(GuildRoleNames.CSHARP));
                default -> role;
            };
        }

        if (role != null) {
            if (guild.getMemberById(event.getUser().getId()).getRoles().contains(role)) {
                guild.removeRoleFromMember(event.getUser(), role).queue();
                event.reply(role.getName() + " role has been removed!").setEphemeral(true).queue();
            } else {
                guild.addRoleToMember(event.getUser(), role).queue();
                event.reply("Congratulation! You have been obtained " + role.getName() + " role!").setEphemeral(true).queue();
            }
        } else {
            event.reply("Sorry, something went wrong. Please try again.").setEphemeral(true).queue();
        }
    }
}
