package dev.crews;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.List;

/** Colores y estandartes disponibles para una crew. */
public final class CrewStyle {
    public record ColorOpt(String id, String label, NamedTextColor color, Material icon) {}
    public record BannerOpt(Material material, String label) {}

    public static final String DEFAULT_COLOR = "gold";
    public static final Material DEFAULT_BANNER = Material.WHITE_BANNER;

    /** Los ids coinciden con los nombres de color de MiniMessage. */
    public static final List<ColorOpt> COLORS = List.of(
            new ColorOpt("white", "Blanco", NamedTextColor.WHITE, Material.WHITE_CONCRETE),
            new ColorOpt("gray", "Gris", NamedTextColor.GRAY, Material.LIGHT_GRAY_CONCRETE),
            new ColorOpt("dark_gray", "Gris oscuro", NamedTextColor.DARK_GRAY, Material.GRAY_CONCRETE),
            new ColorOpt("black", "Negro", NamedTextColor.BLACK, Material.BLACK_CONCRETE),
            new ColorOpt("red", "Rojo", NamedTextColor.RED, Material.RED_CONCRETE),
            new ColorOpt("dark_red", "Rojo oscuro", NamedTextColor.DARK_RED, Material.RED_TERRACOTTA),
            new ColorOpt("gold", "Dorado", NamedTextColor.GOLD, Material.ORANGE_CONCRETE),
            new ColorOpt("yellow", "Amarillo", NamedTextColor.YELLOW, Material.YELLOW_CONCRETE),
            new ColorOpt("green", "Verde", NamedTextColor.GREEN, Material.LIME_CONCRETE),
            new ColorOpt("dark_green", "Verde oscuro", NamedTextColor.DARK_GREEN, Material.GREEN_CONCRETE),
            new ColorOpt("aqua", "Celeste", NamedTextColor.AQUA, Material.LIGHT_BLUE_CONCRETE),
            new ColorOpt("dark_aqua", "Turquesa", NamedTextColor.DARK_AQUA, Material.CYAN_CONCRETE),
            new ColorOpt("blue", "Azul", NamedTextColor.BLUE, Material.BLUE_CONCRETE),
            new ColorOpt("dark_blue", "Azul oscuro", NamedTextColor.DARK_BLUE, Material.BLUE_TERRACOTTA),
            new ColorOpt("light_purple", "Rosa", NamedTextColor.LIGHT_PURPLE, Material.MAGENTA_CONCRETE),
            new ColorOpt("dark_purple", "Morado", NamedTextColor.DARK_PURPLE, Material.PURPLE_CONCRETE));

    /** Los 16 estandartes lisos de Minecraft. */
    public static final List<BannerOpt> BANNERS = List.of(
            new BannerOpt(Material.WHITE_BANNER, "Blanco"),
            new BannerOpt(Material.LIGHT_GRAY_BANNER, "Gris claro"),
            new BannerOpt(Material.GRAY_BANNER, "Gris"),
            new BannerOpt(Material.BLACK_BANNER, "Negro"),
            new BannerOpt(Material.BROWN_BANNER, "Marron"),
            new BannerOpt(Material.RED_BANNER, "Rojo"),
            new BannerOpt(Material.ORANGE_BANNER, "Naranja"),
            new BannerOpt(Material.YELLOW_BANNER, "Amarillo"),
            new BannerOpt(Material.LIME_BANNER, "Lima"),
            new BannerOpt(Material.GREEN_BANNER, "Verde"),
            new BannerOpt(Material.CYAN_BANNER, "Cian"),
            new BannerOpt(Material.LIGHT_BLUE_BANNER, "Celeste"),
            new BannerOpt(Material.BLUE_BANNER, "Azul"),
            new BannerOpt(Material.PURPLE_BANNER, "Morado"),
            new BannerOpt(Material.MAGENTA_BANNER, "Magenta"),
            new BannerOpt(Material.PINK_BANNER, "Rosa"));

    public record PatternOpt(PatternType type, String label) {}

