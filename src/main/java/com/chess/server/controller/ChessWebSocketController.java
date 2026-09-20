package com.chess.server.controller;

import com.chess.server.dto.GameStateResponse;
import com.chess.server.dto.MoveRequest;
import com.chess.server.service.ChessGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChessWebSocketController {

    private final ChessGameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game/{gameId}/move")
    public void handleMove(@DestinationVariable String gameId, @Payload MoveRequest moveRequest) {
        GameStateResponse response = gameService.makeMove(gameId, moveRequest.getFrom(), moveRequest.getTo());
        messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
    }
}
