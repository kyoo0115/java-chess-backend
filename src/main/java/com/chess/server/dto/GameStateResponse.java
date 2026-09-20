package com.chess.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateResponse {
    private String gameId;
    private String fen;
    private String activePlayer;
    private boolean isCheck;
    private boolean isCheckMate;
    private boolean isStaleMate;
    private List<String> legalMoves;
    private String lastMove;
    private String statusMessage;
}
