package net.blancworks.figura.mixin;

import net.blancworks.figura.FiguraMod;
import net.blancworks.figura.PlayerData;
import net.blancworks.figura.PlayerDataManager;
import net.blancworks.figura.gui.ActionWheel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    private ActionWheel actionWheel;

    @Inject(at = @At ("RETURN"), method = "<init>")
    public void init(MinecraftClient client, CallbackInfo ci) {
        actionWheel = new ActionWheel(client);
    }

    @Inject(at = @At ("RETURN"), method = "render")
    public void render(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (FiguraMod.actionWheel.isPressed()) {
            if (ActionWheel.enabled)
                actionWheel.render(matrices);
        }
        else {
            ActionWheel.enabled = true;
        }
    }

    @Inject(at = @At ("HEAD"), method = "renderCrosshair", cancellable = true)
    private void renderCrosshair(MatrixStack matrices, CallbackInfo ci) {
        if (FiguraMod.actionWheel.isPressed() && ActionWheel.enabled)
            ci.cancel();

        //do not render crosshair
        PlayerData currentData = PlayerDataManager.localPlayer;
        if (currentData != null && currentData.script != null && !currentData.script.crossHairEnabled)
            ci.cancel();
    }

    @ModifyArgs(
            method = "renderCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
                    ordinal = 0
            )
    )
    private void renderCrosshairDrawTexture(Args args) {
        //set crosshair offset
        PlayerData currentData = PlayerDataManager.localPlayer;
        if (currentData != null && currentData.script != null && currentData.script.crossHairPos != null) {
            args.set(1, (int) ((int) args.get(1) + currentData.script.crossHairPos.x));
            args.set(2, (int) ((int) args.get(2) + currentData.script.crossHairPos.y));
        }
    }
}
