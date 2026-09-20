package com.chess.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMoveResponse {
    private String move;
    private String fen;
    private int score;
    private Integer mateIn;
    private boolean isMate;
    private String classification;
}
