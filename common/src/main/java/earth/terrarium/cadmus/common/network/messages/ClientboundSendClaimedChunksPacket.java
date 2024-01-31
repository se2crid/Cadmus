package earth.terrarium.cadmus.common.network.messages;

import com.mojang.datafixers.util.Pair;
import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.claims.ClaimScreen;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.Optional;

public record ClientboundSendClaimedChunksPacket(Map<ChunkPos, Pair<String, ClaimType>> claims,
                                                 String id, ChatFormatting color, Optional<String> displayName,
                                                 Map<String, Component> teamDisplayNames, int claimedCount,
                                                 int chunkLoadedCount,
                                                 int maxClaims,
                                                 int maxChunkLoaded,
                                                 int viewDistance) implements Packet<ClientboundSendClaimedChunksPacket> {

    public static final ClientboundPacketType<ClientboundSendClaimedChunksPacket> TYPE = new Type();

    @Override
    public PacketType<ClientboundSendClaimedChunksPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundSendClaimedChunksPacket> implements ClientboundPacketType<ClientboundSendClaimedChunksPacket> {
        private static final MapCodec<ChunkPos, Pair<String, ClaimType>> CHUNK_POS_CLAIM_CODEC = new MapCodec<>(
            ExtraByteCodecs.CHUNK_POS,
            ObjectByteCodec.create(
                ByteCodec.STRING.fieldOf(Pair::getFirst),
                ClaimType.CODEC.fieldOf(Pair::getSecond),
                Pair::new
            )
        );

        public Type() {
            super(
                ClientboundSendClaimedChunksPacket.class,
                new ResourceLocation(Cadmus.MOD_ID, "send_claimed_chunks"),
                ObjectByteCodec.create(
                    CHUNK_POS_CLAIM_CODEC.fieldOf(ClientboundSendClaimedChunksPacket::claims),
                    ByteCodec.STRING.fieldOf(ClientboundSendClaimedChunksPacket::id),
                    ByteCodec.ofEnum(ChatFormatting.class).fieldOf(ClientboundSendClaimedChunksPacket::color),
                    ByteCodec.STRING.optionalFieldOf(ClientboundSendClaimedChunksPacket::displayName),
                    new MapCodec<>(ByteCodec.STRING, ExtraByteCodecs.COMPONENT).fieldOf(ClientboundSendClaimedChunksPacket::teamDisplayNames),
                    ByteCodec.VAR_INT.fieldOf(ClientboundSendClaimedChunksPacket::claimedCount),
                    ByteCodec.VAR_INT.fieldOf(ClientboundSendClaimedChunksPacket::chunkLoadedCount),
                    ByteCodec.VAR_INT.fieldOf(ClientboundSendClaimedChunksPacket::maxClaims),
                    ByteCodec.VAR_INT.fieldOf(ClientboundSendClaimedChunksPacket::maxChunkLoaded),
                    ByteCodec.VAR_INT.fieldOf(ClientboundSendClaimedChunksPacket::viewDistance),
                    ClientboundSendClaimedChunksPacket::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundSendClaimedChunksPacket packet) {
            return () -> ClaimScreen.createFromPacket(packet);
        }
    }
}
