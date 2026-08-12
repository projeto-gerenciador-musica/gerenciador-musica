package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.service.MusicaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Teste de INTEGRAÇÃO da camada web do catálogo público de músicas
 * (GET /api/musicas). O MusicaService é mockado; o foco aqui é o
 * contrato HTTP (status, JSON, erros), não a regra de negócio.
 */
@WebMvcTest(MusicaController.class)
@AutoConfigureMockMvc(addFilters = false)
class MusicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MusicaService musicaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MusicaResponseDTO montarMusicaResposta() {
        return new MusicaResponseDTO(
                1L,
                "Bohemian Rhapsody",
                null,
                354,
                (short) 1975,
                new ArtistaResumoDTO(1L, "Queen", null, null),
                null,
                Set.of(),
                Set.of()
        );
    }

    @Test
    void deveListarMusicasCadastradas() throws Exception {
        when(musicaService.listarMusicas()).thenReturn(List.of(montarMusicaResposta()));

        mockMvc.perform(get("/api/musicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Bohemian Rhapsody"))
                .andExpect(jsonPath("$[0].artistaPrincipal.nome").value("Queen"));
    }

    @Test
    void deveBuscarMusicaPorId() throws Exception {
        when(musicaService.buscarPorId(1L)).thenReturn(montarMusicaResposta());

        mockMvc.perform(get("/api/musicas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Bohemian Rhapsody"));
    }

    @Test
    void deveRetornar404QuandoMusicaNaoEncontrada() throws Exception {
        when(musicaService.buscarPorId(99L))
                .thenThrow(new MusicaNaoEncontradaException(99L));

        mockMvc.perform(get("/api/musicas/99"))
                .andExpect(status().isNotFound());
    }
}
