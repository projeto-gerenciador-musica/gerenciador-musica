package gerenciador_musica_backend.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SeguidorUsuarioTest {

    @Test
    void deveArmazenarSeguidorESeguido() {
        Usuario seguidor = new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        Usuario seguido = new Usuario("João", "joao@email.com", "hash", Role.USER);

        SeguidorUsuario relacao = new SeguidorUsuario(seguidor, seguido);

        assertThat(relacao.getSeguidor()).isEqualTo(seguidor);
        assertThat(relacao.getSeguido()).isEqualTo(seguido);
    }

    @Test
    void deveConsiderarIdsIguaisEDiferentes() {
        SeguidorUsuarioId id1 = new SeguidorUsuarioId(1L, 2L);
        SeguidorUsuarioId id2 = new SeguidorUsuarioId(1L, 2L);
        SeguidorUsuarioId id3 = new SeguidorUsuarioId(1L, 3L);

        assertThat(id1).isEqualTo(id1);
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).hasSameHashCodeAs(id2);
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1.equals(null)).isFalse();
        assertThat(id1).isNotEqualTo("não é um SeguidorUsuarioId");
    }

    @Test
    void deveExporGettersESettersDoId() {
        SeguidorUsuarioId id = new SeguidorUsuarioId();
        id.setSeguidor(5L);
        id.setSeguido(9L);

        assertThat(id.getSeguidor()).isEqualTo(5L);
        assertThat(id.getSeguido()).isEqualTo(9L);
    }

    @Test
    void deveRegistrarDataDeCriacaoAoSerPersistido() {
        SeguidorUsuario relacao = new SeguidorUsuario(null, null);
        ReflectionTestUtils.setField(relacao, "seguidoEm", java.time.OffsetDateTime.now());

        assertThat(relacao.getSeguidoEm()).isNotNull();
    }
}
