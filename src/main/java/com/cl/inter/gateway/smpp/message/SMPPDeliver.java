package com.cl.inter.gateway.smpp.message;

import io.netty.buffer.ByteBuf;

public class SMPPDeliver extends SMPPBody implements Cloneable {













    @Override
    public int encode(ByteBuf buffer) {
        return 0;
    }

    @Override
    public SMPPBody decode(ByteBuf buffer) {
        return null;
    }
}
