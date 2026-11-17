import java.time.LocalDate;

public class Huesped extends Usuario {
    private String rutaFotografia; // Opcional para verificación de identidad

    public Huesped(String nombre, String documento, LocalDate fechaNacimiento,
                   String telefono, String correo, String username, String password, String rutaFotografia) {
        super(nombre, documento, fechaNacimiento, telefono, correo, username, password);
        this.rutaFotografia = rutaFotografia;
    }

    public Huesped(String nombre, String documento, LocalDate fechaNacimiento,
                   String telefono, String correo) {
        this(nombre, documento, fechaNacimiento, telefono, correo, documento, "1234", "");
    }

    @Override
    public String obtenerRol() {
        return "HUESPED";
    }

    public String getRutaFotografia() {
        return rutaFotografia;
    }

    public void setRutaFotografia(String rutaFotografia) {
        this.rutaFotografia = rutaFotografia;
    }
}