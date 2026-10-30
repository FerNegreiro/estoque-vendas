package br.com.estoque.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ErroResponse(int status, String erro, String mensagem, List<String> detalhes,
                           OffsetDateTime momento) {
}
