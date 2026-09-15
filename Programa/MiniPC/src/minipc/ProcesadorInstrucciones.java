package minipc;

import java.util.ArrayList;
import java.util.List;

/**
 * Se encarga de validar, procesar y traducir las lineas de un programa en lenguaje ensamblador.
 * Reconoce:
 *   - Operadores: LOAD, STORE, MOV, SUB, ADD
 *   - Registros : AX, BX, CX, DX
 *
 * Sintaxis esperada por linea:
 *   MOV   registro, valor   (ej: "MOV AX, 5"  o  "MOV BX, -8")
 *   LOAD  registro           (ej: "LOAD AX")
 *   STORE registro           (ej: "STORE AX")
 *   ADD   registro           (ej: "ADD BX")
 *   SUB   registro           (ej: "SUB AX")
 *
 * Las lineas vaciias o que inician con ";" o "#" se consideran comentarios y se ignoran (no generan Instruccion).
 * @author Natalia Granados Rosales
 */
public class ProcesadorInstrucciones {

    /** Magnitud maxima representable en 7 bits (formato entero: signo + 7 bits). */
    private static final int VALOR_MAXIMO_ABSOLUTO = 127;

    /**
     * Procesa un programa completo (una linea de texto por instruccion).
     * Devuelve una Instruccion por cada linea con contenido (ignora comentarios/vacías).
     * El numero de linea reportado corresponde a la linea real dentro del archivo, para que los mensajes de error sean faciles de ubicar.
     */
    public List<Instruccion> procesarPrograma(List<String> lineas) {
        List<Instruccion> resultado = new ArrayList<>();
        int numeroLinea = 0;

        for (String linea : lineas) {
            numeroLinea++;
            String limpia = linea == null ? "" : linea.trim();

            if (limpia.isEmpty() || limpia.startsWith(";") || limpia.startsWith("#")) {
                continue; // linea vacia o comentario: se ignora
            }

            resultado.add(procesarLinea(limpia, numeroLinea));
        }
        return resultado;
    }

    /** Valida, procesa y traduce una linea de ensamblador. */
    public Instruccion procesarLinea(String linea, int numeroLinea) {
        String[] tokens = linea.trim().split("[\\s,]+");

        if (tokens.length == 0 || tokens[0].isEmpty()) {
            return Instruccion.crearInvalida(numeroLinea, linea, "Linea vacia o mal formada.");
        }

        Operador operador = Operador.fromTexto(tokens[0]);
        if (operador == null) {
            return Instruccion.crearInvalida(numeroLinea, linea, "Operador \"" + tokens[0] + "\" no reconocido. Use LOAD, STORE, MOV, SUB o ADD.");
        }

        if (operador.requiereValorInmediato()) {
            return procesarConValorInmediato(linea, numeroLinea, operador, tokens);
        } else {
            return procesarSoloRegistro(linea, numeroLinea, operador, tokens);
        }
    }

    /** Procesa instrucciones tipo "MOV registro, valor". */
    private Instruccion procesarConValorInmediato(String linea, int numeroLinea,
                                                    Operador operador, String[] tokens) {
        if (tokens.length != 3) {
            return Instruccion.crearInvalida(numeroLinea, linea, "La instruccion " + operador + " requiere registro y valor. Ej: \"MOV AX, 5\".");
        }

        Registro registro = Registro.fromTexto(tokens[1]);
        if (registro == null) {
            return Instruccion.crearInvalida(numeroLinea, linea, "Registro \"" + tokens[1] + "\" no reconocido. Use AX, BX, CX o DX.");
        }

        int valor;
        try {
            valor = Integer.parseInt(tokens[2]);
        } catch (NumberFormatException ex) {
            return Instruccion.crearInvalida(numeroLinea, linea, "Valor \"" + tokens[2] + "\" no es un número entero válido.");
        }

        if (Math.abs(valor) > VALOR_MAXIMO_ABSOLUTO) {
            return Instruccion.crearInvalida(numeroLinea, linea, "El valor " + valor + " está fuera de rango (-127 a 127).");
        }

        String valorBinario = traducirValorABinario(valor);

        return Instruccion.crearValida(numeroLinea, linea, operador, registro, valor, operador.getCodigoBinario(), registro.getCodigoBinario(), valorBinario);
    }

    /** Procesa instrucciones tipo "LOAD <registro>", "STORE <registro>", "ADD <registro>", "SUB <registro>". */
    private Instruccion procesarSoloRegistro(String linea, int numeroLinea, Operador operador, String[] tokens) {
        if (tokens.length != 2) {
            return Instruccion.crearInvalida(numeroLinea, linea, "La instrucción " + operador + " requiere un registro. Ej: \"" + operador + " AX\".");
        }

        Registro registro = Registro.fromTexto(tokens[1]);
        if (registro == null) {
            return Instruccion.crearInvalida(numeroLinea, linea, "Registro \"" + tokens[1] + "\" no reconocido. Use AX, BX, CX o DX.");
        }

        // Estas instrucciones no llevan valor inmediato: se rellena con ceros.
        String valorBinario = "00000000";

        return Instruccion.crearValida(numeroLinea, linea, operador, registro, null, operador.getCodigoBinario(), registro.getCodigoBinario(), valorBinario);
    }

    /**
     * Traduce un entero al formato de 8 bits:
     * bit 0 = signo (0 positivo, 1 negativo), bits 1-7 = magnitud.
     */
    private String traducirValorABinario(int valor) {
        String signo = valor < 0 ? "1" : "0";
        int magnitud = Math.abs(valor);
        String magnitudBinaria = Integer.toBinaryString(magnitud);
        // rellena con ceros a la izquierda hasta completar 7 bits
        while (magnitudBinaria.length() < 7) {
            magnitudBinaria = "0" + magnitudBinaria;
        }
        return signo + magnitudBinaria;
    }

    /** Indica si todas las instrucciones procesadas son validas. */
    public boolean todasValidas(List<Instruccion> instrucciones) {
        for (Instruccion instruccion : instrucciones) {
            if (!instruccion.isValida()) {
                return false;
            }
        }
        return true;
    }
}
