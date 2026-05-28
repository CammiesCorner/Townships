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
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

// TODO make translatable with icu4j
public class TownMessages {
    public static class EnterArea {
        public static final Component WILDERNESS = message("enter_area.wilderness", "Wilderness").withStyle(ChatFormatting.GREEN);

        public static Component town(Town town) {
            return message("enter_area.town", town.displayName(), town.displayName()).withStyle(ChatFormatting.AQUA);
        }

        public static Component townHome(Town town) {
            return message("enter_area.town_home", town.displayName() + " §f[Home]", town.displayName()).withStyle(ChatFormatting.AQUA);
        }
    }

    public static class Generic {
        public static MutableComponent playerProfile(String name, UUID uuid) {
            return Component.literal(name).withStyle(ChatFormatting.BLUE).withStyle(style -> style
                    .withClickEvent(new ClickEvent.CopyToClipboard(uuid.toString()))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal(uuid.toString()).withStyle(ChatFormatting.GRAY)))
            );
        }
    }

    public static class Towns {

        public static class Create {
            public static Component success(Town town) {
                return message("town.create.success", "Created town %s".formatted(town.displayName()));
            }

            public static class Error {

                /**
                 * unable to create town: already in a town
                 */
                public static Component alreadyInTown(Town town) {
                    return message("town.create.error.already_in_town", "You're already part of a town: %s!".formatted(town.displayName()));
                }
            }
        }

        public static class Claim {
            public static Component success(ChunkPos chunkPos) {
                return message("town.claim.succes", "Claimed chunk %s/%s".formatted(chunkPos.x(), chunkPos.z()));
            }

            public static class Error {
                public static Component missingPermission(Member.Rank currentRank) {
                    return message("town.claim.error.missing_permission", "Your current rank does not allow you to claim territory!");
                }

                public static Component ownedByOtherTown(ChunkPos chunkPos, Town town) {
                    return message("town.claim.error.owned_by_other_town", "Chunk %s/%s is already claimed by %s!".formatted(chunkPos.x(), chunkPos.z(), town.displayName()));
                }
            }
        }

        public static class Unclaim {
            public static Component success(ChunkPos chunkPos) {
                return message("town.unclaim.success", "Unclaimed chunk %s/%s".formatted(chunkPos.x(), chunkPos.z()));
            }

            public static class Error {
                public static Component claimedByOtherTown(ChunkPos chunkPos, Town otherTown) {
                    return message("town.unclaim.error.claimed_by_other_town", "This chunk belongs to %s, but you are not a member of that town. you cannot unclaim it!".formatted(otherTown.displayName()));
                }

                public static Component missingPermission(Member.Rank currentRank) {
                    return message("town.unclaim.error.missing_permission", "Your current rank does not allow you to unclaim territory!");
                }

                public static Component notClaimed(ChunkPos chunkPos) {
                    return message("town.unclaim.error.not_claimed", "Chunk %s/%s is not claimed by anybody!".formatted(chunkPos.x(), chunkPos.z()));
                }
            }
        }

        public static class Info {
            public static Component listMembers(Town town, MinecraftServer server, int page) {
                var playerCache = server.services().nameToIdCache();
                var members = town.members().keySet().stream()
                        .map(playerCache::get)
                        .filter(Optional::isPresent)
                        .map(Optional::orElseThrow)
                        .sorted(Comparator.comparing(NameAndId::name))
                        .map(nameAndId -> Generic.playerProfile(nameAndId.name(), nameAndId.id()))
                        .toList();

                // TODO finish
                throw new UnsupportedOperationException("Not implemented");
            }

            public static Component listStats(Town town, ClaimsComponent claimComponent) {
                return Component.empty()
                        .append(message("town.info.list_stats.name", "Town: " + town.displayName())).append("\n")
                        .append(message("town.info.list_stats.chunks", "Chunks Claimed: %s".formatted(claimComponent.getChunks(town.id()).size()))).append("\n")
                        .append(message("town.info.list_stats.balance", "Balance: %s Gold".formatted(town.balance()))).append("\n")
                        .append("Members: %s".formatted(town.members().size()));
            }
        }

        public static class Error {
            public static final Component NOT_IN_TOWN = message("generic.error.not_in_town", "You must create a town first!");
        }
    }

    public static MutableComponent message(String key, String fallback) {
        return Component.translatableWithFallback("message.%s.%s".formatted(Townships.MOD_ID, key), fallback);
    }

    public static MutableComponent message(String key, String fallback, Object... args) {
        return Component.translatableWithFallback("message.%s.%s".formatted(Townships.MOD_ID, key), fallback, args);
    }
}
