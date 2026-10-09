package dev.crews;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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
    private static final int[] PICK_SLOTS = {
            19, 20, 21, 22, 23, 24, 25, 26,
            28, 29, 30, 31, 32, 33, 34, 35};
    private static final int MAX_LAYERS = 6;

    private final CrewsPlugin plugin;

    public MenuManager(CrewsPlugin plugin) { this.plugin = plugin; }

    // ================= utilidades =================
    private Component title(String text) {
        return Component.text(SHIFT_BACK_8 + BACKGROUND, NamedTextColor.WHITE).font(FONT)
                .append(Component.text(SHIFT_TO_TITLE).font(FONT))
                .append(Component.text(text, TextColor.color(0x404040)).font(DEFAULT_FONT));
    }

    private ItemStack decorate(ItemStack it, String name, String... lore) {
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

    private ItemStack icon(Material m, String name, String... lore) {
        return decorate(new ItemStack(m), name, lore);
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

    // ================= sin crew =================
    private void openNoCrew(Player p) {
        CrewMenu m = new CrewMenu(title("Crews"));
        m.set(4, icon(Material.WHITE_BANNER, "<gold><bold>Sin crew",
                "<gray>Crea una o acepta una invitacion."), null);

        m.set(49, icon(Material.EMERALD, "<green>Crear crew",
                "<gray>Nombre, color y estandarte."), c -> {
            click(c.player());
            startCreate(c.player());
        });

        List<CrewManager.Invite> invs = plugin.crews().invitesFor(p.getUniqueId());
        for (int i = 0; i < Math.min(invs.size(), MEMBER_SLOTS.length); i++) {
            CrewManager.Invite inv = invs.get(i);
            Crew c = plugin.crews().crewOf(inv);
            if (c == null) continue;
            String col = c.color();
            m.set(MEMBER_SLOTS[i], decorate(c.banner(),
                    "<yellow>Invitacion: <" + col + ">" + c.name(),
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

    /** Flujo de creacion: nombre (chat) -> color -> estandarte. */
    private void startCreate(Player p) {
        p.closeInventory();
        plugin.input().ask(p,
                Msg.info("Escribe el <gold>nombre de tu crew</gold> en el chat (o <red>cancelar</red>):"),
                text -> {
                    String err = plugin.crews().validateName(text, null);
                    if (err != null) { p.sendMessage(Msg.err(err)); open(p); return; }
                    pickColor(p, () -> open(p), colorId ->
                            pickBanner(p, () -> open(p), banner -> {
                                result(p, plugin.crews().create(p, text, colorId, banner), "Crew creada.");
                                open(p);
                            }));
                });
    }

    // ================= con crew =================
    private void openCrew(Player p, Crew crew) {
        boolean leader = crew.isLeader(p.getUniqueId());
        String col = crew.color();
        CrewMenu m = new CrewMenu(title(crew.name()));

        m.set(4, decorate(crew.banner(), "<" + col + "><bold>" + crew.name(),
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
            m.set(45, icon(Material.WRITABLE_BOOK, "<green>Invitar jugador",
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
            m.set(46, icon(Material.NAME_TAG, "<yellow>Renombrar crew",
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
            m.set(47, icon(CrewStyle.color(col).icon(), "<" + col + ">Cambiar color",
                    "<gray>Color actual: <" + col + ">" + CrewStyle.color(col).label()), c -> {
                click(c.player());
                pickColor(c.player(), () -> open(c.player()), id -> {
                    result(c.player(), plugin.crews().setColor(c.player(), id), "Color actualizado.");
                    open(c.player());
                });
            });
            m.set(48, decorate(crew.banner(), "<white>Cambiar estandarte",
                    "<gray>Disenalo o usa el que", "<gray>tienes en la mano."), c -> {
                click(c.player());
                pickBanner(c.player(), () -> open(c.player()), b -> {
                    result(c.player(), plugin.crews().setBanner(c.player(), b), "Estandarte actualizado.");
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

        m.set(51, icon(Material.OAK_DOOR, "<red>Salir de la crew",
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

    // ================= selector de color =================
    private void pickColor(Player p, Runnable onCancel, Consumer<String> onPick) {
        CrewMenu m = new CrewMenu(title("Elige un color"));
        m.set(4, icon(Material.PAINTING, "<gold>Color de la crew",
                "<gray>Es el color del nombre que", "<gray>aparece bajo tu nombre."), null);
        for (int i = 0; i < CrewStyle.COLORS.size(); i++) {
            CrewStyle.ColorOpt opt = CrewStyle.COLORS.get(i);
            m.set(PICK_SLOTS[i], icon(opt.icon(), "<" + opt.id() + ">" + opt.label(),
                    "<gray>Click para elegir"), c -> {
                click(c.player());
                onPick.accept(opt.id());
            });
        }
        m.set(53, icon(Material.BARRIER, "<red>Cancelar"), c -> onCancel.run());
        p.openInventory(m.getInventory());
    }

    // ================= selector de estandarte =================
    private void pickBanner(Player p, Runnable onCancel, Consumer<ItemStack> onPick) {
        CrewMenu m = new CrewMenu(title("Estandarte"));
        m.set(4, icon(Material.WHITE_BANNER, "<gold>Estandarte de la crew",
                "<gray>Es el simbolo de tu hermandad."), null);

        m.set(21, icon(Material.LOOM, "<green>Disenar estandarte",
                "<gray>Elige color base y hasta 6 capas",
                "<gray>con los patrones de Minecraft."), c -> {
            click(c.player());
            openDesigner(c.player(), new Design(onCancel, onPick));
        });

        m.set(23, icon(Material.WHITE_BANNER, "<aqua>Usar el de mi mano",
                "<gray>Usa el estandarte que llevas en la",
                "<gray>mano principal (hazlo en el telar",
                "<gray>y sostenlo antes de abrir el menu)."), c -> {
            ItemStack clean = CrewStyle.cleanBanner(c.player().getInventory().getItemInMainHand());
            if (clean == null) {
                c.player().sendMessage(Msg.err("Sostén un estandarte en la mano principal y vuelve a intentarlo."));
                c.player().closeInventory();
                return;
            }
            click(c.player());
            onPick.accept(clean);
        });

        m.set(53, icon(Material.BARRIER, "<red>Cancelar"), c -> onCancel.run());
        p.openInventory(m.getInventory());
    }

    // ================= disenador de estandartes =================
    private static final class Design {
        Material base = Material.WHITE_BANNER;
        final List<Pattern> layers = new ArrayList<>();
        final Runnable onCancel;
        final Consumer<ItemStack> onPick;

        Design(Runnable onCancel, Consumer<ItemStack> onPick) {
            this.onCancel = onCancel;
            this.onPick = onPick;
        }
    }

    private ItemStack render(Design d, Pattern extra) {
        ItemStack it = new ItemStack(d.base);
        BannerMeta meta = (BannerMeta) it.getItemMeta();
        List<Pattern> all = new ArrayList<>(d.layers);
        if (extra != null) all.add(extra);
        meta.setPatterns(all);
        it.setItemMeta(meta);
        return it;
    }

    private static DyeColor dyeOf(Material banner) {
        return DyeColor.valueOf(banner.name().replace("_BANNER", ""));
    }

    private static Material dyeItem(DyeColor c) {
        return Material.valueOf(c.name() + "_DYE");
    }

    /** Color que contrasta con la base para previsualizar patrones. */
    private static DyeColor contrast(Material base) {
        return switch (dyeOf(base)) {
            case BLACK, GRAY, BROWN, BLUE, PURPLE, GREEN, RED, CYAN -> DyeColor.WHITE;
            default -> DyeColor.BLACK;
        };
    }

    private void openDesigner(Player p, Design d) {
        CrewMenu m = new CrewMenu(title("Disenar estandarte"));
        m.set(4, decorate(render(d, null), "<gold>Vista previa",
                "<gray>Capas: <white>" + d.layers.size() + "/" + MAX_LAYERS), null);

        m.set(20, icon(d.base, "<white>Color base", "<gray>Cambia el color de fondo."), c -> {
            click(c.player());
            pickBase(c.player(), d);
        });

        if (d.layers.size() < MAX_LAYERS) {
            m.set(22, icon(Material.LOOM, "<green>Anadir capa", "<gray>Elige un patron y su color."), c -> {
                click(c.player());
                pickPattern(c.player(), d);
            });
        } else {
            m.set(22, icon(Material.BARRIER, "<gray>Maximo de capas alcanzado"), null);
        }

        if (!d.layers.isEmpty()) {
            m.set(24, icon(Material.ARROW, "<yellow>Deshacer ultima capa"), c -> {
                click(c.player());
                d.layers.remove(d.layers.size() - 1);
                openDesigner(c.player(), d);
            });
        }

        m.set(40, icon(Material.EMERALD, "<green><bold>Terminar", "<gray>Usar este estandarte."), c -> {
            click(c.player());
            d.onPick.accept(render(d, null));
        });
        m.set(53, icon(Material.BARRIER, "<red>Cancelar"), c -> d.onCancel.run());
        p.openInventory(m.getInventory());
    }

    private void pickBase(Player p, Design d) {
        CrewMenu m = new CrewMenu(title("Color base"));
        for (int i = 0; i < CrewStyle.BANNERS.size(); i++) {
            CrewStyle.BannerOpt opt = CrewStyle.BANNERS.get(i);
            m.set(PICK_SLOTS[i], icon(opt.material(), "<white>" + opt.label()), c -> {
                click(c.player());
                d.base = opt.material();
                openDesigner(c.player(), d);
            });
        }
        m.set(49, icon(Material.ARROW, "<yellow>Volver"), c -> openDesigner(c.player(), d));
        p.openInventory(m.getInventory());
    }

    private void pickPattern(Player p, Design d) {
        CrewMenu m = new CrewMenu(title("Elige un patron"));
        DyeColor preview = contrast(d.base);
        for (int i = 0; i < CrewStyle.PATTERNS.size() && i < 45; i++) {
            CrewStyle.PatternOpt opt = CrewStyle.PATTERNS.get(i);
            ItemStack it = decorate(render(d, new Pattern(preview, opt.type())),
                    "<white>" + opt.label(), "<gray>Click para elegir");
            m.set(i, it, c -> {
                click(c.player());
                pickDye(c.player(), d, opt);
            });
        }
        m.set(49, icon(Material.ARROW, "<yellow>Volver"), c -> openDesigner(c.player(), d));
        p.openInventory(m.getInventory());
    }

    private void pickDye(Player p, Design d, CrewStyle.PatternOpt opt) {
        CrewMenu m = new CrewMenu(title("Color del patron"));
        m.set(4, decorate(render(d, null), "<gold>" + opt.label(), "<gray>Elige el color de esta capa."), null);
        for (int i = 0; i < CrewStyle.BANNERS.size(); i++) {
            CrewStyle.BannerOpt b = CrewStyle.BANNERS.get(i);
            DyeColor dye = dyeOf(b.material());
            m.set(PICK_SLOTS[i], icon(dyeItem(dye), "<white>" + b.label()), c -> {
                click(c.player());
                d.layers.add(new Pattern(dye, opt.type()));
                openDesigner(c.player(), d);
            });
        }
        m.set(49, icon(Material.ARROW, "<yellow>Volver"), c -> pickPattern(c.player(), d));
        p.openInventory(m.getInventory());
    }
}
