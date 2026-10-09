package dev.crews;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public final class CrewManager {
    public record Invite(String crewKey, UUID inviter, long expiresAt) {}

    private static final Pattern NAME_RE = Pattern.compile("[A-Za-z0-9_]{3,16}");

    private final CrewsPlugin plugin;
    private final File file;
    private final Map<String, Crew> byKey = new LinkedHashMap<>();
    private final Map<UUID, Crew> byMember = new HashMap<>();
    private final Map<UUID, Map<String, Invite>> invites = new HashMap<>();

    public CrewManager(CrewsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crews.yml");
    }

    // ---------- consultas ----------
    public Crew of(UUID player) { return byMember.get(player); }
    public Crew byName(String name) { return byKey.get(name.toLowerCase()); }
    public int maxMembers() { return Math.max(2, Math.min(21, plugin.getConfig().getInt("max-members", 20))); }

    public List<Invite> invitesFor(UUID player) {
        Map<String, Invite> m = invites.get(player);
        if (m == null) return List.of();
        long now = System.currentTimeMillis();
        m.values().removeIf(i -> i.expiresAt() < now || !byKey.containsKey(i.crewKey()));
        return new ArrayList<>(m.values());
    }

    public Crew crewOf(Invite i) { return byKey.get(i.crewKey()); }

    // ---------- acciones (devuelven mensaje de error o null si OK) ----------
    public String validateName(String name, Crew ignore) {
        if (!NAME_RE.matcher(name).matches()) return "Nombre invalido: 3-16 caracteres, solo letras, numeros y _.";
        Crew other = byKey.get(name.toLowerCase());
        if (other != null && other != ignore) return "Ya existe una crew con ese nombre.";
        return null;
    }

    public String create(Player p, String name, String colorId, org.bukkit.inventory.ItemStack banner) {
        if (byMember.containsKey(p.getUniqueId())) return "Ya perteneces a una crew.";
        String v = validateName(name, null);
        if (v != null) return v;
        if (CrewStyle.findColor(colorId) == null) return "Color invalido.";
        Crew c = new Crew(name, p.getUniqueId());
        c.color(CrewStyle.color(colorId).id());
        org.bukkit.inventory.ItemStack clean = CrewStyle.cleanBanner(banner);
        if (clean != null) c.banner(clean);
        byKey.put(c.key(), c);
        byMember.put(p.getUniqueId(), c);
        invites.remove(p.getUniqueId());
        save();
        plugin.nameTags().refresh(p);
        return null;
    }

    public String setColor(Player actor, String colorId) {
        Crew c = byMember.get(actor.getUniqueId());
        if (c == null) return "No perteneces a ninguna crew.";
        if (!c.isLeader(actor.getUniqueId())) return "Solo el lider puede cambiar el color.";
        if (CrewStyle.findColor(colorId) == null) return "Color invalido.";
        c.color(CrewStyle.color(colorId).id());
        save();
        refreshMembers(c);
        return null;
    }

    public String setBanner(Player actor, org.bukkit.inventory.ItemStack banner) {
        Crew c = byMember.get(actor.getUniqueId());
        if (c == null) return "No perteneces a ninguna crew.";
        if (!c.isLeader(actor.getUniqueId())) return "Solo el lider puede cambiar el estandarte.";
        org.bukkit.inventory.ItemStack clean = CrewStyle.cleanBanner(banner);
        if (clean == null) return "Eso no es un estandarte.";
        c.banner(clean);
        save();
        return null;
    }

    public String invite(Player inviter, Player target) {
        Crew c = byMember.get(inviter.getUniqueId());
        if (c == null) return "No perteneces a ninguna crew.";
        if (!c.isLeader(inviter.getUniqueId()) && !plugin.getConfig().getBoolean("members-can-invite", true))
            return "Solo el lider puede invitar.";
        if (target.getUniqueId().equals(inviter.getUniqueId())) return "No puedes invitarte a ti mismo.";
        if (byMember.containsKey(target.getUniqueId())) return target.getName() + " ya pertenece a una crew.";
        if (c.members().size() >= maxMembers()) return "La crew esta llena.";
        long ttl = plugin.getConfig().getLong("invite-expire-seconds", 60) * 1000L;
        Map<String, Invite> m = invites.computeIfAbsent(target.getUniqueId(), k -> new LinkedHashMap<>());
        m.put(c.key(), new Invite(c.key(), inviter.getUniqueId(), System.currentTimeMillis() + ttl));

        var name = Placeholder.unparsed("crew", c.name());
        var who = Placeholder.unparsed("who", inviter.getName());
        Component accept = Component.text("[Aceptar]", net.kyori.adventure.text.format.NamedTextColor.GREEN)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/crew accept " + c.name()));
        Component deny = Component.text("[Rechazar]", net.kyori.adventure.text.format.NamedTextColor.RED)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/crew deny " + c.name()));
        target.sendMessage(Msg.info("<white><who></white> te invita a la crew <gold><crew></gold>.", who, name)
                .append(Component.space()).append(accept).append(Component.space()).append(deny));
        return null;
    }

    public String accept(Player p, String crewName) {
        if (byMember.containsKey(p.getUniqueId())) return "Ya perteneces a una crew.";
        List<Invite> list = invitesFor(p.getUniqueId());
        Invite inv = null;
        for (Invite i : list) {
            if (crewName == null || i.crewKey().equals(crewName.toLowerCase())) { inv = i; break; }
        }
        if (inv == null) return "No tienes esa invitacion (o expiro).";
        Crew c = byKey.get(inv.crewKey());
        if (c == null) return "Esa crew ya no existe.";
        if (c.members().size() >= maxMembers()) return "La crew esta llena.";
        c.members().add(p.getUniqueId());
        byMember.put(p.getUniqueId(), c);
        invites.remove(p.getUniqueId());
        save();
        broadcast(c, Msg.ok("<white><who></white> se unio a la crew.", Placeholder.unparsed("who", p.getName())));
        plugin.nameTags().refresh(p);
        return null;
    }

    public String deny(Player p, String crewName) {
        Map<String, Invite> m = invites.get(p.getUniqueId());
        if (m == null || m.isEmpty()) return "No tienes invitaciones.";
        if (crewName == null) m.clear();
        else if (m.remove(crewName.toLowerCase()) == null) return "No tienes esa invitacion.";
        return null;
    }

    public String leave(Player p) {
        Crew c = byMember.get(p.getUniqueId());
        if (c == null) return "No perteneces a ninguna crew.";
        if (c.members().size() == 1) { disbandInternal(c); return null; }
        c.members().remove(p.getUniqueId());
        byMember.remove(p.getUniqueId());
        if (c.isLeader(p.getUniqueId())) {
            UUID next = c.members().iterator().next();
            c.leader(next);
            broadcast(c, Msg.info("<white><who></white> es el nuevo lider.",
                    Placeholder.unparsed("who", nameOf(next))));
        }
        save();
        broadcast(c, Msg.info("<white><who></white> salio de la crew.", Placeholder.unparsed("who", p.getName())));
        plugin.nameTags().refresh(p);
        return null;
    }

    public String kick(Player actor, UUID target) {
        Crew c = byMember.get(actor.getUniqueId());
        if (c == null) return "No perteneces a ninguna crew.";
        if (!c.isLeader(actor.getUniqueId())) return "Solo el lider puede expulsar.";
        if (target.equals(actor.getUniqueId())) return "No puedes expulsarte a ti mismo.";
        if (!c.members().remove(target)) return "Ese jugador no esta en tu crew.";
        byMember.remove(target);
        save();
        broadcast(c, Msg.info("<white><who></white> fue expulsado de la crew.", Placeholder.unparsed("who", nameOf(target))));
        Player online = Bukkit.getPlayer(target);
        if (online != null) {
            online.sendMessage(Msg.err("Fuiste expulsado de la crew <gold><crew></gold>.", Placeholder.unparsed("crew", c.name())));
            plugin.nameTags().refresh(online);
        }
        return null;
    }

    public String disband(Player actor) {
        Crew c = byMember.get(actor.getUniqueId());
        if (c == null) return "No perteneces a ninguna crew.";
        if (!c.isLeader(actor.getUniqueId())) return "Solo el lider puede disolver la crew.";
        disbandInternal(c);
        return null;
    }

    public String rename(Player actor, String name) {
        Crew c = byMember.get(actor.getUniqueId());
        if (c == null) return "No perteneces a ninguna crew.";
        if (!c.isLeader(actor.getUniqueId())) return "Solo el lider puede renombrar.";
        String v = validateName(name, c);
        if (v != null) return v;
        byKey.remove(c.key());
        c.name(name);
        byKey.put(c.key(), c);
        invites.values().forEach(Map::clear);
        save();
        broadcast(c, Msg.info("La crew ahora se llama <gold><crew></gold>.", Placeholder.unparsed("crew", name)));
        refreshMembers(c);
        return null;
    }

    // ---------- utilidades ----------
    private void disbandInternal(Crew c) {
        List<UUID> ids = new ArrayList<>(c.members());
        broadcast(c, Msg.err("La crew <gold><crew></gold> fue disuelta.", Placeholder.unparsed("crew", c.name())));
        byKey.remove(c.key());
        ids.forEach(byMember::remove);
        save();
        for (UUID id : ids) {
            Player pl = Bukkit.getPlayer(id);
            if (pl != null) plugin.nameTags().refresh(pl);
        }
    }

    public void broadcast(Crew c, Component msg) {
        for (UUID id : c.members()) {
            Player pl = Bukkit.getPlayer(id);
            if (pl != null) pl.sendMessage(msg);
        }
    }

    private void refreshMembers(Crew c) {
        for (UUID id : c.members()) {
            Player pl = Bukkit.getPlayer(id);
            if (pl != null) plugin.nameTags().refresh(pl);
        }
    }

    public String nameOf(UUID id) {
        String n = Bukkit.getOfflinePlayer(id).getName();
        return n != null ? n : id.toString().substring(0, 8);
    }

    // ---------- persistencia ----------
    private static org.bukkit.inventory.ItemStack decodeBanner(String data) {
        try {
            return org.bukkit.inventory.ItemStack.deserializeBytes(Base64.getDecoder().decode(data));
        } catch (Exception e) {
            return new org.bukkit.inventory.ItemStack(CrewStyle.banner(data));
        }
    }

    public void load() {
        byKey.clear(); byMember.clear();
        if (!file.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = y.getConfigurationSection("crews");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            ConfigurationSection s = sec.getConfigurationSection(key);
            if (s == null) continue;
            try {
                Crew c = new Crew(s.getString("name", key), UUID.fromString(s.getString("leader", "")));
                for (String m : s.getStringList("members")) c.members().add(UUID.fromString(m));
                c.color(CrewStyle.color(s.getString("color", CrewStyle.DEFAULT_COLOR)).id());
                c.banner(decodeBanner(s.getString("banner", "")));
                byKey.put(c.key(), c);
                c.members().forEach(id -> byMember.put(id, c));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Crew corrupta ignorada: " + key);
            }
        }
    }

    public void save() {
        YamlConfiguration y = new YamlConfiguration();
        for (Crew c : byKey.values()) {
            String p = "crews." + c.key() + ".";
            y.set(p + "name", c.name());
            y.set(p + "leader", c.leader().toString());
            y.set(p + "color", c.color());
            y.set(p + "banner", Base64.getEncoder().encodeToString(c.banner().serializeAsBytes()));
            y.set(p + "members", c.members().stream().map(UUID::toString).toList());
        }
        try {
            plugin.getDataFolder().mkdirs();
            y.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar crews.yml: " + e.getMessage());
        }
    }
}
