/*
 * This is a working example of XSDValidator from Tutorials Point web page
 *
 * For more information look at: https://www.tutorialspoint.com/xsd/xsd_validation.htm
 */
package com.mm.xsdvalidator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.io.FileUtils;

import org.xml.sax.SAXException;

public class XSDValidator {

    public static void main(String[] args) throws IOException {
        Map<File, String> breakSchema = new HashMap<File, String>();
        List<File> meetSchema = new ArrayList<File>();

        if (args.length != 3) {
            System.out.println("Usage : XSDValidator <file-name.xsd> <maven-repo-dir> <output-report-dir>");
            return;
        }

        File repoDir = new File(args[1]);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new IOException("Argument: " + args[1] + " does not point to directory or does not exist");
        }

        File outputDir = new File(args[2]);
        if (!outputDir.exists() || !outputDir.isDirectory()) {
            throw new IOException("Argument: " + args[2] + " does not point to directory or does not exist");
        }

        for (File pomFile : FileUtils.listFiles(repoDir, new String[]{"pom"}, true)) {
            validateXMLSchema(args[0], pomFile, breakSchema, meetSchema);
        }

        createReport(meetSchema, breakSchema, outputDir);
    }

    public static void validateXMLSchema(String xsdPath, File xmlFile,
            Map<File, String> breakSchema, List<File> meetSchema) {
        try {
            SchemaFactory factory
                    = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlFile));
        } catch (IOException e) {
            breakSchema.put(xmlFile, "Exception: " + e.getMessage());
            return;

        } catch (SAXException e1) {
            breakSchema.put(xmlFile, "Exception: " + e1.getMessage());
            return;
        }

        meetSchema.add(xmlFile);
    }

    private static void createReport(List<File> meetSchema,
            Map<File, String> breakSchema, File outputDir) throws IOException {

        File report = new File(outputDir, "TEST-XSDSchema.xml");

        try (BufferedWriter fr = new BufferedWriter(new FileWriter(report))) {
            fr.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            fr.newLine();
            fr.write("<testsuite name=\"XSDValidator\" time=\"0\" tests=\""
                    + (breakSchema.size() + meetSchema.size()) + "\" errors=\"0\" "
                    + "skipped=\"0\" failures=\"" + breakSchema.size() + "\">");
            fr.newLine();

            for (File file : meetSchema) {
                fr.write("  <testcase name=\"" + file.getName() + "\"/>");
                fr.newLine();
            }

            for (File file : breakSchema.keySet()) {
                fr.write("  <testcase name=\"" + file.getName() + "\">");
                fr.newLine();
                fr.write("    <failure message=\"" + breakSchema.get(file) + "\"/>");
                fr.newLine();
                fr.write("  </testcase>");
                fr.newLine();
            }

            fr.write("</testsuite>");
            fr.newLine();
        }
    }
}
