/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.command;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

@Mixin(value = {EntityPlayerMP.class, TileEntityCommandBlock.class, EntityMinecartCommandBlock.class, MinecraftServer.class, RConConsoleSource.class},
        targets = IMixinCommandSender.SIGN_CLICK_SENDER)
public abstract class MixinCommandSource implements IMixinCommandSource, CommandSource {

    private MessageChannel channel = MessageChannel.TO_ALL;

    @Override
    public String getName() {
        return this.asICommandSender().getCommandSenderName();
    }

    @Override
    public void sendMessage(Text message) {
        checkNotNull(message, "message");
        this.asICommandSender().addChatMessage(SpongeTexts.toComponent(message));
    }

    @Override
    public MessageChannel getMessageChannel() {
        return this.channel;
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        this.channel = checkNotNull(channel, "channel");
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        if (this instanceof User && !((User) this).isOnline()) {
            return Optional.empty();
        }
        return Optional.<CommandSource>of(this);
    }

}
