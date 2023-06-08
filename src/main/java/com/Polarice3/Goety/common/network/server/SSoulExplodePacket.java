package com.Polarice3.Goety.common.network.server;

import com.Polarice3.Goety.client.particles.ShockwaveParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SSoulExplodePacket {
    private final BlockPos blockPos;
    private final int radius;

    public SSoulExplodePacket(BlockPos blockPos, int radius){
        this.blockPos = blockPos;
        this.radius = radius;
    }

    public static void encode(SSoulExplodePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.blockPos);
        buffer.writeInt(packet.radius);
    }

    public static SSoulExplodePacket decode(FriendlyByteBuf buffer) {
        return new SSoulExplodePacket(buffer.readBlockPos(), buffer.readInt());
    }

    public static void consume(SSoulExplodePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientLevel clientWorld = Minecraft.getInstance().level;
            if (clientWorld != null){
                Minecraft.getInstance().particleEngine.add(new ShockwaveParticle.Explosion(clientWorld, packet.blockPos.getX(), packet.blockPos.getY(), packet.blockPos.getZ(), 0, 0, 0, packet.radius, Minecraft.getInstance().particleEngine));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
