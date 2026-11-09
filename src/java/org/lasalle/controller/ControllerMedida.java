/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.model.Medida;

/**
 *
 * @author osmar
 */
public class ControllerMedida {

    public List<Medida> getMedidas() throws SQLException {
        return query("SELECT * FROM medida", null);
    }

    public List<Medida> getMedidasByUsuarioId(int idUsuario) throws SQLException {
        return query("SELECT * FROM medida WHERE idUsuario = ?", idUsuario);
    }

    private List<Medida> query(String sql, Integer idUsuario) throws SQLException {

        List<Medida> lista = new ArrayList<>();

        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();

        if (conn == null) {
            throw new SQLException("No se pudo conectar con MySQL");
        }

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {

            if (idUsuario != null) {
                pstm.setInt(1, idUsuario);
            }

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    lista.add(fillMedida(rs));
                }
            }

        } finally {
            connMysql.close();
        }

        return lista;
    }

    public Medida fillMedida(ResultSet rs) throws SQLException {

        Medida medida = new Medida();

        medida.setIdMedida(rs.getInt("idMedida"));
        medida.setIdUsuario(rs.getInt("idUsuario"));
        medida.setGlucosa(rs.getInt("glucosa"));
        medida.setFechaHora(rs.getString("fechaHora"));
        medida.setContexto(rs.getString("contexto"));
        medida.setNotas(rs.getString("notas"));

        return medida;
    }
}
