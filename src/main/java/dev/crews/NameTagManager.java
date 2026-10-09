package dev.crews;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Muestra el nombre de la crew bajo el nametag del jugador usando un TextDisplay
 * montado como pasajero. El propio jugador no lo ve (hideEntity).
 */
public final class NameTagManager implements Listener {
    private final CrewsPlugin plugin;
    private final Map<UUID, TextDisplay> displays = new HashMap<>();

    public NameTagManager(CrewsPlugin plugin) { this.plugin = plugin; }

    public void start() {
        // Revalida cada segundo: cubre teleports, respawn, cambios de mundo, etc.
        Bukkit.getScheduler().runTaskTimer(plugin, this::refreshAll, 20L, 20L);
    }

    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) refresh(p);
    }

    public void refresh(Player p) {
        if (!plugin.getConfig().getBoolean("nametag.enabled", true)) { remove(p); return; }
        Crew crew = plugin.crews().of(p.getUniqueId());
        boolean hidden = crew == null || !p.isOnline() || p.isDead()
                || p.getGameMode() == GameMode.SPECTATOR
                || p.hasPotionEffect(PotionEffectType.INVISIBILITY);
        if (hidden) { remove(p); return; }

        TextDisplay td = displays.get(p.getUniqueId());
        if (td == null || !td.isValid() || !td.getWorld().equals(p.getWorld()) || !p.getPassengers().contains(td)) {
            remove(p);
            td = spawn(p);
        }
        td.text(format(crew));
    }

    private Component format(Crew crew) {
        String f = plugin.getConfig().getString("nametag.format", "<gray>[<gold><crew></gold>]");
        return Msg.MM.deserialize(f, Placeholder.unparsed("crew", crew.name()));
    }

    private TextDisplay spawn(Player p) {
        float scale = (float) plugin.getConfig().getDouble("nametag.scale", 0.8);
        float y = (float) plugin.getConfig().getDouble("nametag.y-offset", 0.1);
        TextDisplay td = p.getWorld().spawn(p.getLocation(), TextDisplay.class, d -> {
            d.setPersistent(false);
            d.setBillboard(Display.Billboard.CENTER);
            d.setShadowed(true);
            d.setSeeThrough(false);
            d.setDefaultBackground(false);
            d.setBackgroundColor(Color.fromARGB(64, 0, 0, 0));
            d.setViewRange(0.5f);
            d.setTransformation(new Transformation(
                    new Vector3f(0f, y, 0f), new AxisAngle4f(), new Vector3f(scale, scale, scale), new AxisAngle4f()));
        });
        p.addPassenger(td);
        p.hideEntity(plugin, td);
        displays.put(p.getUniqueId(), td);
        return td;
    }

    public void remove(Player p) {
        TextDisplay td = displays.remove(p.getUniqueId());
        if (td != null) td.remove();
    }

    public void removeAll() {
        displays.values().forEach(TextDisplay::remove);
        displays.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> refresh(e.getPlayer()), 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { remove(e.getPlayer()); }
}
