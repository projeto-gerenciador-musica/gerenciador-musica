package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.ArtistaRequestDTO;
import gerenciador_musica_backend.dto.MusicaRequestDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.exception.DadosMusicaInvalidosException;
import gerenciador_musica_backend.exception.MusicaDuplicadaException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Genero;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.GeneroRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Teste de UNIDADE do MusicaService: os 4 repositórios são mockados,
 * então nada disso toca um banco de dados de verdade.
 */
@ExtendWith(MockitoExtension.class)
class MusicaServiceTest {

    @Mock
    private MusicaRepository musicaRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private GeneroRepository generoRepository;

    @InjectMocks
    private MusicaService musicaService;

    private MusicaRequestDTO montarRequestValida() {
        return new MusicaRequestDTO(
                "Bohemian Rhapsody",
                null,
                354,
                (short) 1975,
                new ArtistaRequestDTO("Queen", null, null),
                Set.of(),
                new AlbumRequestDTO("A Night at the Opera", (short) 1975, null),
                Set.of("Rock")
        );
    }

    @Test
    void deveCadastrarMusicaComSucessoCriandoArtistaAlbumEGenero() {
        MusicaRequestDTO request = montarRequestValida();

        Artista artistaSalvo = new Artista("Queen", null, null);
        Album albumSalvo = new Album(artistaSalvo, "A Night at the Opera", (short) 1975, null);
        Genero generoSalvo = new Genero("Rock");

        when(artistaRepository.findByNomeIgnoreCase("Queen")).thenReturn(Optional.empty());
        when(artistaRepository.save(any(Artista.class))).thenReturn(artistaSalvo);

        when(albumRepository.findByTituloIgnoreCaseAndArtistaAndAnoLancamento(
                "A Night at the Opera", artistaSalvo, (short) 1975))
                .thenReturn(Optional.empty());
        when(albumRepository.save(any(Album.class))).thenReturn(albumSalvo);

        when(musicaRepository.existsByAlbumAndTituloIgnoreCase(albumSalvo, "Bohemian Rhapsody"))
                .thenReturn(false);

        when(generoRepository.findByNomeIgnoreCase("Rock")).thenReturn(Optional.empty());
        when(generoRepository.save(any(Genero.class))).thenReturn(generoSalvo);

        when(musicaRepository.save(any(Musica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MusicaResponseDTO response = musicaService.cadastrarMusica(request);

        assertThat(response.titulo()).isEqualTo("Bohemian Rhapsody");
        assertThat(response.duracaoSegundos()).isEqualTo(354);
        assertThat(response.artistaPrincipal().nome()).isEqualTo("Queen");
        assertThat(response.album().titulo()).isEqualTo("A Night at the Opera");
        assertThat(response.generos()).hasSize(1);
    }

    @Test
    void deveLancarExcecaoQuandoArtistaPrincipalNaoInformado() {
        MusicaRequestDTO request = new MusicaRequestDTO(
                "Título", null, 200, (short) 2020,
                null, Set.of(), null, Set.of("Pop")
        );

        assertThatThrownBy(() -> musicaService.cadastrarMusica(request))
                .isInstanceOf(DadosMusicaInvalidosException.class)
                .hasMessage("O artista principal é obrigatório.");

        verify(musicaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoArtistaParticipanteEhIgualAoPrincipal() {
        MusicaRequestDTO request = new MusicaRequestDTO(
                "Título", null, 200, (short) 2020,
                new ArtistaRequestDTO("Queen", null, null),
                Set.of(new ArtistaRequestDTO("queen", null, null)),
                null,
                Set.of("Rock")
        );

        assertThatThrownBy(() -> musicaService.cadastrarMusica(request))
                .isInstanceOf(DadosMusicaInvalidosException.class)
                .hasMessage("O artista principal não pode aparecer como participante.");

        verify(musicaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoMusicaJaEstaCadastradaNoMesmoAlbum() {
        MusicaRequestDTO request = montarRequestValida();

        Artista artistaExistente = new Artista("Queen", null, null);
        Album albumExistente = new Album(artistaExistente, "A Night at the Opera", (short) 1975, null);

        when(artistaRepository.findByNomeIgnoreCase("Queen"))
                .thenReturn(Optional.of(artistaExistente));
        when(albumRepository.findByTituloIgnoreCaseAndArtistaAndAnoLancamento(
                "A Night at the Opera", artistaExistente, (short) 1975))
                .thenReturn(Optional.of(albumExistente));
        when(musicaRepository.existsByAlbumAndTituloIgnoreCase(albumExistente, "Bohemian Rhapsody"))
                .thenReturn(true);

        assertThatThrownBy(() -> musicaService.cadastrarMusica(request))
                .isInstanceOf(MusicaDuplicadaException.class)
                .hasMessage("A música já está cadastrada.");

        verify(musicaRepository, never()).save(any());
    }

    @Test
    void deveListarMusicasCadastradas() {
        Artista artista = new Artista("Queen", null, null);
        Musica musica = new Musica(
                "Bohemian Rhapsody", null, 354, (short) 1975,
                artista, null, Set.of(), Set.of()
        );

        when(musicaRepository.findAll(any(Sort.class))).thenReturn(List.of(musica));

        List<MusicaResponseDTO> resultado = musicaService.listarMusicas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().titulo()).isEqualTo("Bohemian Rhapsody");
    }

    @Test
    void deveBuscarMusicaPorId() {
        Artista artista = new Artista("Queen", null, null);
        Musica musica = new Musica(
                "Bohemian Rhapsody", null, 354, (short) 1975,
                artista, null, Set.of(), Set.of()
        );

        when(musicaRepository.findById(1L)).thenReturn(Optional.of(musica));

        MusicaResponseDTO resultado = musicaService.buscarPorId(1L);

        assertThat(resultado.titulo()).isEqualTo("Bohemian Rhapsody");
    }

    @Test
    void deveLancarExcecaoQuandoMusicaNaoEncontradaPorId() {
        when(musicaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> musicaService.buscarPorId(99L))
                .isInstanceOf(MusicaNaoEncontradaException.class);
    }
}
