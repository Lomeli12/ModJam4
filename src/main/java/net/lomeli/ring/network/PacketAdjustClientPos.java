package net.lomeli.ring.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;

import net.lomeli.ring.lib.ModLibs;

public class PacketAdjustClientPos implements IPacket {

    private int x, y;

    public PacketAdjustClientPos() {
    }

    public PacketAdjustClientPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void toByte(ByteBuf buffer) {
        buffer.writeInt(this.x);
        buffer.writeInt(this.y);
    }

    @Override
    public void fromByte(ByteBuf buffer) {
        this.x = buffer.readInt();
        this.y = buffer.readInt();
    }

    @Override
    public void readClient(EntityPlayer player) {
        ModLibs.DISPLAY_X = this.x;
        ModLibs.DISPLAY_Y = this.y;
    }

    @Override
    public void readServer() {
    }

}
