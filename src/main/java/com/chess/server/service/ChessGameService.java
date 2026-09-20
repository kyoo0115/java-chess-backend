package com.chess.server.service;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.AnalysisResult;
import com.chess.engine.player.ai.StockfishEngine;
import com.chess.engine.util.BoardUtils;
import com.chess.server.dto.AiMoveResponse;
import com.chess.server.dto.GameStateResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChessGameService {

    private final Map<String, Board> activeGames = new ConcurrentHashMap<>();

    public GameStateResponse createNewGame() {
        String gameId = UUID.randomUUID().toString();
        Board board = Board.createStandardBoard();
        activeGames.put(gameId, board);
        return toGameStateResponse(gameId, board, null, "Game started");
    }

    public GameStateResponse loadGameFromFen(String fen) {
        String gameId = UUID.randomUUID().toString();
        Board board = Board.fromFEN(fen);
        activeGames.put(gameId, board);
        return toGameStateResponse(gameId, board, null, "Game loaded from FEN");
    }

    public GameStateResponse getGameState(String gameId) {
        Board board = getBoardOrThrow(gameId);
        return toGameStateResponse(gameId, board, null, "Active game state");
    }

    public GameStateResponse makeMove(String gameId, String fromStr, String toStr) {
        Board board = getBoardOrThrow(gameId);

        int from = parseCoordinate(fromStr);
        int to = parseCoordinate(toStr);

        Move move = Move.MoveFactory.createMove(board, from, to);
        if (move.getCurrentCoordinate() < 0) {
            throw new IllegalArgumentException("Invalid move from " + fromStr + " to " + toStr);
        }

        MoveTransition transition = board.getCurrentPlayer().makeMove(move);
        if (!transition.getMoveStatus().isDone()) {
            throw new IllegalArgumentException("Illegal move: " + transition.getMoveStatus());
        }

        Board newBoard = transition.getTransitionBoard();
        activeGames.put(gameId, newBoard);

        String moveNotation = BoardUtils.getPositionAtCoordinate(from) + BoardUtils.getPositionAtCoordinate(to);
        return toGameStateResponse(gameId, newBoard, moveNotation, "Move applied: " + moveNotation);
    }

    public AiMoveResponse makeAiMove(String gameId, int moveTimeMs) {
        Board board = getBoardOrThrow(gameId);

        try (StockfishEngine sf = new StockfishEngine(moveTimeMs > 0 ? moveTimeMs : StockfishEngine.MOVETIME_MEDIUM)) {
            Move bestMove = sf.execute(board);
            if (bestMove == null || bestMove.getCurrentCoordinate() < 0) {
                throw new IllegalStateException("Stockfish could not compute a move for position.");
            }

            MoveTransition transition = board.getCurrentPlayer().makeMove(bestMove);
            if (!transition.getMoveStatus().isDone()) {
                throw new IllegalStateException("AI move was rejected by engine: " + transition.getMoveStatus());
            }

            Board newBoard = transition.getTransitionBoard();
            activeGames.put(gameId, newBoard);

            AnalysisResult analysis = sf.analysePosition(newBoard.toFEN(), moveTimeMs > 0 ? moveTimeMs : 300);

            String moveNotation = BoardUtils.getPositionAtCoordinate(bestMove.getCurrentCoordinate())
                    + BoardUtils.getPositionAtCoordinate(bestMove.getDestinationCoordinate());

            return AiMoveResponse.builder()
                    .move(moveNotation)
                    .fen(newBoard.toFEN())
                    .score(analysis.scoreWhiteCp())
                    .mateIn(analysis.mateIn())
                    .isMate(analysis.isMate())
                    .classification("AI Move")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Stockfish engine error: " + e.getMessage(), e);
        }
    }

    private Board getBoardOrThrow(String gameId) {
        Board board = activeGames.get(gameId);
        if (board == null) {
            throw new IllegalArgumentException("Game not found with ID: " + gameId);
        }
        return board;
    }

    private int parseCoordinate(String pos) {
        if (pos == null || pos.isBlank()) {
            throw new IllegalArgumentException("Position string cannot be empty");
        }
        pos = pos.trim().toLowerCase();
        if (BoardUtils.POSITION_TO_COORDINATE.containsKey(pos)) {
            return BoardUtils.getCoordinateAtPosition(pos);
        }
        try {
            int coord = Integer.parseInt(pos);
            if (BoardUtils.isValidTileCoordinate(coord)) {
                return coord;
            }
        } catch (NumberFormatException ignored) {
        }
        throw new IllegalArgumentException("Unrecognized square coordinate: " + pos);
    }

    private GameStateResponse toGameStateResponse(String gameId, Board board, String lastMove, String statusMessage) {
        List<String> legalMoves = new ArrayList<>();
        for (Move m : board.getCurrentPlayer().getLegalMoves()) {
            legalMoves.add(BoardUtils.getPositionAtCoordinate(m.getCurrentCoordinate())
                    + BoardUtils.getPositionAtCoordinate(m.getDestinationCoordinate()));
        }

        return GameStateResponse.builder()
                .gameId(gameId)
                .fen(board.toFEN())
                .activePlayer(board.getCurrentPlayer().getAlliance().name())
                .isCheck(board.getCurrentPlayer().isInCheck())
                .isCheckMate(board.getCurrentPlayer().isCheckMate())
                .isStaleMate(board.getCurrentPlayer().isStaleMate())
                .legalMoves(legalMoves)
                .lastMove(lastMove)
                .statusMessage(statusMessage)
                .build();
    }
}
