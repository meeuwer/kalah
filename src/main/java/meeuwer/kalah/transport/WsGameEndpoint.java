package meeuwer.kalah.transport;

import meeuwer.kalah.logic.GameClient;
import meeuwer.kalah.logic.GameService;
import meeuwer.kalah.transport.impl.WsGameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/game")
public class WsGameEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(WsGameEndpoint.class);

    @Inject
    private GameService gameService;

    private Map<String, GameClient> clients = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        LOG.debug("{} Opened WS session", session.getId());
        clients.put(session.getId(), new WsGameClient(session));
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String sessionId = session.getId();

        LOG.debug("{} Received message: {}", sessionId, message);

        StringTokenizer t = new StringTokenizer(message, " ");
        String type;
        try {
            type = t.nextToken();
        } catch (NoSuchElementException e) {
            LOG.error("{} Event type not found in the message", sessionId);
            return;
        }

        Command command;
        try {
            command = Command.valueOf(type);
        } catch (IllegalArgumentException e) {
            LOG.error("{} Unknown command: {}", sessionId, type);
            return;
        }

        switch (command) {
        case JOIN:
            gameService.joinGame(clients.get(session.getId()));
            break;
        case MOVE:
            try {
                int pitIdx = Integer.parseInt(t.nextToken());
                clients.get(session.getId()).move(pitIdx);
            } catch (NoSuchElementException e) {
                LOG.error("{} Pit index not found in MOVE command message", sessionId);
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOG.debug("{} Closed WS session", session.getId());
        GameClient client = clients.remove(session.getId());
        client.part();
    }

}
