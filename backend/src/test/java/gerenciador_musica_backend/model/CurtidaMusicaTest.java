package gerenciador_musica_backend.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CurtidaMusicaTest {

    @Test
    void deveArmazenarUsuarioEMusica() {
        Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        Artista artista = new Artista("Queen", "Queen", "Banda britânica.", null);
        Musica musica = new Musica(
                "Bohemian Rhapsody", null, 354, (short) 1975, artista, null
        );

        CurtidaMusica curtida = new CurtidaMusica(usuario, musica);

        assertThat(curtida.getUsuario()).isEqualTo(usuario);
        assertThat(curtida.getMusica()).isEqualTo(musica);
    }

    @Test
    void deveConsiderarIdsIguaisEDiferentes() {
        CurtidaMusicaId id1 = new CurtidaMusicaId(1L, 2L);
        CurtidaMusicaId id2 = new CurtidaMusicaId(1L, 2L);
        CurtidaMusicaId id3 = new CurtidaMusicaId(1L, 3L);

        assertThat(id1).isEqualTo(id1);
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).hasSameHashCodeAs(id2);
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1.equals(null)).isFalse();
        assertThat(id1).isNotEqualTo("não é um CurtidaMusicaId");
    }

    @Test
    void deveExporGettersESettersDoId() {
        CurtidaMusicaId id = new CurtidaMusicaId();
        id.setUsuario(5L);
        id.setMusica(9L);

        assertThat(id.getUsuario()).isEqualTo(5L);
        assertThat(id.getMusica()).isEqualTo(9L);
    }

    @Test
    void deveRegistrarDataDeCriacaoAoSerPersistido() {
        CurtidaMusica curtida = new CurtidaMusica(null, null);
        ReflectionTestUtils.setField(curtida, "curtidaEm", java.time.OffsetDateTime.now());

        assertThat(curtida.getCurtidaEm()).isNotNull();
    }
}
