package bg.notify.utils;

import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.List;

public class MenuBuilder {

    public static StringSelectMenu getRolesMenu(List<SelectOption> options) {
        return StringSelectMenu.create("role_select")
                .setPlaceholder("Изберете своя програмен език")
                .addOptions(options)
                .build();
    }
}
