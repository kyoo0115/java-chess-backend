package com.chess.server;

import com.chess.server.dto.GameStateResponse;
import com.chess.server.dto.MoveRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ChessServerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateGameAndMakeMove() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/games/new"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").isNotEmpty())
                .andExpect(jsonPath("$.activePlayer").value("WHITE"))
                .andExpect(jsonPath("$.fen").isNotEmpty())
                .andReturn();

        GameStateResponse created = objectMapper.readValue(result.getResponse().getContentAsString(), GameStateResponse.class);
        String gameId = created.getGameId();
        assertNotNull(gameId);

        // Move e2 -> e4
        MoveRequest moveRequest = new MoveRequest("e2", "e4");
        mockMvc.perform(post("/api/games/" + gameId + "/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activePlayer").value("BLACK"))
                .andExpect(jsonPath("$.lastMove").value("e2e4"));

        // Verify state retrieval
        mockMvc.perform(get("/api/games/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activePlayer").value("BLACK"));
    }
}
