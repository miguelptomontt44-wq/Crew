package dev.crews;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class CrewMenu implements InventoryHolder {
    public record Click(Player player, ClickType type) {}

    private final Inventory inventory;
    private final Map<Integer, Consumer<Click>> actions = new HashMap<>();

    public CrewMenu(Component title) {
        this.inventory = Bukkit.createInventory(this, 54, title);
    }

    public void set(int slot, ItemStack item, Consumer<Click> action) {
        inventory.setItem(slot, item);
        if (action != null) actions.put(slot, action);
    }

    public void handle(int slot, Player p, ClickType type) {
        Consumer<Click> a = actions.get(slot);
        if (a != null) a.accept(new Click(p, type));
    }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }
}
