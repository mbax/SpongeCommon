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
package org.spongepowered.common.mixin.core.entity.player;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.entity.CombatHelper.getNewTracker;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.entity.player.PlayerKickHelper;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.IMixinPacketResourcePackSend;
import org.spongepowered.common.interfaces.IMixinServerScoreboard;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.IMixinTeam;
import org.spongepowered.common.interfaces.text.IMixinTitle;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.chat.SpongeChatType;
import org.spongepowered.common.util.LanguageUtil;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends MixinEntityPlayer implements Player, IMixinSubject, IMixinEntityPlayerMP, IMixinCommandSender,
        IMixinCommandSource {

    public int newExperience = 0;
    public int newLevel = 0;
    public int newTotalExperience = 0;
    public boolean keepsLevel = false;
    private boolean sleepingIgnored;

    private final User user = SpongeImpl.getGame().getServiceManager().provideUnchecked(UserStorageService.class).getOrCreate((GameProfile) getGameProfile());

    @Shadow private String translator;
    @Shadow public NetHandlerPlayServer playerNetServerHandler;
    @Shadow public ItemInWorldManager theItemInWorldManager;
    @Shadow public int lastExperience;
    @Shadow public MinecraftServer mcServer;
    @Shadow public abstract void setSpectatingEntity(Entity entityToSpectate);
    @Shadow public abstract void sendPlayerAbilities();

    private Scoreboard spongeScoreboard = Sponge.getGame().getServer().getServerScoreboard().get();

    @Nullable private Vector3d velocityOverride = null;

    @Inject(method = "removeEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void onRemoveEntity(Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof EntityHuman) {
            ((EntityHuman) entityIn).onRemovedFrom((EntityPlayerMP) (Object) this);
        }
    }

    @SuppressWarnings("rawtypes")
    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getObjectivesFromCriteria(Lnet/minecraft/scoreboard/IScoreObjectiveCriteria;)Ljava/util/Collection;"))
    public Collection onGetObjectivesFromCriteria(net.minecraft.scoreboard.Scoreboard this$0, IScoreObjectiveCriteria criteria) {
        return this.getWorldScoreboard().getObjectivesFromCriteria(criteria);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "onDeath", at = @At(value = "RETURN"))
    public void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        IMixinWorld world = (IMixinWorld) this.worldObj;
        // Special case for players as sometimes tick capturing won't capture deaths
        if (world.getCapturedEntityItems().size() > 0) {
            world.handleDroppedItems(Cause.of(NamedCause.source(this), NamedCause.of("Attacker", damageSource)),
                (List<org.spongepowered.api.entity.Entity>) (List<?>) world.getCapturedEntityItems(), null, true);
        }
    }

    @Override
    public GameProfile getProfile() {
        return this.user.getProfile();
    }

    @Override
    public boolean isOnline() {
        return this.user.isOnline();
    }

    @Override
    public Optional<Player> getPlayer() {
        return this.user.getPlayer();
    }

    @Override
    public User getUserObject() {
        return this.user;
    }

    @Override
    public Locale getLocale() {
        return LanguageUtil.LOCALE_CACHE.getUnchecked(this.translator);
    }

    @Override
    public void sendMessage(ChatType type, Text message) {
        IChatComponent component = SpongeTexts.toComponent(message);
        if (type == ChatTypes.ACTION_BAR) {
            component = SpongeTexts.fixActionBarFormatting(component);
        }

        this.playerNetServerHandler.sendPacket(new S02PacketChat(component, ((SpongeChatType) type).getByteId()));
    }

    @Override
    public void sendMessages(ChatType type, Text... messages) {
        for (Text text : messages) {
            sendMessage(type, text);
        }
    }

    @Override
    public void sendMessages(ChatType type, Iterable<Text> messages) {
        for (Text text : messages) {
            sendMessages(type, text);
        }
    }

    @Override
    public void sendTitle(Title title) {
        ((IMixinTitle) (Object) title).send((EntityPlayerMP) (Object) this);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        List<Packet> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            if (position.sub(this.posX, this.posY, this.posZ).lengthSquared() < (long) radius * (long) radius) {
                for (Packet packet : packets) {
                    this.playerNetServerHandler.sendPacket(packet);
                }
            }
        }
    }

    @Override
    public PlayerConnection getConnection() {
        return (PlayerConnection) this.playerNetServerHandler;
    }

    // this needs to be overridden from EntityPlayer so we can force a resend of the experience level
    @Override
    public void setLevel(int level) {
        super.experienceLevel = level;
        this.lastExperience = -1;
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return ((IMixinSubject) this.user).getSubjectCollectionIdentifier();
    }

    @Override
    public String getIdentifier() {
        return this.user.getIdentifier();
    }

    @Override
    public Tristate permDefault(String permission) {
        return ((IMixinSubject) this.user).permDefault(permission);
    }

    @Override
    public void reset() {
        float experience = 0;
        boolean keepInventory = this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory");

        if (this.keepsLevel || keepInventory) {
            experience = this.experience;
            this.newTotalExperience = this.experienceTotal;
            this.newLevel = this.experienceLevel;
        }

        this.clearActivePotions();
        this._combatTracker = getNewTracker(this);
        this.deathTime = 0;
        this.experience = 0;
        this.experienceLevel = this.newLevel;
        this.experienceTotal = this.newTotalExperience;
        this.fire = 0;
        this.fallDistance = 0;
        this.foodStats = new FoodStats();
        this.potionsNeedUpdate = true;
        this.openContainer = this.inventoryContainer;
        this.attackingPlayer = null;
        this.entityLivingToAttack = null;
        this.lastExperience = -1;
        this.setHealth(this.getMaxHealth());

        if (this.keepsLevel || keepInventory) {
            this.experience = experience;
        } else {
            this.addExperience(this.newExperience);
        }

        this.keepsLevel = false;
    }

    @Override
    public boolean isViewingInventory() {
        return this.openContainer != null;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {
        if (scoreboard == null) {
            scoreboard = Sponge.getGame().getServer().getServerScoreboard().get();
        }
        ((IMixinServerScoreboard) this.spongeScoreboard).removePlayer((EntityPlayerMP) (Object) this);
        this.spongeScoreboard = scoreboard;
        ((IMixinServerScoreboard) this.spongeScoreboard).addPlayer((EntityPlayerMP) (Object) this);
    }

    @Override
    public void initScoreboard() {
        ((IMixinServerScoreboard) this.spongeScoreboard).addPlayer((EntityPlayerMP) (Object) this);
    }

    @Override
    public Text getTeamRepresentation() {
        return Text.of(this.getName());
    }

    @Override
    public MessageChannel getDeathMessageChannel() {
        EntityPlayerMP player = (EntityPlayerMP) (Object) this;
        if (player.worldObj.getGameRules().getGameRuleBooleanValue("showDeathMessages")) {
            @Nullable Team team = player.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                    return ((IMixinTeam) team).getTeamChannel(player);
                } else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                    return ((IMixinTeam) team).getNonTeamChannel();
                }
            } else {
                return this.getMessageChannel();
            }
        }

        return MessageChannel.TO_NONE;
    }

    @Override
    public net.minecraft.scoreboard.Scoreboard getWorldScoreboard() {
        return (net.minecraft.scoreboard.Scoreboard) this.spongeScoreboard;
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.spongeScoreboard;
    }

    @Override
    public void kick() {
        kick(Text.of(SpongeImpl.getGame().getRegistry().getTranslationById("disconnect.disconnected").get()));
    }

    @Override
    public void kick(Text message) {
        final IChatComponent component = SpongeTexts.toComponent(message);
        PlayerKickHelper.kickPlayer((EntityPlayerMP) (Object) this, component);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume) {
        this.playSound(sound, position, volume, 1);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch) {
        this.playSound(sound, position, volume, pitch, 0);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch, double minVolume) {
        this.playerNetServerHandler.sendPacket(new S29PacketSoundEffect(sound.getId(), position.getX(), position.getY(), position.getZ(),
                (float) Math.max(minVolume, volume), (float) pitch));
    }

    @Override
    public void sendResourcePack(ResourcePack pack) {
        S48PacketResourcePackSend packet = new S48PacketResourcePackSend();
        ((IMixinPacketResourcePackSend) packet).setResourcePack(pack);
        this.playerNetServerHandler.sendPacket(packet);
    }

    @Override
    public CommandSource asCommandSource() {
        return this;
    }

    @Override
    public ICommandSender asICommandSender() {
        return (ICommandSender) this;
    }

    @Override
    public boolean isSleepingIgnored() {
        return this.sleepingIgnored;
    }

    @Override
    public void setSleepingIgnored(boolean sleepingIgnored) {
        this.sleepingIgnored = sleepingIgnored;
    }

    @Override
    public Vector3d getVelocity() {
        if (this.velocityOverride != null) {
            return this.velocityOverride;
        }
        return super.getVelocity();
    }

    @Override
    public void setImplVelocity(Vector3d velocity) {
        super.setImplVelocity(velocity);
        this.velocityOverride = null;
    }

    @Override
    public void setVelocityOverride(@Nullable Vector3d velocity) {
        this.velocityOverride = velocity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return (CarriedInventory<? extends Carrier>) this.inventory;
    }

    /**
     * We overwrite this method to call the ChangeGameModeEvent.TargetPlayer event.
     *
     * <p>This does not use another injection annotation because we set the {@code gameType}
     * variable, and to do so using other annotations would result in a mess.</p>
     *
     * @author kashike - 30/12/2015
     */
    @Overwrite
    public void setGameType(WorldSettings.GameType gameType) {
        // Sponge start
        ChangeGameModeEvent.TargetPlayer event = SpongeEventFactory.createChangeGameModeEventTargetPlayer(Cause.of(NamedCause.source(this)),
                (GameMode) (Object) this.theItemInWorldManager.getGameType(), (GameMode) (Object) gameType, this);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            return;
        }

        gameType = (WorldSettings.GameType) (Object) event.getGameMode();
        // Sponge end
        this.theItemInWorldManager.setGameType(gameType);
        this.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(3, (float) gameType.getID()));

        if (gameType == WorldSettings.GameType.SPECTATOR)
        {
            this.mountEntity((Entity)null);
        }
        else
        {
            this.setSpectatingEntity((Entity) (Object) this);
        }

        this.sendPlayerAbilities();
        this.markPotionsDirty();
    }
}
