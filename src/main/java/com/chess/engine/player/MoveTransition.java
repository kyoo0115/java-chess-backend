package com.chess.engine.player;

import com.chess.engine.board.Board;
import lombok.Getter;

@Getter
public class MoveTransition {

    private final Board transitionBoard;
    private final MoveStatus moveStatus;

    public MoveTransition(final Board transitionBoard, final MoveStatus moveStatus) {
        this.transitionBoard = transitionBoard;
        this.moveStatus = moveStatus;
    }

}
