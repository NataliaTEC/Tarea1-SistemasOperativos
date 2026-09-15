package minipc;

/**
 * Representa los operadores (mnemonicos) validos del lenguaje ensamblador
 * de la Mini PC, junto con su código de operación (opcode) en binario de 4 bits.
 *
 * Tabla de opcodes:
 *   LOAD  -> 0001
 *   STORE -> 0010
 *   MOV   -> 0011
 *   SUB   -> 0100
 *   ADD   -> 0101
 * @author Natalia Granados Rosales
 */
public enum Operador {

    LOAD("0001", false),
    STORE("0010", false),
    MOV("0011", true),
    SUB("0100", false),
    ADD("0101", false);

    private final String codigoBinario;
    private final boolean requiereValorInmediato;

    Operador(String codigoBinario, boolean requiereValorInmediato) {
        this.codigoBinario = codigoBinario;
        this.requiereValorInmediato = requiereValorInmediato;
    }

    public String getCodigoBinario() {
        return codigoBinario;
    }

    /** Indica si el operador necesita un valor numerico inmediato ademas del registro. */
    public boolean requiereValorInmediato() {
        return requiereValorInmediato;
    }

    /** Traduce un texto (mnemonico) a su Operador correspondiente. Acepta mayusculas/minúusculas. Devuelve null si no es reconocido. */
    public static Operador fromTexto(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return Operador.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Traduccion inversa: dado un codigo binario de 4 bits, devuelve el Operador correspondiente o null si no coincide con ninguno.
     * La usa la CPU al decodificar lo que leyo de Memoria.
     */
    public static Operador fromCodigoBinario(String codigoBinario) {
        for (Operador operador : Operador.values()) {
            if (operador.codigoBinario.equals(codigoBinario)) {
                return operador;
            }
        }
        return null;
    }
}
