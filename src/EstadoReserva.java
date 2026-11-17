
public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    EN_CURSO,
    FINALIZADA,
    CANCELADA,
    NO_SHOW;

    public boolean esActiva() {
        return this == PENDIENTE || this == CONFIRMADA || this == EN_CURSO;
    }

    public boolean esEstadoFinal() {
        return this == FINALIZADA || this == CANCELADA || this == NO_SHOW;
    }
}