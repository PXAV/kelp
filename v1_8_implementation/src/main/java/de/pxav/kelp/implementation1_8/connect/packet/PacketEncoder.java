package de.pxav.kelp.implementation1_8.connect.packet;

import com.google.common.base.Preconditions;
import de.pxav.kelp.core.connect.KelpBuffer;
import de.pxav.kelp.core.connect.packet.IPacketEncoder;
import de.pxav.kelp.core.connect.packet.Packet;
import de.pxav.kelp.core.connect.packet.PacketRegistry;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author Etrayed
 */
public class PacketEncoder extends MessageToMessageEncoder<Packet> implements IPacketEncoder {

  private final PacketRegistry registry;

  public PacketEncoder(PacketRegistry registry) {
    this.registry = registry;
  }

  @Override
  protected void encode(ChannelHandlerContext context, Packet packet, List<Object> list) throws Exception {
    Preconditions.checkArgument(registry.isRegistered(packet.getClass()), "could not encode unregistered packet: "
      + packet.getClass().getCanonicalName());

    KelpBuffer buffer = new KelpBuffer(Unpooled.buffer());
    int packetId = registry.getId(packet.getClass());

    buffer.writeVarInt(packetId);

    packet.store(buffer);

    list.add(buffer);
  }
}