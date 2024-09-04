package bg.notify.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "jda.bot.manager")
public class ManagerProperties {

    private Map<String,String> managerChannels;

}
