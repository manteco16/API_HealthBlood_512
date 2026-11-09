/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.controller;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author osmar
 */
public class ConnectionMysql {

    Connection conn;

    public Connection open() {
        String host = getEnv("DB_HOST", "127.0.0.1");
        String port = getEnv("DB_PORT", "3306");
        String dbName = getEnv("DB_NAME", "gluco_api");
        String user = getEnv("DB_USER", "root");
        String password = getEnv("DB_PASSWORD", "root");
        String sslMode = getEnv("DB_SSL_MODE", "DISABLED");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
        String parametros = "?sslMode=" + sslMode
                + "&useUnicode=true&characterEncoding=utf-8";

        if (!"DISABLED".equalsIgnoreCase(sslMode)) {
            parametros += "&enabledTLSProtocols=TLSv1.2,TLSv1.3";
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url + parametros, user, password);
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
