package dev.crews;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MenuManager {
    // Glifos del resource pack (assets/crews/font/menu.json)
    private static final Key FONT = Key.key("crews", "menu");
    private static final Key DEFAULT_FONT = Key.key("minecraft", "default");
    private static final String SHIFT_BACK_8 = "\uF808";
    private static final String BACKGROUND = "\uE000";
    private static final String SHIFT_TO_TITLE = "\uF801";

    private static final int[] MEMBER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34};

    private final CrewsPlugin plugin;

    public MenuManager(CrewsPlugin plugin) { this.plugin = plugin; }

    private Component title(String text) {
        return Component.text(SHIFT_BACK_8 + BACKGROUND, NamedTextColor.WHITE).font(FONT)
                .append(Component.text(SHIFT_TO_TITLE).font(FONT))
                .append(Component.text(text, TextColor.color(0x404040)).font(DEFAULT_FONT));
    }

    private ItemStack icon(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Msg.item(name));
        if (lore.length > 0) {
            List<Component> l = new ArrayList<>();
            for (String s : lore) l.add(Msg.item(s));
            meta.lore(l);
        }
        it.setItemMeta(meta);
        return it;
    }

    private void click(Player p) { p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1f); }

    private void result(Player p, String error, String okMsg) {
        if (error != null) p.sendMessage(Msg.err(error));
        else if (okMsg != null) p.sendMessage(Msg.ok(okMsg));
    }

    public void open(Player p) {
        Crew crew = plugin.crews().of(p.getUniqueId());
        if (crew == null) openNoCrew(p); else openCrew(p, crew);
    }

    // ---------------- sin crew ----------------
    private void openNoCrew(Player p) {
        CrewMenu m = new CrewMenu(title("Crews"));
        m.set(4, icon(Material.WHITE_BANNER, "<gold><bold>Sin crew",
                "<gray>Crea una o acepta una invitacion."), null);

        m.set(49, icon(Material.EMERALD, "<green>Crear crew",
                "<gray>Click para escribir el nombre", "<gray>en el chat."), c -> {
            click(c.player());
            c.player().closeInventory();
            plugin.input().ask(c.player(),
                    Msg.info("Escribe el <gold>nombre de tu crew</gold> en el chat (o <red>cancelar</red>):"),
                    text -> {
                        result(c.player(), plugin.crews().create(c.player(), text), "Crew creada.");
                        open(c.player());
                    });
        });

        List<CrewManager.Invite> invs = plugin.crews().invitesFor(p.getUniqueId());
        for (int i = 0; i < Math.min(invs.size(), MEMBER_SLOTS.length); i++) {
            CrewManager.Invite inv = invs.get(i);
            Crew c = plugin.crews().crewOf(inv);
            if (c == null) continue;
            m.set(MEMBER_SLOTS[i], icon(Material.PAPER,
                    "<yellow>Invitacion: <gold>" + c.name(),
                    "<gray>De: <white>" + plugin.crews().nameOf(inv.inviter()),
                    "<gray>Miembros: <white>" + c.members().size(),
                    "",
                    "<green>Click izquierdo: aceptar",
                    "<red>Click derecho: rechazar"), cl -> {
                click(cl.player());
                if (cl.type().isRightClick()) {
                    plugin.crews().deny(cl.player(), c.name());
                } else {
                    result(cl.player(), plugin.crews().accept(cl.player(), c.name()), "Te uniste a la crew.");
                }
                open(cl.player());
            });
        }

        m.set(53, icon(Material.BARRIER, "<red>Cerrar"), c -> c.player().closeInventory());
        p.openInventory(m.getInventory());
    }

    // ---------------- con crew ----------------
    private void openCrew(Player p, Crew crew) {
        boolean leader = crew.isLeader(p.getUniqueId());
        CrewMenu m = new CrewMenu(title(crew.name()));

        m.set(4, icon(Material.WHITE_BANNER, "<gold><bold>" + crew.name(),
                "<gray>Lider: <white>" + plugin.crews().nameOf(crew.leader()),
                "<gray>Miembros: <white>" + crew.members().size() + "/" + plugin.crews().maxMembers()), null);

        List<UUID> list = new ArrayList<>(crew.members());
        for (int i = 0; i < Math.min(list.size(), MEMBER_SLOTS.length); i++) {
            UUID id = list.get(i);
            OfflinePlayer op = Bukkit.getOfflinePlayer(id);
            boolean isLeader = crew.isLeader(id);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(op);
            meta.displayName(Msg.item((isLeader ? "<gold>\u2605 " : "<white>") + plugin.crews().nameOf(id)));
            List<Component> lore = new ArrayList<>();
            lore.add(Msg.item(op.isOnline() ? "<green>Conectado" : "<gray>Desconectado"));
            if (isLeader) lore.add(Msg.item("<gold>Lider"));
            if (leader && !id.equals(p.getUniqueId())) {
                lore.add(Component.empty());
                lore.add(Msg.item("<red>Click derecho: expulsar"));
            }
            meta.lore(lore);
            head.setItemMeta(meta);
            m.set(MEMBER_SLOTS[i], head, cl -> {
                if (leader && cl.type().isRightClick() && !id.equals(cl.player().getUniqueId())) {
                    click(cl.player());
                    result(cl.player(), plugin.crews().kick(cl.player(), id), null);
                    open(cl.player());
                }
            });
        }

        boolean canInvite = leader || plugin.getConfig().getBoolean("members-can-invite", true);
        if (canInvite) {
            m.set(46, icon(Material.WRITABLE_BOOK, "<green>Invitar jugador",
                    "<gray>Escribe el nombre del jugador", "<gray>en el chat."), c -> {
                click(c.player());
                c.player().closeInventory();
                plugin.input().ask(c.player(),
                        Msg.info("Escribe el <gold>nombre del jugador</gold> a invitar (o <red>cancelar</red>):"),
                        text -> {
                            Player target = Bukkit.getPlayerExact(text);
                            if (target == null) {
                                c.player().sendMessage(Msg.err("Ese jugador no esta conectado."));
                            } else {
                                String err = plugin.crews().invite(c.player(), target);
                                result(c.player(), err, "Invitacion enviada a <white>" + target.getName() + "</white>.");
                            }
                            open(c.player());
                        });
            });
        }

        if (leader) {
            m.set(48, icon(Material.NAME_TAG, "<yellow>Renombrar crew",
                    "<gray>Escribe el nuevo nombre", "<gray>en el chat."), c -> {
                click(c.player());
                c.player().closeInventory();
                plugin.input().ask(c.player(),
                        Msg.info("Escribe el <gold>nuevo nombre</gold> de la crew (o <red>cancelar</red>):"),
                        text -> {
                            result(c.player(), plugin.crews().rename(c.player(), text), null);
                            open(c.player());
                        });
            });
            m.set(52, icon(Material.TNT, "<dark_red>Disolver crew",
                    "<gray>Shift + click para confirmar."), c -> {
                if (c.type().isShiftClick()) {
                    click(c.player());
                    result(c.player(), plugin.crews().disband(c.player()), null);
                    open(c.player());
                }
            });
        }

        m.set(50, icon(Material.OAK_DOOR, "<red>Salir de la crew",
                "<gray>Shift + click para confirmar."), c -> {
            if (c.type().isShiftClick()) {
                click(c.player());
                result(c.player(), plugin.crews().leave(c.player()), "Saliste de la crew.");
                open(c.player());
            }
        });
        m.set(53, icon(Material.BARRIER, "<red>Cerrar"), c -> c.player().closeInventory());
        p.openInventory(m.getInventory());
    }
}
