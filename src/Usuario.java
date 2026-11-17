import java.time.LocalDate;

public abstract class Usuario extends Persona {
    private final String documento;
    private String telefono;
    private String correo;
    private final String username;
    private String password;
    private boolean activo;

    public Usuario(String nombre, String documento, LocalDate fechaNacimiento,
                   String telefono, String correo, String username, String password) {
        super(nombre, fechaNacimiento);
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        this.documento = documento.trim();
        this.telefono = telefono;
        this.correo = correo;
        this.username = username;
        this.password = password;
        this.activo = true;
    }

    public boolean autenticar(String user, String pass) {
        return activo && this.username.equals(user) && this.password.equals(pass);
    }

    public abstract String obtenerRol();

    public String getDocumento() { return documento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getUsername() { return username; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return getNombre() + " - Doc: " + documento + " [" + obtenerRol() + "]";
    }
}