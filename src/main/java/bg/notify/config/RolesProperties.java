package bg.notify.config;

import bg.notify.enums.GuildRoleNames;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "jda.bot.roles")
public class RolesProperties {

    private Map<GuildRoleNames, String> basicsRoles;
    private Map<GuildRoleNames, String> fundamentalsRoles;
    private Map<GuildRoleNames, String> testRoles;
}
