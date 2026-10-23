package ad03;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class Ejer3 {

    public static void main(String[] args) {

        try (Scanner teclado = new Scanner(System.in);
             Connection con = ConexionBD.obtenerConexion()) {

            System.out.print("Introduce el DNI: ");
            String dni = teclado.nextLine().trim().toUpperCase();

            // Ocho números seguidos de una letra
            if (!dni.matches("\\d{8}[A-Z]")) {
                System.out.println(
                        "DNI no válido. Debe contener 8 números y una letra.");
                return;
            }

            // al poner el AutoCommit en false, hacemos que no se realicen los cambios aun en la BBDD.
            con.setAutoCommit(false);

            try {
                String nombre = buscarNombre(con, dni);
                boolean alumnoNuevo = (nombre == null);

                if (alumnoNuevo) {
                    System.out.print("Nombre del nuevo alumno: ");
                    nombre = teclado.nextLine().trim();

                    if (nombre.equals("")) {
                        
                    	System.out.println("El nombre no puede estar vacío.");
                    	return;
                    }
                } else {
                    System.out.println("Alumno encontrado: " + nombre);
                }
                
                System.out.println("\n Cursos disponibles:");
                mostrarCursos(con);

                System.out.println("Introduce el ID del curso: ");
                int idCurso;

                try {
                    idCurso = Integer.parseInt(
                            teclado.nextLine().trim());
                } catch (NumberFormatException e) {
                	System.out.println("El ID debe ser un número entero.");
                	return;
                }

                // Comprobamos que el curso existe
                Curso curso = buscarCurso(con, idCurso);

                if (curso == null) {
                	System.out.println("El curso no existe.");
                	return;
                }

                // Comprobamos que quedan plazas
                if (curso.capacidad() - curso.matriculados() <= 0) {
                	System.out.println("El curso está completo.");
                	return;
                }

                // Comprobamos que no este ya matriculado
                if (yaMatriculado(con, dni, idCurso)) {
                	System.out.println("El alumno ya está matriculado en ese curso.");
                	return;
                }

                // Dar de alta al alumno solo dentro de la transacción
                if (alumnoNuevo) {
                    String sqlAlumno =
                            "INSERT INTO alumnos (dni, nombre) VALUES (?, ?)";

                    try (PreparedStatement ps =
                                 con.prepareStatement(sqlAlumno)) {
                        ps.setString(1, dni);
                        ps.setString(2, nombre);
                        ps.executeUpdate();
                    }
                }

                // Registrar la matrícula
                String sqlMatricula = """
                        INSERT INTO matriculas
                            (dni, id_curso, fecha_matricula)
                        VALUES (?, ?, ?)
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(sqlMatricula)) {
                    ps.setString(1, dni);
                    ps.setInt(2, idCurso);
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                }

                con.commit();

                System.out.println(
                        "Matrícula registrada correctamente en: "
                                + curso.titulo());

            } catch (Exception e) {
            	//revertimos los cambios
                con.rollback();
                System.out.println(
                        "Error: " + e.getMessage());

            } finally {
            	//cuando ya hemos hecho todos los cambios, volvemos a poner el AutoCommit en true para que se realicen los cambios.
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error de conexión: " + e.getMessage());
        }
    }
    //creamos una clase Curso, para manejar la informaciond de los cursos
    private record Curso(
            int id,
            String titulo,
            int capacidad,
            int matriculados) {
    }
    
    //se le pasa el DNI de un alumno y devuelve el nombre
    private static String buscarNombre(
            Connection con, String dni) throws SQLException {

        String sql = "SELECT nombre FROM alumnos WHERE dni = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);

            try (ResultSet rs = ps.executeQuery()) {
            	if(rs.next()) {
            		return rs.getString("nombre");
            	}else {
            		return null;
            	}
            }  
        }
    }
    
    //muestra los cursos por consola
    private static void mostrarCursos(
            Connection con) throws SQLException {

        String sql = """
                SELECT c.id_curso, c.titulo, a.capacidad,
                       COUNT(m.dni) AS matriculados
                FROM cursos c
                JOIN aulas a ON a.id_aula = c.id_aula
                LEFT JOIN matriculas m ON m.id_curso = c.id_curso
                GROUP BY c.id_curso, c.titulo, a.capacidad
                ORDER BY c.fecha, c.id_curso
                """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int capacidad = rs.getInt("capacidad");
                int matriculados = rs.getInt("matriculados");
                int libres = capacidad - matriculados;

                System.out.printf(
                        " %d.  %-25s  Plazas libres: %d%n",
                        rs.getInt("id_curso"),
                        rs.getString("titulo"),
                        libres);
            }
        }
    }
    //
    private static Curso buscarCurso(
            Connection con, int idCurso) throws SQLException {

        String sql = """
                SELECT c.id_curso, c.titulo, a.capacidad,
                       COUNT(m.dni) AS matriculados
                FROM cursos c
                JOIN aulas a ON a.id_aula = c.id_aula
                LEFT JOIN matriculas m ON m.id_curso = c.id_curso
                WHERE c.id_curso = ?
                GROUP BY c.id_curso, c.titulo, a.capacidad
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Curso(
                            rs.getInt("id_curso"),
                            rs.getString("titulo"),
                            rs.getInt("capacidad"),
                            rs.getInt("matriculados"));
                }
                return null;
            }
        }
    }
    //metodo que al que pasando la conexion, el DNI y el ID del curso, comprueba si ese DNI esta matriculado en ese ID de curso, devuelve la matricula.
    private static boolean yaMatriculado(
            Connection con, String dni, int idCurso)
            throws SQLException {

        String sql = """
                SELECT 1 FROM matriculas
                WHERE dni = ? AND id_curso = ?
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.setInt(2, idCurso);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
