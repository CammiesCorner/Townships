package dev.cammiescorner.townships.util;

import dev.cammiescorner.townships.Townships;
import dev.cammiescorner.townships.component.level.ClaimsComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

// TODO make translatable with icu4j
/**
 * error messages are denoted with _E
 */
public class TownMessages {
    public static final Component ENTER_AREA_WILDERNESS = message("claim.area.wilderness", "Wilderness").withStyle(ChatFormatting.GREEN);

    public static Component enterArea_town(Town town) {
        return message("claim.area.town", town.displayName(), town.displayName()).withStyle(ChatFormatting.GOLD);
    }

    public static Component enterArea_townHome(Town town) {
        return message("claim.area.town_home", town.displayName() + " (Home)", town.displayName()).withStyle(ChatFormatting.GOLD);
    }

    public static MutableComponent message(String key, String fallback) {
        return Component.translatableWithFallback("message.%s.%s".formatted(Townships.MOD_ID, key), fallback);
    }

    public static MutableComponent message(String key, String fallback, Object... args) {
        return Component.translatableWithFallback("message.%s.%s".formatted(Townships.MOD_ID, key), fallback, args);
    }

    public static MutableComponent playerProfile(String name, UUID uuid) {
        return Component.literal(name).withStyle(ChatFormatting.BLUE).withStyle(style -> style
                .withClickEvent(new ClickEvent.CopyToClipboard(uuid.toString()))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(uuid.toString()).withStyle(ChatFormatting.GRAY)))
        );
    }

    public static Component townMembers(Town town, MinecraftServer server, int page) {
        var playerCache = server.services().nameToIdCache();
        var members = town.members().keySet().stream()
                .map(playerCache::get)
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .sorted(Comparator.comparing(NameAndId::name))
                .map(nameAndId -> playerProfile(nameAndId.name(), nameAndId.id()))
                .toList();

        // TODO finish
        throw new UnsupportedOperationException("Not implemented");
    }

    public static Component townInfo(Town town, ClaimsComponent claimComponent) {
        return Component.literal("Town: " + town.displayName()).append("\n")
                .append("Gold: " + town.balance()).append("\n")
                .append("Chunks Claimed: " + claimComponent.getChunks(town.id()).size()).append("\n")
                .append("Members: " + town.members().size());
    }

    /**
     * unable to create town: already in a town
     */
    public static Component createTown_E_inTown(Town town) {
        return Component.literal("You're already part of a town: %s!".formatted(town.displayName()));
    }

    public static Component createTown_success(Town town) {
        return Component.literal("Created town %s".formatted(town.displayName()));
    }
}
