package com.sagres.modulo_usuario.domain.vo;

import com.sagres.shared.exception.DomainException;

import java.util.Objects;

/**
 * Value Object imutável que representa um e-mail institucional da UFG.
 * <p>
 * Domínios aceitos: @ufg.br e qualquer subdomínio institucional
 * (ex.: @discente.ufg.br, @inf.ufg.br, @eee.ufg.br), já que alunos,
 * professores e servidores de diferentes unidades usam domínios distintos.
 * Lança {@link DomainException} se o valor for inválido — falha rápida no domínio.
 * </p>
 */
public final class EmailInstitucional {

    private static final String SUFIXO_UFG = ".ufg.br";
    private static final String DOMINIO_UFG_DIRETO = "@ufg.br";

    private final String valor;

    public EmailInstitucional(String valor) {
        validar(valor);
        this.valor = valor.trim().toLowerCase();
    }

    private void validar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DomainException("O e-mail institucional não pode ser vazio.");
        }

        String normalizado = valor.trim().toLowerCase();

        boolean dominioValido = normalizado.endsWith(SUFIXO_UFG)
                || normalizado.endsWith(DOMINIO_UFG_DIRETO);

        if (!dominioValido) {
            throw new DomainException(
                    "E-mail inválido: apenas endereços institucionais @ufg.br são aceitos."
            );
        }

        // Validação básica de formato (parte local não vazia)
        int arrobaIndex = normalizado.indexOf('@');
        if (arrobaIndex <= 0) {
            throw new DomainException("Formato de e-mail inválido.");
        }
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailInstitucional)) return false;
        EmailInstitucional other = (EmailInstitucional) o;
        return Objects.equals(valor, other.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}