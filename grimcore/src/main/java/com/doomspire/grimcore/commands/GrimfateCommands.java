package com.doomspire.grimcore.commands;

import com.doomspire.grimcore.attach.PlayerProgressAttachment;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.net.ProgressNetworking;
import com.doomspire.grimcore.stat.ModAttachments;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public final class GrimfateCommands {
    private GrimfateCommands(){}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(
                Commands.literal("grimfate")
                        .requires(src -> src.hasPermission(2)) // только операторы по умолчанию
                        .then(Commands.literal("give")
                                .then(Commands.literal("exp")
                                        // вариант: /grimfate give exp <amount>
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    ServerPlayer target = ctx.getSource().getPlayerOrException();
                                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                                    return giveExp(ctx.getSource(), List.of(target), amount);
                                                })
                                                // вариант: /grimfate give exp <amount> <targets>
                                                .then(Commands.argument("targets", EntityArgument.players())
                                                        .executes(ctx -> {
                                                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
                                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                                            return giveExp(ctx.getSource(), targets, amount);
                                                        })
                                                )
                                        )
                                )
                        )
        );
    }

    private static int giveExp(CommandSourceStack src, Collection<ServerPlayer> targets, int amount) {
        int totalLevels = 0;

        for (ServerPlayer player : targets) {
            PlayerProgressAttachment prog = player.getData(ModAttachments.PLAYER_PROGRESS.get());
            PlayerStatsAttachment    stats= player.getData(ModAttachments.PLAYER_STATS.get());
            if (prog == null || stats == null) continue;

            int levels = prog.addExp(amount);
            if (levels > 0) {
                stats.addUnspentPoints(levels);
                stats.markDirty();
                GrimcoreNetworking.syncPlayerStats(player, stats);         // мгновенный HUD (очки/полосы)
            }
            ProgressNetworking.syncPlayerProgress(player, prog);            // мгновенный HUD (XP/уровень)

            totalLevels += levels;
            src.sendSuccess(() -> Component.literal(
                    "Given " + amount + " XP to " + player.getGameProfile().getName() +
                            (levels > 0 ? (" (+" + levels + " level)") : "")), true);
        }

        return Math.max(1, totalLevels);
    }
}

