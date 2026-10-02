package org.ctw.dao;

import org.ctw.config.ConnectionFactory;
import org.ctw.exception.DatabaseException;
import org.ctw.model.AlertaMotor;
import org.ctw.model.Motor;
import org.ctw.model.Setor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlertaMotorDAO {

    public AlertaMotor inserir(AlertaMotor alerta) {

        String sql = """
                INSERT INTO alertas_motores (
                    motor_id,
                    data_alerta,
                    tipo_anomalia,
                    criticidade,
                    descricao,
                    resolvido
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try(Connection connection = ConnectionFactory.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            statement.setInt(1, alerta.getMotorId());
            statement.setTimestamp(2, Timestamp.valueOf(alerta.getDataAlerta()));
            statement.setString(3, alerta.getTipoAnomalia());
            statement.setString(4, alerta.getCriticidade());
            statement.setString(5, alerta.getDescricao());
            statement.setBoolean(6, alerta.getResolvido());

            int linhasAfetadas = statement.executeUpdate();

            if(linhasAfetadas == 0){
                throw new DatabaseException("ERRO: O alerta não foi cadastrado.", null);
            }

            try(ResultSet resultSet = statement.getGeneratedKeys()){

                if(resultSet.next()){
                    alerta.setId(resultSet.getInt(1));
                }
            }

            return alerta;

        } catch (SQLException e){
            throw new DatabaseException("ERRO: Não foi possível cadastrar o alerta.", e);
        }
    }

    public List<AlertaMotor> listarTodos() {
        String sql =
                """
                   SELECT id,
                          motor_id,
                          data_alerta,
                          tipo_anomalia,
                          criticidade,
                          descricao,
                          resolvido
                   FROM alertas_motores
                   ORDER BY data_alerta DESC
                """;

        List<AlertaMotor> alertas = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                alertas.add(mapearAlerta(resultSet));
            }

            return alertas;

        } catch (SQLException e) {
            throw new DatabaseException("ERRO: Não foi possível listar os alertas.", e);
        }
    }

    public Optional<AlertaMotor> buscarPorId(Integer id) {
        String sql = """
                SELECT id,
                       motor_id,
                       data_alerta,
                       tipo_anomalia,
                       criticidade,
                       descricao,
                       resolvido
                FROM alertas_motores
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapearAlerta(resultSet));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DatabaseException("ERRO: Não foi possível encontrar o alerta.", e);
        }
    }

    public List<AlertaMotor> listarNaoResolvidos() {

        String sql = """
                SELECT id,
                       motor_id,
                       data_alerta,
                       tipo_anomalia,
                       criticidade,
                       descricao,
                       resolvido
                FROM alertas_motores
                WHERE resolvido = false
                """;

        List<AlertaMotor> alertas = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    alertas.add(mapearAlerta(resultSet));
                }
            }

            return alertas;

        } catch (SQLException e) {
            throw new DatabaseException("ERRO: não foi possível listar alertas não resolvidos.", e);
        }
    }

    public List<AlertaMotor> buscarPorCriticidade(String criticidade) {
        String sql = """
                SELECT id,
                       motor_id,
                       data_alerta,
                       tipo_anomalia,
                       criticidade,
                       descricao,
                       resolvido
                FROM alertas_motores
                WHERE criticidade LIKE ?
                """;

        List<AlertaMotor> alertas = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + criticidade + "%");

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    alertas.add(mapearAlerta(resultSet));
                }
            }

            return alertas;

        } catch (SQLException e) {
            throw new DatabaseException("ERRO: Não foi possível listar os alertas por criticidade", e);
        }
    }

    public boolean marcarComoResolvido(Integer id) {
        String sql = """
                UPDATE alertas_motores
                SET resolvido = true
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int linhasAfetadas = statement.executeUpdate();

            if(linhasAfetadas == 0){
                throw new DatabaseException("ERRO: O alerta não foi atualizado", null);
            }

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("ERRO: Não foi possível marcar o alerta como resolvido", e);
        }
    }

    public boolean excluir(Integer id) {

        String sql = """
                     DELETE FROM alertas_motores
                     WHERE id = ?
                     """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int linhasAfetadas = statement.executeUpdate();

            if(linhasAfetadas == 0){
                throw new DatabaseException("ERRO: O alerta não foi excluído", null);
            }

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException(" ERRO: Não foi possível excluir o alerta. Verifique se existem motores associados.", e);
        }
    }

    private AlertaMotor mapearAlerta(ResultSet resultSet) throws SQLException {

        AlertaMotor alerta = new AlertaMotor();

        alerta.setId(resultSet.getInt("id"));
        alerta.setMotorId(resultSet.getInt("motor_id"));
        Timestamp dataAlerta = resultSet.getTimestamp("data_alerta");

        if (dataAlerta != null) {alerta.setDataAlerta(dataAlerta.toLocalDateTime());}

        alerta.setTipoAnomalia(resultSet.getString("tipo_anomalia"));
        alerta.setCriticidade(resultSet.getString("criticidade"));
        alerta.setDescricao(resultSet.getString("descricao"));
        alerta.setResolvido(resultSet.getBoolean("resolvido"));

        return alerta;
    }
}
