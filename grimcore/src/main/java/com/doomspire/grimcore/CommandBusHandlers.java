package com.doomspire.grimcore;

import com.doomspire.grimcore.commands.GrimfateCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Grimcore.MODID)
public final class CommandBusHandlers {
    private CommandBusHandlers(){}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        GrimfateCommands.register(e.getDispatcher());
    }
}
