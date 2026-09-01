package gerenciador_musica_backend.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CurtidaAlbumTest {

    @Test
    void deveArmazenarUsuarioEAlbum() {
        Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        Artista artista = new Artista("Queen", "Queen", "Banda britânica.", null);
        Album album = new Album(artista, "A Night at the Opera", (short) 1975, null);

        CurtidaAlbum curtida = new CurtidaAlbum(usuario, album);

        assertThat(curtida.getUsuario()).isEqualTo(usuario);
        assertThat(curtida.getAlbum()).isEqualTo(album);
    }

    @Test
    void deveConsiderarIdsIguaisEDiferentes() {
        CurtidaAlbumId id1 = new CurtidaAlbumId(1L, 2L);
        CurtidaAlbumId id2 = new CurtidaAlbumId(1L, 2L);
        CurtidaAlbumId id3 = new CurtidaAlbumId(1L, 3L);

        assertThat(id1).isEqualTo(id1);
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).hasSameHashCodeAs(id2);
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1.equals(null)).isFalse();
        assertThat(id1).isNotEqualTo("não é um CurtidaAlbumId");
    }

    @Test
    void deveExporGettersESettersDoId() {
        CurtidaAlbumId id = new CurtidaAlbumId();
        id.setUsuario(5L);
        id.setAlbum(9L);

        assertThat(id.getUsuario()).isEqualTo(5L);
        assertThat(id.getAlbum()).isEqualTo(9L);
    }

    @Test
    void deveRegistrarDataDeCriacaoAoSerPersistido() {
        CurtidaAlbum curtida = new CurtidaAlbum(null, null);
        ReflectionTestUtils.setField(curtida, "curtidaEm", java.time.OffsetDateTime.now());

        assertThat(curtida.getCurtidaEm()).isNotNull();
    }
}
