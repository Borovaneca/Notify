package bg.mck.sentinel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "jda.bot.valuable-material")
public class ValuableMaterialsProperties {

    private Map<String,String> channels;
}
