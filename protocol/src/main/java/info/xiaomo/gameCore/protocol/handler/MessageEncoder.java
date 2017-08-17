package info.xiaomo.gameCore.protocol.handler;

import com.google.protobuf.MessageLite;
import info.xiaomo.gameCore.protocol.MessagePool;
import info.xiaomo.gameCore.protocol.entity.BaseMsg.MsgPack;
import info.xiaomo.gameCore.protocol.entity.BaseMsg.MsgPack.Builder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageEncoder extends MessageToByteEncoder<MessageLite> {
    private int lengthFieldLength;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageEncoder.class);

    private MessagePool msgPool;

    public MessageEncoder(MessagePool msgPool) {
        this.msgPool = msgPool;
        this.lengthFieldLength = 4;
    }

    /**
     * 编码
     *
     * @param ctx ctx
     * @param msg msg
     * @param out out
     * @throws Exception Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageLite msg, ByteBuf out) throws Exception {
        byte[] bytes = null;
        if (msg == null) {
            return;
        }
        try {
            int messageId = this.msgPool.getMessageId(msg.getClass());
            Builder builder = MsgPack.newBuilder();
            builder.setMsgID(messageId);
            builder.setBody(msg.toByteString());

            MsgPack msgPack = builder.build();
            bytes = msgPack.toByteArray();
        } catch (Exception e) {
            LOGGER.error("编码错误错误", e);
        }
        if ((bytes == null) || (bytes.length == 0)) {
            return;
        }
        boolean writable = out.isWritable(this.lengthFieldLength + bytes.length);
        if (!writable) {
            LOGGER.error("消息过大, 编码失败【{}】", bytes.length);
            return;
        }
        out.writeShort(bytes.length + this.lengthFieldLength).writeBytes(bytes);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}