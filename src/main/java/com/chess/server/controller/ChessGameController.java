package com.chess.server.controller;

import com.chess.server.dto.AiMoveResponse;
import com.chess.server.dto.GameStateResponse;
import com.chess.server.dto.MoveRequest;
import com.chess.server.service.ChessGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChessGameController {

    private final ChessGameService gameService;

    @PostMapping("/new")
    public ResponseEntity<GameStateResponse> createGame() {
        return ResponseEntity.ok(gameService.createNewGame());
    }

    @PostMapping("/load-fen")
    public ResponseEntity<GameStateResponse> loadFen(@RequestParam String fen) {
        return ResponseEntity.ok(gameService.loadGameFromFen(fen));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable String gameId) {
        return ResponseEntity.ok(gameService.getGameState(gameId));
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameStateResponse> makeMove(@PathVariable String gameId,
                                                      @RequestBody MoveRequest moveRequest) {
        return ResponseEntity.ok(gameService.makeMove(gameId, moveRequest.getFrom(), moveRequest.getTo()));
    }

    @PostMapping("/{gameId}/ai-move")
    public ResponseEntity<AiMoveResponse> makeAiMove(@PathVariable String gameId,
                                                     @RequestParam(defaultValue = "500") int moveTimeMs) {
        return ResponseEntity.ok(gameService.makeAiMove(gameId, moveTimeMs));
    }
}
