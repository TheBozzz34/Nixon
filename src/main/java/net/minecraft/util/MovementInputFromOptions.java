package net.minecraft.util;

import net.minecraft.client.settings.GameSettings;
import xyz.necrozma.Client;
import xyz.necrozma.event.impl.Button;
import xyz.necrozma.event.impl.input.MoveButtonEvent;

public class MovementInputFromOptions extends MovementInput
{
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState()
    {
        final MoveButtonEvent event = new MoveButtonEvent(new Button(this.gameSettings.keyBindLeft.isKeyDown(), 90), new Button(this.gameSettings.keyBindRight.isKeyDown(), -90), new Button(this.gameSettings.keyBindBack.isKeyDown(), 180), new Button(this.gameSettings.keyBindForward.isKeyDown(), 0), this.gameSettings.keyBindSneak.isKeyDown(), this.gameSettings.keyBindJump.isKeyDown());
        Client.BUS.post(event);

        if (event.isCancelled()) return;

        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.gameSettings.keyBindForward.isKeyDown())
        {
            ++this.moveForward;
        }

        if (this.gameSettings.keyBindBack.isKeyDown())
        {
            --this.moveForward;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown())
        {
            ++this.moveStrafe;
        }

        if (this.gameSettings.keyBindRight.isKeyDown())
        {
            --this.moveStrafe;
        }

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        if (this.sneak)
        {
            this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
            this.moveForward = (float)((double)this.moveForward * 0.3D);
        }
    }
}
