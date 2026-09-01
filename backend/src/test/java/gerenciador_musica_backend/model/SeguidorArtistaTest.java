package gerenciador_musica_backend.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SeguidorArtistaTest {

    @Test
    void deveArmazenarUsuarioEArtista() {
        Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        Artista artista = new Artista("Queen", "Queen", "Banda britânica.", null);

        SeguidorArtista seguidor = new SeguidorArtista(usuario, artista);

        assertThat(seguidor.getUsuario()).isEqualTo(usuario);
        assertThat(seguidor.getArtista()).isEqualTo(artista);
    }

    @Test
    void deveConsiderarIdsIguaisEDiferentes() {
        SeguidorArtistaId id1 = new SeguidorArtistaId(1L, 2L);
        SeguidorArtistaId id2 = new SeguidorArtistaId(1L, 2L);
        SeguidorArtistaId id3 = new SeguidorArtistaId(1L, 3L);

        assertThat(id1).isEqualTo(id1);
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).hasSameHashCodeAs(id2);
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1.equals(null)).isFalse();
        assertThat(id1).isNotEqualTo("não é um SeguidorArtistaId");
    }

    @Test
    void deveExporGettersESettersDoId() {
        SeguidorArtistaId id = new SeguidorArtistaId();
        id.setUsuario(5L);
        id.setArtista(9L);

        assertThat(id.getUsuario()).isEqualTo(5L);
        assertThat(id.getArtista()).isEqualTo(9L);
    }

    @Test
    void deveRegistrarDataDeCriacaoAoSerPersistido() {
        SeguidorArtista seguidor = new SeguidorArtista(null, null);
        ReflectionTestUtils.setField(seguidor, "seguidoEm", java.time.OffsetDateTime.now());

        assertThat(seguidor.getSeguidoEm()).isNotNull();
    }
}
