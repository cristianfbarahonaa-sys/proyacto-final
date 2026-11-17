import java.time.LocalDate;

public class Empleado extends Usuario {
    private String cargo; // "RECEPCIONISTA" o "ADMINISTRADOR"

    public Empleado(String nombre, String documento, LocalDate fechaNacimiento,
                    String telefono, String correo, String username, String password, String cargo) {
        super(nombre, documento, fechaNacimiento, telefono, correo, username, password);
        this.cargo = (cargo == null || cargo.isBlank()) ? "RECEPCIONISTA" : cargo.toUpperCase();
    }

    @Override
    public String obtenerRol() {
        return cargo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo.toUpperCase();
    }
}