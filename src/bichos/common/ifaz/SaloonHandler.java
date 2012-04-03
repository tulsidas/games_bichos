package bichos.common.ifaz;

import org.apache.mina.common.IoSession;

/**
 * Interfaz de los mensajes que recibe el Saloon de los clientes
 * 
 */
public interface SaloonHandler {

    void dig(IoSession session, int i, int j);
}