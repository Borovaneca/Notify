package bg.notify.services;

import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static bg.notify.constants.Constants.LOCK_CHANNELS_ENDPOINT;
import static bg.notify.constants.Constants.UNLOCK_CHANNELS_ENDPOINT;

@Component
public class EventService {

    public void createCloseChannelsEvent(String guildId, TextChannel logChannel) throws IOException {
        proceed(guildId, logChannel, true);
    }

    public void createOpenChannelsEvent(String guildId, TextChannel logChannel) throws IOException {
        proceed(guildId, logChannel, false);
    }

    private void proceed(String guildId, TextChannel logChannel, boolean closingEvent) throws IOException {
        String endPoint = closingEvent ? String.format(LOCK_CHANNELS_ENDPOINT, guildId) : String.format(UNLOCK_CHANNELS_ENDPOINT, guildId);
        Request request = new Request.Builder()
                .url(endPoint)
                .post(RequestBody.create(new byte[0]))
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.MINUTES)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                logChannel.sendMessageEmbeds(EmbeddedMessages.getEventExecutedSuccessfully()).queue();
            } else {
                logChannel.sendMessageEmbeds(EmbeddedMessages.getEventExecutedUnSuccessfully()).queue();
            }
        }
    }
}
