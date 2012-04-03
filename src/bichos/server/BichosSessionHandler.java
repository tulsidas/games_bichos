package bichos.server;

import server.ServerSessionHandler;

import bichos.common.messages.BichosProtocolDecoder;

import com.google.common.collect.Lists;

public class BichosSessionHandler extends ServerSessionHandler {

    public BichosSessionHandler() {
        super(new BichosProtocolDecoder());

        salones = Lists.newArrayList();
        salones.add(new BichosSaloon(0, this));
        salones.add(new BichosSaloon(1, this));
        salones.add(new BichosSaloon(2, this));
    }

    @Override
    protected int getCodigoJuego() {
        // bichos = 3 para la base
        return 3;
    }
}
