package br.com.estoque.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.estoque.domain.Perfil;
import br.com.estoque.domain.Usuario;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres";
    private final JwtService jwtService = new JwtService(SEGREDO, 60);
    private final Usuario usuario = new Usuario("Ana", "ana@loja.com", "hash", Perfil.ADMIN);

    @Test
    void deveGerarTokenEExtrairOEmail() {
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.extrairEmail(token)).contains("ana@loja.com");
    }

    @Test
    void deveRejeitarTokenAssinadoComOutraChave() {
        JwtService outroServidor = new JwtService("outro-segredo-completamente-diferente-123", 60);
        String tokenForjado = outroServidor.gerarToken(usuario);

        assertThat(jwtService.extrairEmail(tokenForjado)).isEmpty();
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtService expirado = new JwtService(SEGREDO, -1);
        String token = expirado.gerarToken(usuario);

        assertThat(jwtService.extrairEmail(token)).isEmpty();
    }

    @Test
    void deveRejeitarTextoQueNaoEToken() {
        assertThat(jwtService.extrairEmail("isto-nao-e-um-jwt")).isEmpty();
        assertThat(jwtService.extrairEmail("")).isEmpty();
    }

    @Test
    void deveExigirSegredoComPeloMenos32Caracteres() {
        assertThatThrownBy(() -> new JwtService("curto", 60))
                .isInstanceOf(IllegalStateException.class);
    }
}
