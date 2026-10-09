package dev.crews;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public final class Msg {
    public static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String PREFIX = "<gold>[Crew]</gold> ";

    private Msg() {}

    public static Component info(String mm, TagResolver... r) {
        return MM.deserialize(PREFIX + "<gray>" + mm, r);
    }

    public static Component ok(String mm, TagResolver... r) {
        return MM.deserialize(PREFIX + "<green>" + mm, r);
    }

    public static Component err(String mm, TagResolver... r) {
        return MM.deserialize(PREFIX + "<red>" + mm, r);
    }

    /** Texto sin cursiva (para items de menu). */
    public static Component item(String mm, TagResolver... r) {
        return MM.deserialize("<!italic>" + mm, r);
    }
}
