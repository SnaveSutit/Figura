package net.blancworks.figura.mixin;

import com.mojang.authlib.GameProfile;
import net.blancworks.figura.FiguraMod;
import net.blancworks.figura.PlayerData;
import net.blancworks.figura.PlayerDataManager;
import net.blancworks.figura.config.ConfigManager.Config;
import net.blancworks.figura.lua.api.nameplate.NamePlateAPI;
import net.blancworks.figura.lua.api.nameplate.NamePlateCustomization;
import net.blancworks.figura.trust.PlayerTrustManager;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.UUID;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(at = @At("RETURN"), method = "getPlayerName", cancellable = true)
    private void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        Text text = cir.getReturnValue();

        if ((boolean) Config.PLAYERLIST_MODIFICATIONS.value) {
            UUID uuid = entry.getProfile().getId();
            String playerName = entry.getProfile().getName();

            PlayerData currentData = PlayerDataManager.getDataForPlayer(uuid);
            if (currentData != null && !playerName.equals("")) {
                NamePlateCustomization nameplateData = currentData.script == null ? null : currentData.script.nameplateCustomizations.get(NamePlateAPI.TABLIST);

                try {
                    if (text instanceof TranslatableText) {
                        Object[] args = ((TranslatableText) text).getArgs();

                        for (Object arg : args) {
                            if (arg instanceof TranslatableText || !(arg instanceof Text))
                                continue;

                            if (NamePlateAPI.applyFormattingRecursive((LiteralText) arg, uuid, playerName, nameplateData, currentData))
                                break;
                        }
                    } else if (text instanceof LiteralText) {
                        NamePlateAPI.applyFormattingRecursive((LiteralText) text, uuid, playerName, nameplateData, currentData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        cir.setReturnValue(text);
    }

    @Unique private PlayerEntity playerEntity;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawableHelper;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIFFIIII)V", shift = At.Shift.BEFORE), method = "render", locals = LocalCapture.CAPTURE_FAILHARD)
    private void render(MatrixStack matrices, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci, ClientPlayNetworkHandler clientPlayNetworkHandler, List list, int i, int j, int l, int m, int n, boolean bl, int q, int r, int s, int t, int u, List list2, List list3, int w, int x, int y, int z, int aa, int ab, PlayerListEntry playerListEntry2, GameProfile gameProfile, PlayerEntity playerEntity, boolean bl2, int ae, int af) {
        this.playerEntity = playerEntity;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawableHelper;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIFFIIII)V"), method = "render")
    private void render(MatrixStack matrices, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        if (playerEntity != null && PlayerDataManager.hasPlayerData(playerEntity.getUuid()) && (boolean) Config.PLAYERLIST_MODIFICATIONS.value) {
            PlayerData data = PlayerDataManager.getDataForPlayer(playerEntity.getUuid());
            if ((data != null && data.model != null && data.getTrustContainer().getBoolSetting(PlayerTrustManager.ALLOW_VANILLA_MOD_ID))) {
                FiguraMod.currentData = data;
                FiguraMod.currentPlayer = (AbstractClientPlayerEntity) playerEntity;

                MatrixStack stack = new MatrixStack();

                stack.push();

                stack.translate(x + 4, y + 8, 0);
                stack.scale(-16,16,16);
                stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180));

                DiffuseLighting.disableGuiDepthLighting();

                data.model.renderSkull(data, stack, FiguraMod.tryGetImmediate(), 15728864);
                DiffuseLighting.enableGuiDepthLighting();

                stack.pop();
                return;
            }
        }

        DrawableHelper.drawTexture(matrices, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }
}
