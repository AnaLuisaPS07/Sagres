package com.sagres.modulo_usuario.application.api;

import com.sagres.modulo_usuario.application.dto.MensagemResponse;
import com.sagres.modulo_usuario.application.dto.TokenResponse;
import com.sagres.modulo_usuario.application.service.UsuarioService;
import com.sagres.modulo_usuario.infrastructure.security.JwtService;
import com.sagres.modulo_usuario.infrastructure.security.UsuarioDetailsService;
import com.sagres.shared.exception.ConflictException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Este teste verifica a lógica do CONTROLLER (status codes por cenário),
// não as regras de segurança — isso é testado separadamente, se necessário.
//
// addFilters = false desliga a cadeia de filtros do Spring Security
// (incluindo JwtAuthFilter e authorizeHttpRequests) para este teste.
//
// excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class evita
// que o Spring Boot crie um usuário padrão automático (inMemoryUserDetailsManager)
// quando não há outro UserDetailsService real configurado no contexto do slice.
// Sem essa exclusão, esse usuário automático entra em conflito com o mock de
// UsuarioDetailsService (dois beans candidatos ao tipo UserDetailsService),
// e o Spring não sabe qual usar — o contexto falha ao subir.
@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean UsuarioService usuarioService;

    // O Spring ainda precisa construir o JwtAuthFilter como bean (ele é
    // detectado automaticamente por ser um Filter), mesmo que addFilters=false
    // impeça sua execução nas requisições deste teste. Por isso essas duas
    // dependências dele continuam precisando de mock.
    @MockBean JwtService jwtService;
    @MockBean UsuarioDetailsService usuarioDetailsService;

    // ──────────────────────────────────────────────────────────
    // POST /api/auth/cadastrar
    // ──────────────────────────────────────────────────────────

    @Test
    void cadastrar_deveRetornar201_quandoSucesso() throws Exception {
        Mockito.when(usuarioService.cadastrar(any()))
               .thenReturn(new MensagemResponse("Cadastro realizado."));

        mockMvc.perform(post("/api/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Maria Silva",
                                  "email": "maria@discente.ufg.br",
                                  "senha": "Senha@123"
                                }
                                """))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void cadastrar_deveRetornar400_quandoCamposVazios() throws Exception {
        mockMvc.perform(post("/api/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void cadastrar_deveRetornar409_quandoEmailDuplicado() throws Exception {
        Mockito.when(usuarioService.cadastrar(any()))
               .thenThrow(new ConflictException("E-mail já cadastrado."));

        mockMvc.perform(post("/api/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Maria",
                                  "email": "maria@discente.ufg.br",
                                  "senha": "Senha@123"
                                }
                                """))
               .andExpect(status().isConflict());
    }

    // ──────────────────────────────────────────────────────────
    // POST /api/auth/login
    // ──────────────────────────────────────────────────────────

    @Test
    void login_deveRetornar200_quandoCredenciaisValidas() throws Exception {
        Mockito.when(usuarioService.login(any()))
               .thenReturn(TokenResponse.bearer("jwt.token.aqui", "maria@discente.ufg.br", "ALUNO"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "maria@discente.ufg.br",
                                  "senha": "Senha@123"
                                }
                                """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.token").exists())
               .andExpect(jsonPath("$.tipo").value("Bearer"));
    }
}