    /** Patrones vanilla que se pueden aplicar en el disenador. */
    public static final List<PatternOpt> PATTERNS = List.of(
            new PatternOpt(PatternType.SQUARE_BOTTOM_LEFT, "Cuadrado abajo-izquierda"),
            new PatternOpt(PatternType.SQUARE_BOTTOM_RIGHT, "Cuadrado abajo-derecha"),
            new PatternOpt(PatternType.SQUARE_TOP_LEFT, "Cuadrado arriba-izquierda"),
            new PatternOpt(PatternType.SQUARE_TOP_RIGHT, "Cuadrado arriba-derecha"),
            new PatternOpt(PatternType.STRIPE_BOTTOM, "Franja inferior"),
            new PatternOpt(PatternType.STRIPE_TOP, "Franja superior"),
            new PatternOpt(PatternType.STRIPE_LEFT, "Franja izquierda"),
            new PatternOpt(PatternType.STRIPE_RIGHT, "Franja derecha"),
            new PatternOpt(PatternType.STRIPE_CENTER, "Franja vertical central"),
            new PatternOpt(PatternType.STRIPE_MIDDLE, "Franja horizontal central"),
            new PatternOpt(PatternType.STRIPE_DOWNRIGHT, "Diagonal descendente"),
            new PatternOpt(PatternType.STRIPE_DOWNLEFT, "Diagonal ascendente"),
            new PatternOpt(PatternType.SMALL_STRIPES, "Franjas pequenas"),
            new PatternOpt(PatternType.CROSS, "Cruz diagonal"),
            new PatternOpt(PatternType.STRAIGHT_CROSS, "Cruz recta"),
            new PatternOpt(PatternType.TRIANGLE_BOTTOM, "Triangulo inferior"),
            new PatternOpt(PatternType.TRIANGLE_TOP, "Triangulo superior"),
            new PatternOpt(PatternType.TRIANGLES_BOTTOM, "Dientes inferiores"),
            new PatternOpt(PatternType.TRIANGLES_TOP, "Dientes superiores"),
            new PatternOpt(PatternType.DIAGONAL_LEFT, "Diagonal superior izquierda"),
            new PatternOpt(PatternType.DIAGONAL_RIGHT, "Diagonal inferior derecha"),
            new PatternOpt(PatternType.DIAGONAL_UP_LEFT, "Diagonal inferior izquierda"),
            new PatternOpt(PatternType.DIAGONAL_UP_RIGHT, "Diagonal superior derecha"),
            new PatternOpt(PatternType.CIRCLE, "Circulo"),
            new PatternOpt(PatternType.RHOMBUS, "Rombo"),
            new PatternOpt(PatternType.HALF_VERTICAL, "Mitad izquierda"),
            new PatternOpt(PatternType.HALF_VERTICAL_RIGHT, "Mitad derecha"),
            new PatternOpt(PatternType.HALF_HORIZONTAL, "Mitad superior"),
            new PatternOpt(PatternType.HALF_HORIZONTAL_BOTTOM, "Mitad inferior"),
            new PatternOpt(PatternType.BORDER, "Borde"),
            new PatternOpt(PatternType.CURLY_BORDER, "Borde ondulado"),
            new PatternOpt(PatternType.GRADIENT, "Degradado superior"),
            new PatternOpt(PatternType.GRADIENT_UP, "Degradado inferior"),
            new PatternOpt(PatternType.BRICKS, "Ladrillos"),
            new PatternOpt(PatternType.GLOBE, "Globo"),
            new PatternOpt(PatternType.CREEPER, "Creeper"),
            new PatternOpt(PatternType.SKULL, "Calavera"),
            new PatternOpt(PatternType.FLOWER, "Flor"),
            new PatternOpt(PatternType.MOJANG, "Mojang"),
            new PatternOpt(PatternType.PIGLIN, "Piglin"),
            new PatternOpt(PatternType.FLOW, "Flujo"),
            new PatternOpt(PatternType.GUSTER, "Guster"));

    private CrewStyle() {}

    /** Copia limpia de un estandarte (solo material + patrones), o null si no es un estandarte. */
    public static ItemStack cleanBanner(ItemStack src) {
        if (src == null || !src.getType().name().endsWith("_BANNER")) return null;
        ItemStack out = new ItemStack(src.getType());
        if (src.getItemMeta() instanceof BannerMeta from && out.getItemMeta() instanceof BannerMeta to) {
            to.setPatterns(from.getPatterns());
            out.setItemMeta(to);
        }
        return out;
    }

    /** Devuelve la opcion de color o null si el id no existe. */
    public static ColorOpt findColor(String id) {
        if (id == null) return null;
        for (ColorOpt c : COLORS) if (c.id().equalsIgnoreCase(id)) return c;
        return null;
    }

    public static ColorOpt color(String id) {
        ColorOpt c = findColor(id);
        return c != null ? c : findColor(DEFAULT_COLOR);
    }

    public static Material banner(String name) {
        for (BannerOpt b : BANNERS) if (b.material().name().equalsIgnoreCase(name)) return b.material();
        return DEFAULT_BANNER;
    }
}
