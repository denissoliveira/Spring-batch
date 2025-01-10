package br.com.denisoliveira.sb;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;

@Configuration
public class BatchConfig {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    Job job(Step step) {
        return new JobBuilder("job", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer()) //Para roda o job mais de uma vez
                .build();
    }

    @Bean
    Step step(ItemReader<TransacaoCnab> reader,
              ItemProcessor<TransacaoCnab, Transacao> processor,
              ItemWriter<Transacao> writer) {
        return new StepBuilder("step", jobRepository)
                .<TransacaoCnab, Transacao>chunk(1000,  transactionManager)//quantos itens por vez
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    FlatFileItemReader<TransacaoCnab> reader() {
        return new FlatFileItemReaderBuilder<TransacaoCnab>()
                .name("reader")
                .resource(new FileSystemResource("files/CNAB.txt"))
                .fixedLength()
                .columns(
                        new Range(1,1), new Range(2,9),
                        new Range(10,19), new Range(20,30),
                        new Range(31,42), new Range(43,48),
                        new Range(49,62), new Range(63,80)
                )
                .names("tipo", "data", "valor", "cpf", "cartao", "hora", "donoLoja", "nomeLoja")
                .targetType(TransacaoCnab.class)
                .build();
    }

    @Bean
    ItemProcessor<TransacaoCnab, Transacao> processor() {
        return item -> {
            var transacao = new Transacao(
                    null,
                    item.tipo(),
                    null,
                    null,
                    item.cpf(),
                    item.cartao(),
                    null,
                    item.donoLoja(),
                    item.nomeLoja()
            ).withValor(item.valor().divide(BigDecimal.valueOf(100)))
            .withData(item.data())
            .withHora(item.hora());
            return transacao;
        };
    }

    @Bean
    JdbcBatchItemWriter<Transacao> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transacao>()
                .dataSource(dataSource)
                .sql("""
                        insert into transacao (tipo, data, valor, cpf, cartao, hora, dono_loja, nome_loja)
                        values (:tipo, :data, :valor, :cpf, :cartao, :hora, :donoLoja, :nomeLoja)
                        """)
                .beanMapped()
                .build();
    }

}
