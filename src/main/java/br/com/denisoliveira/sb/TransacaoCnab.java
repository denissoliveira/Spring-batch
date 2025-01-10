package br.com.denisoliveira.sb;

import java.math.BigDecimal;

public record TransacaoCnab (
        Integer tipo,
        String data,
        BigDecimal valor,
        Long cpf,
        String cartao,
        String hora,
        String donoLoja,
        String nomeLoja
) {
}
