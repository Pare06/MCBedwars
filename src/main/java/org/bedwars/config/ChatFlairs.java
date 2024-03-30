package org.bedwars.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bedwars.stats.ChatFlair;

public class ChatFlairs {
    private ChatFlairs() { }

    public static void initializeFlairs() {
        // ChatFlair() aggiunge automaticamente se stesso a ChatFlair.flairs, quindi basta creare le flair
        new ChatFlair(Component.text("Nessun flair"));
        new ChatFlair(Component.text("[FLAIR TEST]").color(NamedTextColor.DARK_GRAY), Component.text("test 1"));
        new ChatFlair(Component.text("[FLAIR TEST 2]").color(NamedTextColor.LIGHT_PURPLE), Component.text("test 2").color(NamedTextColor.RED));
    }
}
