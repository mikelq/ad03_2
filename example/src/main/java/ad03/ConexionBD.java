
package ad03;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD {


    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = "jdbc:mysql://127.0.0.1:3306/dbformacion?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String usuario = "root";
        String contraseña = "secret";

        return DriverManager.getConnection(url, usuario, contraseña);
    }
}
