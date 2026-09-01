package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.PaginaResponseDTO;
import gerenciador_musica_backend.dto.ReviewAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.ReviewRequestDTO;
import gerenciador_musica_backend.dto.ReviewResponseDTO;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosReviewInvalidosException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.exception.ReviewAcessoNegadoException;
import gerenciador_musica_backend.exception.ReviewJaExisteException;
import gerenciador_musica_backend.exception.ReviewNaoEncontradaException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Review;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.TipoAlvoReview;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.ReviewRepository;
import gerenciador_musica_backend.repository.SeguidorUsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Teste de UNIDADE do ReviewService. Assim como o PlaylistService, ele
 * descobre o usuário logado pelo SecurityContextHolder, então
 * simulamos a autenticação "de verdade" antes de cada teste.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MusicaRepository musicaRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private SeguidorUsuarioRepository seguidorUsuarioRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Usuario usuarioLogado;
    private Musica musica;
    private Album album;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(usuarioLogado, "id", 1L);
        autenticarComo(usuarioLogado);

        Artista artista = new Artista("Queen", "Queen", "Banda", null);
        ReflectionTestUtils.setField(artista, "idArtista", 5L);

        album = new Album(artista, "A Night at the Opera", (short) 1975, "capa.jpg");
        album.setIdAlbum(10L);

        musica = new Musica(
                "Bohemian Rhapsody",
                null,
                354,
                (short) 1975,
                artista,
                album
        );
        musica.setIdMusica(20L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(usuario, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void deveCriarReviewDeMusica() {
        when(musicaRepository.findById(20L)).thenReturn(Optional.of(musica));
        when(reviewRepository.existsByUsuario_IdAndMusica_IdMusica(1L, 20L))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> {
                    Review review = invocation.getArgument(0);
                    ReflectionTestUtils.setField(review, "idReview", 100L);
                    return review;
                });

        ReviewResponseDTO resposta = reviewService.criarReview(
                new ReviewRequestDTO(20L, null, BigDecimal.valueOf(5), "  Obra-prima!  ")
        );

        assertThat(resposta.idReview()).isEqualTo(100L);
        assertThat(resposta.nota()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(resposta.texto()).isEqualTo("Obra-prima!");
        assertThat(resposta.minhaReview()).isTrue();
        assertThat(resposta.alvo().tipo()).isEqualTo(TipoAlvoReview.MUSICA);
        assertThat(resposta.alvo().titulo()).isEqualTo("Bohemian Rhapsody");
        assertThat(resposta.alvo().artista()).isEqualTo("Queen");
        assertThat(resposta.alvo().capaUrl()).isEqualTo("capa.jpg");
        assertThat(resposta.autor().nome()).isEqualTo("Maria");
    }

    @Test
    void deveCriarReviewDeAlbum() {
        when(albumRepository.findById(10L)).thenReturn(Optional.of(album));
        when(reviewRepository.existsByUsuario_IdAndAlbum_IdAlbum(1L, 10L))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> {
                    Review review = invocation.getArgument(0);
                    ReflectionTestUtils.setField(review, "idReview", 101L);
                    return review;
                });

        ReviewResponseDTO resposta = reviewService.criarReview(
                new ReviewRequestDTO(null, 10L, BigDecimal.valueOf(4), null)
        );

        assertThat(resposta.alvo().tipo()).isEqualTo(TipoAlvoReview.ALBUM);
        assertThat(resposta.alvo().titulo()).isEqualTo("A Night at the Opera");
        assertThat(resposta.texto()).isNull();
    }

    @Test
    void deveRejeitarReviewSemAlvo() {
        assertThatThrownBy(() -> reviewService.criarReview(
                new ReviewRequestDTO(null, null, BigDecimal.valueOf(5), null)
        )).isInstanceOf(DadosReviewInvalidosException.class);
    }

    @Test
    void deveRejeitarReviewComDoisAlvos() {
        assertThatThrownBy(() -> reviewService.criarReview(
                new ReviewRequestDTO(20L, 10L, BigDecimal.valueOf(5), null)
        )).isInstanceOf(DadosReviewInvalidosException.class);
    }

    @Test
    void deveRejeitarNotaForaDoIntervalo() {
        assertThatThrownBy(() -> reviewService.criarReview(
                new ReviewRequestDTO(20L, null, BigDecimal.valueOf(6), null)
        )).isInstanceOf(DadosReviewInvalidosException.class);

        assertThatThrownBy(() -> reviewService.criarReview(
                new ReviewRequestDTO(20L, null, BigDecimal.valueOf(0), null)
        )).isInstanceOf(DadosReviewInvalidosException.class);
    }

    @Test
    void deveAceitarNotaComMeiaEstrelaERejeitarPassoInvalido() {
        when(musicaRepository.findById(20L)).thenReturn(Optional.of(musica));
        when(reviewRepository.existsByUsuario_IdAndMusica_IdMusica(1L, 20L))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> {
                    Review review = invocation.getArgument(0);
                    ReflectionTestUtils.setField(review, "idReview", 102L);
                    return review;
                });

        ReviewResponseDTO resposta = reviewService.criarReview(
                new ReviewRequestDTO(20L, null, new BigDecimal("3.5"), null)
        );

        assertThat(resposta.nota()).isEqualByComparingTo(new BigDecimal("3.5"));

        assertThatThrownBy(() -> reviewService.criarReview(
                new ReviewRequestDTO(20L, null, new BigDecimal("3.3"), null)
        )).isInstanceOf(DadosReviewInvalidosException.class);
    }

    @Test
    void deveRejeitarReviewDuplicadaDeMusica() {
        when(musicaRepository.findById(20L)).thenReturn(Optional.of(musica));
        when(reviewRepository.existsByUsuario_IdAndMusica_IdMusica(1L, 20L))
                .thenReturn(true);

        assertThatThrownBy(() -> reviewService.criarReview(
                new ReviewRequestDTO(20L, null, BigDecimal.valueOf(5), null)
        )).isInstanceOf(ReviewJaExisteException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoMusicaNaoExiste() {
        when(musicaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.criarReview(
                new ReviewRequestDTO(999L, null, BigDecimal.valueOf(5), null)
        )).isInstanceOf(MusicaNaoEncontradaException.class);
    }

    @Test
    void deveLancarExcecaoQuandoAlbumNaoExiste() {
        when(albumRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.criarReview(
                new ReviewRequestDTO(null, 999L, BigDecimal.valueOf(5), null)
        )).isInstanceOf(AlbumNaoEncontradoException.class);
    }

    @Test
    void deveBuscarReviewDeOutroUsuarioSomenteParaVisualizacao() {
        Usuario outroUsuario = new Usuario("João", "joao@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(outroUsuario, "id", 2L);

        Review review = new Review(outroUsuario, musica, null, BigDecimal.valueOf(4), "Muito bom");
        ReflectionTestUtils.setField(review, "idReview", 70L);

        when(reviewRepository.findById(70L)).thenReturn(Optional.of(review));

        ReviewResponseDTO resposta = reviewService.buscarPorId(70L);

        assertThat(resposta.idReview()).isEqualTo(70L);
        assertThat(resposta.autor().nome()).isEqualTo("João");
        assertThat(resposta.minhaReview()).isFalse();
    }

    @Test
    void deveLancarExcecaoAoBuscarReviewInexistente() {
        when(reviewRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.buscarPorId(404L))
                .isInstanceOf(ReviewNaoEncontradaException.class);
    }

    @Test
    void deveAtualizarReviewDoProprioUsuario() {
        Review review = new Review(usuarioLogado, musica, null, BigDecimal.valueOf(3), "Ok");
        ReflectionTestUtils.setField(review, "idReview", 50L);

        when(reviewRepository.findById(50L)).thenReturn(Optional.of(review));

        ReviewResponseDTO resposta = reviewService.atualizarReview(
                50L,
                new ReviewAtualizacaoRequestDTO(BigDecimal.valueOf(5), "Mudei de ideia")
        );

        assertThat(resposta.nota()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(resposta.texto()).isEqualTo("Mudei de ideia");
    }

    @Test
    void deveRejeitarAtualizacaoDeReviewDeOutroUsuario() {
        Usuario outroUsuario = new Usuario("João", "joao@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(outroUsuario, "id", 2L);

        Review review = new Review(outroUsuario, musica, null, BigDecimal.valueOf(3), null);
        ReflectionTestUtils.setField(review, "idReview", 50L);

        when(reviewRepository.findById(50L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.atualizarReview(
                50L,
                new ReviewAtualizacaoRequestDTO(BigDecimal.valueOf(5), null)
        )).isInstanceOf(ReviewAcessoNegadoException.class);
    }

    @Test
    void deveExcluirReviewDoProprioUsuario() {
        Review review = new Review(usuarioLogado, musica, null, BigDecimal.valueOf(3), null);
        ReflectionTestUtils.setField(review, "idReview", 50L);

        when(reviewRepository.findById(50L)).thenReturn(Optional.of(review));

        reviewService.excluirReview(50L);

        verify(reviewRepository).delete(review);
    }

    @Test
    void deveRejeitarExclusaoDeReviewDeOutroUsuario() {
        Usuario outroUsuario = new Usuario("João", "joao@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(outroUsuario, "id", 2L);

        Review review = new Review(outroUsuario, null, album, BigDecimal.valueOf(3), null);
        ReflectionTestUtils.setField(review, "idReview", 60L);

        when(reviewRepository.findById(60L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.excluirReview(60L))
                .isInstanceOf(ReviewAcessoNegadoException.class);

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deveLancarExcecaoAoExcluirReviewInexistente() {
        when(reviewRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.excluirReview(404L))
                .isInstanceOf(ReviewNaoEncontradaException.class);
    }

    @Test
    void deveMarcarMinhaReviewCorretamenteNoFeed() {
        Usuario outroUsuario = new Usuario("João", "joao@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(outroUsuario, "id", 2L);

        Review reviewPropria = new Review(usuarioLogado, musica, null, BigDecimal.valueOf(5), null);
        ReflectionTestUtils.setField(reviewPropria, "idReview", 1L);

        Review reviewAlheia = new Review(outroUsuario, null, album, BigDecimal.valueOf(3), null);
        ReflectionTestUtils.setField(reviewAlheia, "idReview", 2L);

        Page<Review> pagina = new PageImpl<>(List.of(reviewPropria, reviewAlheia));

        when(reviewRepository.findAllByOrderByCriadaEmDesc(any(Pageable.class)))
                .thenReturn(pagina);

        PaginaResponseDTO<ReviewResponseDTO> resultado =
                reviewService.listarFeed(null, null);

        assertThat(resultado.itens()).hasSize(2);
        assertThat(resultado.itens().get(0).minhaReview()).isTrue();
        assertThat(resultado.itens().get(1).minhaReview()).isFalse();
    }

    @Test
    void deveListarReviewsDosUsuariosSeguidos() {
        when(seguidorUsuarioRepository.buscarIdsSeguidosPeloUsuario(1L))
                .thenReturn(List.of(2L, 3L));

        Review review = new Review(usuarioLogado, musica, null, BigDecimal.valueOf(5), null);
        ReflectionTestUtils.setField(review, "idReview", 80L);
        Page<Review> pagina = new PageImpl<>(List.of(review));

        when(reviewRepository.findByUsuario_IdInOrderByCriadaEmDesc(
                eq(List.of(2L, 3L)), any(Pageable.class)
        )).thenReturn(pagina);

        PaginaResponseDTO<ReviewResponseDTO> resultado =
                reviewService.listarSeguindo(null, null);

        assertThat(resultado.itens()).hasSize(1);
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoSegueNinguem() {
        when(seguidorUsuarioRepository.buscarIdsSeguidosPeloUsuario(1L))
                .thenReturn(List.of());

        PaginaResponseDTO<ReviewResponseDTO> resultado =
                reviewService.listarSeguindo(null, null);

        assertThat(resultado.itens()).isEmpty();
        assertThat(resultado.totalItens()).isZero();
        verify(reviewRepository, never())
                .findByUsuario_IdInOrderByCriadaEmDesc(any(), any());
    }

    @Test
    void deveListarMinhasReviews() {
        Review review = new Review(usuarioLogado, musica, null, BigDecimal.valueOf(5), null);
        ReflectionTestUtils.setField(review, "idReview", 81L);
        Page<Review> pagina = new PageImpl<>(List.of(review));

        when(reviewRepository.findByUsuario_IdOrderByCriadaEmDesc(eq(1L), any(Pageable.class)))
                .thenReturn(pagina);

        PaginaResponseDTO<ReviewResponseDTO> resultado =
                reviewService.listarMinhas(null, null);

        assertThat(resultado.itens()).hasSize(1);
        assertThat(resultado.itens().getFirst().minhaReview()).isTrue();
    }

    @Test
    void deveListarReviewsPorMusica() {
        Page<Review> pagina = new PageImpl<>(List.of());
        when(reviewRepository.findByMusica_IdMusicaOrderByCriadaEmDesc(
                eq(20L), any(Pageable.class)
        )).thenReturn(pagina);

        PaginaResponseDTO<ReviewResponseDTO> resultado =
                reviewService.listarPorMusica(20L, null, null);

        assertThat(resultado.itens()).isEmpty();
    }

    @Test
    void deveListarReviewsPorAlbum() {
        Page<Review> pagina = new PageImpl<>(List.of());
        when(reviewRepository.findByAlbum_IdAlbumOrderByCriadaEmDesc(
                eq(10L), any(Pageable.class)
        )).thenReturn(pagina);

        PaginaResponseDTO<ReviewResponseDTO> resultado =
                reviewService.listarPorAlbum(10L, null, null);

        assertThat(resultado.itens()).isEmpty();
    }

    @Test
    void deveRejeitarPaginaNegativa() {
        assertThatThrownBy(() -> reviewService.listarFeed(-1, null))
                .isInstanceOf(DadosReviewInvalidosException.class);
    }

    @Test
    void deveRejeitarTamanhoDePaginaZeroOuNegativo() {
        assertThatThrownBy(() -> reviewService.listarFeed(0, 0))
                .isInstanceOf(DadosReviewInvalidosException.class);
    }

    @Test
    void deveLimitarTamanhoDaPaginaAoMaximo() {
        Page<Review> paginaVazia = new PageImpl<>(List.of());
        when(reviewRepository.findAllByOrderByCriadaEmDesc(any(Pageable.class)))
                .thenReturn(paginaVazia);

        reviewService.listarFeed(0, 500);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewRepository).findAllByOrderByCriadaEmDesc(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoAutenticado() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> reviewService.buscarPorId(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deveConverterAlvoDeMusicaSemAlbum() {
        Artista artistaSemAlbum = new Artista("Solo", "Solo", "Artista solo.", null);
        ReflectionTestUtils.setField(artistaSemAlbum, "idArtista", 6L);

        Musica musicaSemAlbum = new Musica(
                "Instrumental", null, 120, (short) 2020, artistaSemAlbum, null
        );
        musicaSemAlbum.setIdMusica(30L);

        Review review = new Review(
                usuarioLogado, musicaSemAlbum, null, BigDecimal.valueOf(4), null
        );
        ReflectionTestUtils.setField(review, "idReview", 90L);

        when(reviewRepository.findById(90L)).thenReturn(Optional.of(review));

        ReviewResponseDTO resposta = reviewService.buscarPorId(90L);

        assertThat(resposta.alvo().artista()).isEqualTo("Solo");
        assertThat(resposta.alvo().capaUrl()).isNull();
    }
}
