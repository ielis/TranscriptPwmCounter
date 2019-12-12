package org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Decode Yaml file with PWM definitions into {@link PWMatrix} objects.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.0.1
 * @see PWMatrix
 * @since 0.0
 */
class PwmParser {

    /**
     * Map with PWM names as keys and {@link PWMatrix} instances as values.
     */
    private final Map<String, PWMatrix> matrixMap;

    /**
     * Parse Yaml content of provided reader, create PWM representations ({@link PWMatrix}) and store them in {@link
     * Map} by their names.
     *
     * @param reader {@link Reader} with PWM definitions in Yaml format as described in {@link PWMatrix} class
     *               description
     */
    PwmParser(Reader reader) {
        this.matrixMap = parseAll(reader);
    }

    /**
     * Decode records from provided Yaml file into corresponding {@link PWMatrix} objects, store them in a {@link Map}
     * by their names.
     *
     * @param reader {@link Reader} with PWM definitions in Yaml format as described in {@link PWMatrix} class
     *               description. This method is not responsible for closing of the reader after parsing
     * @return {@link Map} key - PWM name, Value - {@link PWMatrix} object.
     */
    static Map<String, PWMatrix> parseAll(Reader reader) {
        Map<String, PWMatrix> matrixMap = new HashMap<>();

        Yaml yaml = new Yaml(new Constructor(PWMatrix.class));
        for (Object object : yaml.loadAll(reader)) {
            PWMatrix matrix = (PWMatrix) object;

            String name = matrix.getName();
            matrixMap.put(name, matrix);
        }
        return matrixMap;
    }

    /**
     * Decode records from provided Yaml file into corresponding {@link PWMatrix} objects, store them in a {@link Map}
     * by their names.
     *
     * @param filePath String with path to file with PWM definitions in Yaml format as described in {@link PWMatrix}
     *                 class description
     * @return {@link Map} key - PWM name, Value - {@link PWMatrix} object.
     * @throws IOException if there is problem reading file
     */
    static Map<String, PWMatrix> parseAll(Path filePath) throws IOException {
        try (Reader reader = Files.newBufferedReader(filePath)) {
            return parseAll(reader);
        }
    }

    /**
     * Get map containing names of {@link PWMatrix} as keys and {@link PWMatrix} objects as values.
     *
     * @return {@link Map}{@literal <String,PWMatrix>} with the data
     */
    Map<String, PWMatrix> getMatrixMap() {
        return matrixMap;
    }

}
