package org.ctw.service;

import org.ctw.dao.AlertaMotorDAO;
import org.ctw.model.AlertaMotor;

import java.time.LocalDateTime;
import java.util.List;

public class AlertaMotorService {
    private static final List<String> CRITICIDADES_VALIDAS =
            List.of(
                    "Baixa",
                    "Média",
                    "Alta",
                    "Crítica"
            );

    private final AlertaMotorDAO alertaDAO;
    private final MotorService motorService;

    public AlertaMotorService(
            AlertaMotorDAO alertaDAO,
            MotorService motorService
    ) {
        this.alertaDAO = alertaDAO;
        this.motorService = motorService;
    }

    public AlertaMotor cadastrar(AlertaMotor alerta) {
        validarAlerta(alerta);
        alerta.setId(null);
        alerta.setDataAlerta(LocalDateTime.now());

        return alertaDAO.inserir(alerta);
    }

    public List<AlertaMotor> listarTodos() {
        return alertaDAO.listarTodos();
    }

    public List<AlertaMotor> listarNaoResolvidos() {
        return alertaDAO.listarNaoResolvidos();
    }

    public List<AlertaMotor> buscarPorCriticidade(String criticidade) {
        validarCriticidade(criticidade);

        return alertaDAO.buscarPorCriticidade(criticidade);
    }

    public boolean marcarComoResolvido(Integer alertaId) {
        validarId(alertaId);

        return alertaDAO.marcarComoResolvido(alertaId);
    }

    private void validarAlerta(AlertaMotor alerta) {

        if (alerta == null) {
            throw new IllegalArgumentException("ERRO: Os dados do alerta são obrigatórios.");
        }

        if (alerta.getTipoAnomalia() == null || alerta.getTipoAnomalia().isBlank()) {
            throw new IllegalArgumentException("ERRO: A anomalia é obrigatória");
        }

        if (alerta.getDescricao() == null || alerta.getDescricao().isBlank()) {
            throw new IllegalArgumentException("ERRO: A descrição é obrigatória.");
        }
    }

    private void validarCriticidade(String criticidade) {
        if (!CRITICIDADES_VALIDAS.contains(criticidade)) {
            throw new IllegalArgumentException("Criticidade inválida. Valores permitidos: " + CRITICIDADES_VALIDAS);
        }
    }

    private void validarId(Integer id) {
        if ( id == null || id <= 0) {
            throw new IllegalArgumentException("ERRO: O ID do motor é obrigatório.");
        }
    }
}
