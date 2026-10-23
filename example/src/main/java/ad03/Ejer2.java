
package ad03;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Ejer2 {

    public static void main(String[] args) {

        try (Scanner teclado = new Scanner(System.in)) {

            System.out.print("Introduce el DNI del alumno: ");
            String dni = teclado.nextLine().trim().toUpperCase();

            try (Connection con = ConexionBD.obtenerConexion()) {

                String nombreActual;

                // Buscar al alumno
                String sqlBuscar =
                        "SELECT nombre FROM alumnos WHERE dni = ?";

                try (PreparedStatement ps =
                             con.prepareStatement(sqlBuscar)) {

                    ps.setString(1, dni);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            System.out.println(
                                    "No existe ningún alumno con ese DNI.");
                            return;
                        }

                        nombreActual = rs.getString("nombre");
                    }
                }

                System.out.println(
                        "Nombre actual: " + nombreActual);

                System.out.print(
                        "Introduce el nuevo nombre (vacío para cancelar): ");

                String nuevoNombre = teclado.nextLine().trim();

                if (nuevoNombre.equals("")) {
                    System.out.println(
                            "No se ha realizado ninguna modificación.");
                    return;
                }

                // update que modifica el nombre
                String sqlActualizar =
                        "UPDATE alumnos SET nombre = ? WHERE dni = ?";

                try (PreparedStatement ps =
                             con.prepareStatement(sqlActualizar)) {
                	//añadimos los datos al update
                    ps.setString(1, nuevoNombre);
                    ps.setString(2, dni);
                    //executeUpdate devuelve el numero de filas modificadas
                    int filas = ps.executeUpdate();

                    if (filas == 1) {
                        System.out.println(
                                "Nombre actualizado correctamente.");
                    } else {
                        System.out.println(
                                "No se realizado ningun modificación.");
                    }
                }

            } catch (SQLException e) {
                System.err.println(
                        "Error de base de datos: " + e.getMessage());
            }
        }
    }
}
