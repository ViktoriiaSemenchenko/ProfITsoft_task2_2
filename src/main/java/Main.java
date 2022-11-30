import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read data from all files, calculate and display violations into a file.
 * The output file must contain total fines for each type of violations for all years,
 * sorted by the amount (initially the highest amount of the fine).
 *
 * @author Semenchenko V.
 */
public class Main {
    public static void main(String[] args) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("./src/main/resources/output.xml"))){
            Map<String, Double> allViolations = new LinkedHashMap<>();

            File violations2018JsonFile = new File("src/main/resources/violations2018.json");
            File violations2019JsonFile = new File("src/main/resources/violations2019.json");
            File violations2020JsonFile = new File("src/main/resources/violations2020.json");

            allViolations = getStatisticsOfViolations(violations2018JsonFile, allViolations);
            allViolations = getStatisticsOfViolations(violations2019JsonFile, allViolations);
            allViolations = getStatisticsOfViolations(violations2020JsonFile, allViolations);

            writeStatisticsOfViolations(allViolations, writer);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * getStatisticsOfViolations
     *
     * Receives data from files, calculates statistics and sorts it.
     *
     * @param jsonFile
     * @param allViolations
     * @return
     */
    public static Map<String, Double> getStatisticsOfViolations(File jsonFile, Map<String, Double> allViolations) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Double> sortedAllViolations = new LinkedHashMap<>();
        try {
            Violation[] violations = objectMapper.readValue(jsonFile, Violation[].class);

            //Writes the type of violation as a map key and a fine as a value. Summarizes all fines
            for (Violation violation : violations) {
                String key = violation.getType();
                double value = violation.getFine_amount();
                if (allViolations.containsKey(key)) {
                    allViolations.put(key, allViolations.get(key) + value);
                } else {
                    allViolations.put(key, value);
                }
            }

            //sorted and put to new map
            allViolations.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .forEach(entry -> sortedAllViolations.put(entry.getKey(), entry.getValue()));

            return sortedAllViolations;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * writeStatisticsOfViolations
     *
     * Output violation statistics into a file
     *
     * @param violations
     * @param writer
     * @throws IOException
     */
    public static void writeStatisticsOfViolations(Map<String, Double> violations, BufferedWriter writer) throws IOException {
        writer.write("<violations>\n");

        violations.forEach((key, value) -> {
            try {
                writer.write("    " + "<violation type=\"" + key + "\" fine_amount=\"" + value + "\" />\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        writer.write("</violations>\n");
    }
}

