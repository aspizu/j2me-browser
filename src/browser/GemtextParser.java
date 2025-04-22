package browser;
import java.util.Vector;

class GemtextParser {

    public static GemtextElement[] parseGemtext(String input) {
        Vector doc = new Vector();
        boolean inPre = false;
        Vector preBuffer = new Vector();

        int startIndex = 0;
        int endIndex;

        // Process the input string
        while ((endIndex = input.indexOf("\n", startIndex)) != -1) {
            String line = extractLine(input, startIndex, endIndex).trim();
            startIndex = endIndex + 1; // Move past the newline character

            if (line.equals("```")) {
                inPre = handlePreformatted(inPre, preBuffer, doc);
                continue;
            }

            if (inPre) {
                preBuffer.addElement(line);
                continue;
            }

            processLine(line, doc);
        }

        // Handle the last line after the final newline
        String lastLine = input.substring(startIndex).trim();
        if (lastLine.length() > 0) {
            doc.addElement(new Paragraph(lastLine));
        }

        // Convert Vector to GemtextElement[]
        return convertVectorToArray(doc);
    }

    // Extract a single line from the input
    private static String extractLine(String input, int startIndex, int endIndex) {
        return input.substring(startIndex, endIndex);
    }

    // Handle the preformatted block logic
    private static boolean handlePreformatted(boolean inPre, Vector preBuffer, Vector doc) {
        if (inPre) {
            String[] preLines = new String[preBuffer.size()];
            for (int j = 0; j < preBuffer.size(); j++) {
                preLines[j] = (String) preBuffer.elementAt(j);
            }
            doc.addElement(new Preformatted(preLines));
            preBuffer.removeAllElements();
        }
        return !inPre;
    }

    // Process the current line based on its prefix
    private static void processLine(String line, Vector doc) {
        if (line.startsWith("###")) {
            doc.addElement(new Heading(3, line.substring(3).trim()));
        } else if (line.startsWith("##")) {
            doc.addElement(new Heading(2, line.substring(2).trim()));
        } else if (line.startsWith("#")) {
            doc.addElement(new Heading(1, line.substring(1).trim()));
        } else if (line.startsWith("=>")) {
            processLink(line, doc);
        } else if (line.startsWith("*")) {
            doc.addElement(new ListItem(line.substring(1).trim()));
        } else if (line.startsWith(">")) {
            doc.addElement(new Quote(line.substring(1).trim()));
        } else if (line.length() > 0) {
            doc.addElement(new Paragraph(line));
        }
    }

    // Handle processing of a link line
    private static void processLink(String line, Vector doc) {
        String[] parts = customSplit(line.substring(2).trim(), " ", 2);  // Custom split
        String url = parts[0];
        String label = parts.length > 1 ? parts[1] : url;
        doc.addElement(new Link(url, label));
    }

    // Convert the Vector of GemtextElements to an array
    private static GemtextElement[] convertVectorToArray(Vector doc) {
        GemtextElement[] result = new GemtextElement[doc.size()];
        for (int i = 0; i < doc.size(); i++) {
            result[i] = (GemtextElement) doc.elementAt(i);
        }
        return result;
    }

    // Custom split function to split a string by a delimiter with a limit
    private static String[] customSplit(String input, String delimiter, int limit) {
        Vector result = new Vector();  // Raw type without generics
        int count = 0;
        int startIndex = 0;
        int endIndex;

        while ((endIndex = input.indexOf(delimiter, startIndex)) != -1 && count < limit - 1) {
            result.addElement(input.substring(startIndex, endIndex));
            startIndex = endIndex + delimiter.length();
            count++;
        }

        // Add the remaining part of the string
        result.addElement(input.substring(startIndex));

        // Convert the result Vector to a String[] array
        String[] outputArray = new String[result.size()];
        result.copyInto(outputArray);

        return outputArray;
    }
}
