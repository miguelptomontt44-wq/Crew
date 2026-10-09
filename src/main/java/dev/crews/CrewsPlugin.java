package dev.crews;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

public final class CrewsPlugin extends JavaPlugin implements Listener {
    private static final UUID PACK_ID = UUID.fromString("5f0a9c34-8d1e-4c7b-9a62-3b1d4e7f2c10");

    private CrewManager crews;
    private NameTagManager nameTags;
    private MenuManager menus;
    private InputManager input;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        crews = new CrewManager(this);
        crews.load();
        nameTags = new NameTagManager(this);
        menus = new MenuManager(this);
        input = new InputManager(this);

        var pm = getServer().getPluginManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new MenuListener(), this);
        pm.registerEvents(input, this);
        pm.registerEvents(nameTags, this);

        CrewCommand cmd = new CrewCommand(this);
        Objects.requireNonNull(getCommand("crew")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("crew")).setTabCompleter(cmd);

        nameTags.start();
    }

    @Override
    public void onDisable() {
        if (nameTags != null) nameTags.removeAll();
        if (crews != null) crews.save();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        String trigger = getConfig().getString("open-trigger", "SNEAK_SWAP").toUpperCase();
        Player p = e.getPlayer();
        if (trigger.equals("SWAP") || (trigger.equals("SNEAK_SWAP") && p.isSneaking())) {
            e.setCancelled(true);
            menus.open(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        String url = getConfig().getString("resource-pack.url", "");
        if (url == null || url.isBlank()) return;
        String sha1 = getConfig().getString("resource-pack.sha1", "");
        byte[] hash = new byte[0];
        try {
            if (sha1 != null && !sha1.isBlank()) hash = HexFormat.of().parseHex(sha1.trim());
        } catch (IllegalArgumentException ex) {
            getLogger().warning("resource-pack.sha1 invalido.");
        }
        e.getPlayer().setResourcePack(PACK_ID, url, hash,
                Msg.MM.deserialize("<gold>Textura del menu de Crews"),
                getConfig().getBoolean("resource-pack.force", false));
    }

    public CrewManager crews() { return crews; }
    public NameTagManager nameTags() { return nameTags; }
    public MenuManager menus() { return menus; }
    public InputManager input() { return input; }
}
