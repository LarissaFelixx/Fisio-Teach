package com.app.fisiotech.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EnvioCodigoRecuperacaoService {

    private static final Logger log = LoggerFactory.getLogger(EnvioCodigoRecuperacaoService.class);

    // O Spring só cria o JavaMailSender quando spring.mail.host está configurado; com
    // ObjectProvider a aplicação sobe normalmente sem SMTP (ex: rodando local no profile dev).
    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean logarCodigo;
    private final String remetente;
    private final long validadeMinutos;

    public EnvioCodigoRecuperacaoService(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.recuperacao-senha.logar-codigo:false}") boolean logarCodigo,
            @Value("${app.recuperacao-senha.remetente:no-reply@fisiotech.com}") String remetente,
            @Value("${app.recuperacao-senha.validade-minutos:15}") long validadeMinutos
    ) {
        this.mailSender = mailSender;
        this.logarCodigo = logarCodigo;
        this.remetente = remetente;
        this.validadeMinutos = validadeMinutos;
    }

    public void enviar(String email, String codigo) {
        if (logarCodigo) {
            // Só ligado em application-dev.properties - nunca logar o código em produção.
            log.info("[DEV] Código de recuperação de senha para {}: {}", email, codigo);
            return;
        }

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.error("Recuperação de senha solicitada, mas o SMTP não está configurado (SPRING_MAIL_HOST).");
            return;
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(email);
        mensagem.setSubject("FisioTech - código para redefinir sua senha");
        mensagem.setText("""
                Olá!

                Recebemos um pedido para redefinir a senha da sua conta FisioTech.
                Seu código é: %s

                Ele vale por %d minutos. Se você não fez esse pedido, ignore este email - sua senha continua a mesma.
                """.formatted(codigo, validadeMinutos));

        try {
            sender.send(mensagem);
        } catch (MailException ex) {
            // Não propagamos: o endpoint responde igual exista ou não o email, e um erro 500
            // aqui revelaria que a conta existe. Fica registrado no log para investigação.
            log.error("Falha ao enviar email de recuperação de senha.", ex);
        }
    }

}
