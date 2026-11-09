
package org.lasalle.controller;

import java.util.ArrayList;
import java.util.List;
import org.lasalle.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class ControllerrUsuario {

    public List<Usuario> getUsuarios() throws SQLException {

        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT * FROM usuario";

        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();

        if (conn == null) {
            throw new SQLException("No se pudo conectar con MySQL");
        }

        try (PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                lista.add(fillUsuario(rs));
            }

        } finally {
            connMysql.close();
        }

        return lista;
    }

    public int saveUsuario(Usuario user) throws SQLException {

        String sql = "INSERT INTO usuario (nombre, primerApellido, segundoApellido, email) "
                   + "VALUES (?, ?, ?, ?)";

        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();

        if (conn == null) {
            throw new SQLException("No se pudo conectar con MySQL");
        }

        try (PreparedStatement pstm = conn.prepareStatement(
                sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            pstm.setString(1, user.getNombre());
            pstm.setString(2, user.getPrimerApellido());
            pstm.setString(3, user.getSegundoApellido());
            pstm.setString(4, user.getEmail());

            if (pstm.executeUpdate() == 0) {
                return -1;
            }

            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    user.setIdUsuario(idGenerado);
                    return idGenerado;
                }
            }

            return -1;

        } finally {
            connMysql.close();
        }
    }

    public Usuario fillUsuario(ResultSet rs) throws SQLException {

        Usuario user = new Usuario();

        user.setIdUsuario(rs.getInt("idUsuario"));
        user.setNombre(rs.getString("nombre"));
        user.setPrimerApellido(rs.getString("primerApellido"));
        user.setSegundoApellido(rs.getString("segundoApellido"));
        user.setEmail(rs.getString("email"));

        return user;
    }
}
