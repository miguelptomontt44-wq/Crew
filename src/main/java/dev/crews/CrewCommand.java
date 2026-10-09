package dev.crews;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CrewCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBS =
            List.of("menu", "create", "color", "banner", "invite", "accept", "deny", "leave", "kick", "rename", "disband", "info");

    private final CrewsPlugin plugin;

    public CrewCommand(CrewsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] a) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Solo jugadores."); return true; }
        CrewManager cm = plugin.crews();
        if (a.length == 0 || a[0].equalsIgnoreCase("menu")) { plugin.menus().open(p); return true; }

        String arg = a.length > 1 ? a[1] : null;
        String err;
        switch (a[0].toLowerCase()) {
            case "create" -> {
                if (arg == null) { p.sendMessage(Msg.err("Uso: /crew create <nombre> [color]")); return true; }
                String color = a.length > 2 ? a[2].toLowerCase() : CrewStyle.DEFAULT_COLOR;
                if (CrewStyle.findColor(color) == null) {
                    p.sendMessage(Msg.err("Color invalido. Usa el menu o: " + colorIds()));
                    return true;
                }
                err = cm.create(p, arg, color, new org.bukkit.inventory.ItemStack(CrewStyle.DEFAULT_BANNER));
                reply(p, err, "Crew <gold>" + arg + "</gold> creada. Elige el estandarte desde el menu.");
            }
            case "banner" -> reply(p, cm.setBanner(p, p.getInventory().getItemInMainHand()),
                    "Estandarte de la crew actualizado con el que llevas en la mano.");
            case "color" -> {
                if (arg == null) { p.sendMessage(Msg.err("Uso: /crew color <color>  (" + colorIds() + ")")); return true; }
                reply(p, cm.setColor(p, arg.toLowerCase()), "Color actualizado.");
            }
            case "invite" -> {
                if (arg == null) { p.sendMessage(Msg.err("Uso: /crew invite <jugador>")); return true; }
                Player t = Bukkit.getPlayerExact(arg);
                if (t == null) { p.sendMessage(Msg.err("Ese jugador no esta conectado.")); return true; }
                reply(p, cm.invite(p, t), "Invitacion enviada a <white>" + t.getName() + "</white>.");
            }
            case "accept" -> reply(p, cm.accept(p, arg), "Te uniste a la crew.");
            case "deny" -> reply(p, cm.deny(p, arg), "Invitacion rechazada.");
            case "leave" -> reply(p, cm.leave(p), "Saliste de la crew.");
            case "disband" -> reply(p, cm.disband(p), null);
            case "rename" -> {
                if (arg == null) { p.sendMessage(Msg.err("Uso: /crew rename <nombre>")); return true; }
                reply(p, cm.rename(p, arg), null);
            }
            case "kick" -> {
                if (arg == null) { p.sendMessage(Msg.err("Uso: /crew kick <jugador>")); return true; }
                Crew c = cm.of(p.getUniqueId());
                UUID target = null;
                if (c != null) for (UUID id : c.members()) if (cm.nameOf(id).equalsIgnoreCase(arg)) target = id;
                if (target == null) { p.sendMessage(Msg.err("Ese jugador no esta en tu crew.")); return true; }
                reply(p, cm.kick(p, target), null);
            }
            case "info" -> {
                Crew c = cm.of(p.getUniqueId());
                if (c == null) { p.sendMessage(Msg.info("No perteneces a ninguna crew.")); return true; }
                List<String> names = new ArrayList<>();
                c.members().forEach(id -> names.add(cm.nameOf(id)));
                p.sendMessage(Msg.info("Crew <gold><crew></gold> | Lider: <white><l></white> | Miembros: <white><m></white>",
                        Placeholder.unparsed("crew", c.name()),
                        Placeholder.unparsed("l", cm.nameOf(c.leader())),
                        Placeholder.unparsed("m", String.join(", ", names))));
            }
            default -> p.sendMessage(Msg.err("Uso: /crew [" + String.join("|", SUBS) + "]"));
        }
        return true;
    }

    private static String colorIds() {
        return String.join(", ", CrewStyle.COLORS.stream().map(CrewStyle.ColorOpt::id).toList());
    }

    private void reply(Player p, String err, String ok) {
        if (err != null) p.sendMessage(Msg.err(err));
        else if (ok != null) p.sendMessage(Msg.ok(ok));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            for (String sub : SUBS) if (sub.startsWith(a[0].toLowerCase())) out.add(sub);
        } else if ((a.length == 3 && a[0].equalsIgnoreCase("create")) || (a.length == 2 && a[0].equalsIgnoreCase("color"))) {
            String pre = a[a.length - 1].toLowerCase();
            for (CrewStyle.ColorOpt opt : CrewStyle.COLORS) if (opt.id().startsWith(pre)) out.add(opt.id());
        } else if (a.length == 2 && (a[0].equalsIgnoreCase("invite") || a[0].equalsIgnoreCase("kick"))) {
            for (Player p : Bukkit.getOnlinePlayers())
                if (p.getName().toLowerCase().startsWith(a[1].toLowerCase())) out.add(p.getName());
        }
        return out;
    }
}
