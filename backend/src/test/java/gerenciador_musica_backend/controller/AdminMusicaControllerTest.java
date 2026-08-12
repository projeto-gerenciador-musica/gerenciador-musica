package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.exception.MusicaDuplicadaException;
import gerenciador_musica_backend.service.MusicaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Teste de INTEGRAÇÃO da camada web do cadastro administrativo de
 * músicas (POST /api/admin/musicas). O MusicaService é mockado.
 */
@WebMvcTest(AdminMusicaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMusicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MusicaService musicaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CORPO_VALIDO = """
            {
                "titulo": "Bohemian Rhapsody",
                "duracaoSegundos": 354,
                "anoLancamento": 1975,
                "artistaPrincipal": { "nome": "Queen" },
                "artistasParticipantes": [],
                "album": { "titulo": "A Night at the Opera", "anoLancamento": 1975 },
                "generos": ["Rock"]
            }
            """;

    @Test
    void deveCadastrarMusicaComSucesso() throws Exception {
        MusicaResponseDTO resposta = new MusicaResponseDTO(
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

        when(musicaService.cadastrarMusica(any())).thenReturn(resposta);

        mockMvc.perform(post("/api/admin/musicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.titulo").value("Bohemian Rhapsody"));
    }

    @Test
    void deveRetornar400QuandoTituloEstaAusente() throws Exception {
        mockMvc.perform(post("/api/admin/musicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "duracaoSegundos": 354,
                                    "anoLancamento": 1975,
                                    "artistaPrincipal": { "nome": "Queen" },
                                    "artistasParticipantes": [],
                                    "generos": ["Rock"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.titulo").exists());
    }

    @Test
    void deveRetornar409QuandoMusicaJaCadastrada() throws Exception {
        when(musicaService.cadastrarMusica(any()))
                .thenThrow(new MusicaDuplicadaException("A música já está cadastrada."));

        mockMvc.perform(post("/api/admin/musicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A música já está cadastrada."));
    }
}
