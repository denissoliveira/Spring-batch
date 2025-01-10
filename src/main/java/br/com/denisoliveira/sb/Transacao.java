package br.com.denisoliveira.sb;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public record Transacao (
        Long id,
        Integer tipo,
        Date data,
        BigDecimal valor,
        Long cpf,
        String cartao,
        Time hora,
        String donoLoja,
        String nomeLoja
) {
    public Transacao withValor(BigDecimal valor) {
        return new Transacao(
            this.id(),
            this.tipo(),
            this.data(),
            valor,
            this.cpf(),
            this.cartao(),
            this.hora(),
            this.donoLoja(),
            this.nomeLoja()
        );
    }

    public Transacao withData(String data) throws ParseException {
        var dateFormat = new SimpleDateFormat("yyyyMMdd");
        var date = dateFormat.parse(data);
        return new Transacao(
                this.id(),
                this.tipo(),
                new Date(date.getTime()),
                this.valor,
                this.cpf(),
                this.cartao(),
                this.hora(),
                this.donoLoja(),
                this.nomeLoja()
        );
    }

    public Transacao withHora(String hora) throws ParseException {
        var dateFormat = new SimpleDateFormat("HHmmss");
        var date = dateFormat.parse(hora);
        return new Transacao(
                this.id(),
                this.tipo(),
                this.data(),
                this.valor(),
                this.cpf(),
                this.cartao(),
                new Time(date.getTime()),
                this.donoLoja(),
                this.nomeLoja()
        );
    }
}