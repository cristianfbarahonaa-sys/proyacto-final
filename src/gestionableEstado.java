public interface gestionableEstado<T> {
    boolean puedeCambiarA(T nuevoEstado);
    void cambiarEstado(T nuevoEstado, String motivo);
}