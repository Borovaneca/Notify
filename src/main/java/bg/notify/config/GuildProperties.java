package bg.notify.config;

import bg.notify.enums.GuildNames;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "jda.bot.guilds")
public class GuildProperties {

    private Map<GuildNames, String> guildIds;
    private Map<GuildNames, String> guildNames;
    private Map<String, List<String>> textChannelsToLock;
    private Map<String, String> voiceChannelsToLock;
    private Map<String, String> announcementChannels;
    private Map<String, String> logsChannels;
}
