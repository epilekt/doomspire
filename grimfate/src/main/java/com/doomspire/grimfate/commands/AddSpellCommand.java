package com.doomspire.grimfate.commands;

import com.doomspire.grimcore.attach.PlayerLoadoutAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Collection;

public final class AddSpellCommand {

    private AddSpellCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("grimfate")
                .then(Commands.literal("addspell")
                        // /grimfate addspell <spell>  — для себя
                        .then(Commands.argument("spell", ResourceLocationArgument.id())
                                .executes(ctx -> {
                                    ServerPlayer self = ctx.getSource().getPlayerOrException();
                                    ResourceLocation rl = ResourceLocationArgument.getId(ctx, "spell");
                                    int added = addToPlayer(self, rl);
                                    if (added > 0) {
                                        ctx.getSource().sendSuccess(() ->
                                                Component.literal("Added spell " + rl + " to " + self.getGameProfile().getName()), true);
                                        return 1;
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("No free slot for " + rl + " on " + self.getGameProfile().getName()));
                                        return 0;
                                    }
                                }))
                        // /grimfate addspell <targets> <spell>
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("spell", ResourceLocationArgument.id())
                                        .executes(ctx -> {
                                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
                                            ResourceLocation rl = ResourceLocationArgument.getId(ctx, "spell");
                                            int total = 0;
                                            for (ServerPlayer sp : targets) {
                                                total += addToPlayer(sp, rl);
                                            }
                                            if (total > 0) {
                                                final int count = total; // effectively final копия для лямбды
                                                ctx.getSource().sendSuccess(() ->
                                                        Component.literal("Added spell " + rl + " to " + count + " player(s)"), true);
                                                return total;
                                            } else {
                                                ctx.getSource().sendFailure(Component.literal("No recipients had a free slot for " + rl));
                                                return 0;
                                            }
                                        })))
                )
        );
    }

    /**
     * Пытается добавить спелл в первый свободный слот лоадаута игрока.
     * Возвращает 1 если добавлено, иначе 0.
     */
    private static int addToPlayer(ServerPlayer sp, ResourceLocation rl) throws CommandSyntaxException {
        PlayerLoadoutAttachment att = sp.getData(ModAttachments.PLAYER_LOADOUT.get());
        if (att == null) {
            return 0;
        }
        for (int i = 0; i < PlayerLoadoutAttachment.SLOTS; i++) {
            if (att.get(i) == null) {
                att.set(i, rl);
                // Триггерим авто-синхронизацию (ModAttachments.PLAYER_LOADOUT должен быть зарегистрирован с .sync(...))
                sp.setData(ModAttachments.PLAYER_LOADOUT.get(), att);
                return 1;
            }
        }
        return 0;
    }
}
