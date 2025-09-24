package com.doomspire.grimfate.commands;

import com.doomspire.grimcore.attach.PlayerLoadoutAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class AddSpellCommand {
    private AddSpellCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("grimfate")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("addspell")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("spell_id", StringArgumentType.string())
                                        .executes(ctx -> {
                                            var targets = EntityArgument.getPlayers(ctx, "targets");
                                            var idStr = StringArgumentType.getString(ctx, "spell_id");
                                            var rl = ResourceLocation.tryParse(idStr);
                                            if (rl == null) {
                                                ctx.getSource().sendFailure(Component.literal("Bad id: " + idStr));
                                                return 0;
                                            }

                                            int added = 0;
                                            for (ServerPlayer sp : targets) {
                                                var att = sp.getData(ModAttachments.PLAYER_LOADOUT.get());
                                                if (att == null) continue;
                                                for (int i = 0; i < PlayerLoadoutAttachment.SLOTS; i++) {
                                                    if (att.getSlot(i) == null) {
                                                        att.setSlot(i, rl);
                                                        att.markDirty();
                                                        added++;
                                                        break;
                                                    }
                                                }
                                            }

                                            final int resultCount = added;            // <- делаем effectively final
                                            final String shownId = idStr;             // <- то же, если нужно в сообщении
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Added " + resultCount + " entries of " + shownId),
                                                    true
                                            );
                                            return resultCount;
                                        })
                                )
                        )
                )
        );
    }
}
