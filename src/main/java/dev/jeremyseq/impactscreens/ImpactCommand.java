package dev.jeremyseq.impactscreens;

import dev.jeremyseq.impactscreens.networking.TriggerOverlayPacket;
import dev.jeremyseq.impactscreens.overlays.ImpactImages;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;

public class ImpactCommand {

    private static final SuggestionProvider<CommandSourceStack> IMAGE_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(ImpactImages.getAll().keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("impactscreen")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("image", StringArgumentType.word())
                                .suggests(IMAGE_SUGGESTIONS)
                                // /impactscreen <image> text <message>
                                .then(Commands.literal("text")
                                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                                .executes(ctx -> run(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "image"),
                                                        StringArgumentType.getString(ctx, "text"),
                                                        ChatFormatting.WHITE,
                                                        null
                                                ))
                                        )
                                )
                                // /impactscreen <image> color <color> text <message>
                                .then(Commands.literal("color")
                                        .then(Commands.argument("color", ColorArgument.color())
                                                .then(Commands.literal("text")
                                                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                                                .executes(ctx -> run(
                                                                        ctx.getSource(),
                                                                        StringArgumentType.getString(ctx, "image"),
                                                                        StringArgumentType.getString(ctx, "text"),
                                                                        ColorArgument.getColor(ctx, "color"),
                                                                        null
                                                                ))
                                                        )
                                                )
                                                // /impactscreen <image> color <color> targets <targets> text <message>
                                                .then(Commands.literal("targets")
                                                        .then(Commands.argument("targets", EntityArgument.players())
                                                                .then(Commands.literal("text")
                                                                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                                                                .executes(ctx -> run(
                                                                                        ctx.getSource(),
                                                                                        StringArgumentType.getString(ctx, "image"),
                                                                                        StringArgumentType.getString(ctx, "text"),
                                                                                        ColorArgument.getColor(ctx, "color"),
                                                                                        EntityArgument.getPlayers(ctx, "targets")
                                                                                ))
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }

    private static int run(CommandSourceStack source, String image, String text,
                           ChatFormatting color, Collection<ServerPlayer> targets) {

        TriggerOverlayPacket packet = new TriggerOverlayPacket(image, text, color.getColor() == null ? 0xFFFFFF : color.getColor());

        if (targets == null) {
            MinecraftServer server = source.getServer();
            PacketDistributor.sendToAllPlayers(packet);
        } else {
            for (ServerPlayer player : targets) {
                PacketDistributor.sendToPlayer(player, packet);
            }
        }

        return 1;
    }
}