package minipc;

/**
 * Representa los registros de proposito general de la Mini PC,
 * junto con su codigo de direccionamiento en binario de 4 bits.
 *
 *   AX -> 0001
 *   BX -> 0010
 *   CX -> 0011
 *   DX -> 0100
 * @author Natalia Granados Rosales
 */
public enum Registro {

    AX("0001"),
    BX("0010"),
    CX("0011"),
    DX("0100");

    private final String codigoBinario;

    Registro(String codigoBinario) {
        this.codigoBinario = codigoBinario;
    }

    public String getCodigoBinario() {
        return codigoBinario;
    }

    /** Traduce un texto a su Registro correspondiente. Acepta mayusculas/minusculas. Devuelve null si no es reconocido. */
    public static Registro fromTexto(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return Registro.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Traduccion inversa: dado un codigo binario de 4 bits, devuelve el Registro correspondiente, o null si no coincide con ninguno.
     * La usa la CPU al decodificar lo que leyo de Memoria.
     */
    public static Registro fromCodigoBinario(String codigoBinario) {
        for (Registro registro : Registro.values()) {
            if (registro.codigoBinario.equals(codigoBinario)) {
                return registro;
            }
        }
        return null;
    }
}
