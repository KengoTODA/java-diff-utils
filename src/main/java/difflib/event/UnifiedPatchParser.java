package difflib.event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import difflib.DiffUtils;
import difflib.Patch;

public class UnifiedPatchParser {

    void parse(File unifiedPatch, @Nonnull PatchHandler<String> handler) throws FileNotFoundException, IOException {
        try (BufferedReader reader = Files.newReader(unifiedPatch, Charsets.UTF_8)) {
            String line = reader.readLine();
            do {
                List<String> patch = Lists.newArrayList();
                while (!line.startsWith("---")) {
                    // in prelude: skip all lines
                    line = reader.readLine();
                }
                assert line.startsWith("---");
                patch.add(line);
                final String originalFileName = line.substring(4);
    
                line = reader.readLine();
                assert line.startsWith("+++");
                patch.add(line);
                final String revisedFileName = line.substring(4);
    
                line = reader.readLine();
                do {
                    patch.add(line);
                    line = reader.readLine();
                } while (
                        line != null &&
                        (line.startsWith("@@ ") || line.startsWith(" ") || line.startsWith("+") || line.startsWith("-")) // TODO this implementation expects that we have comment between 2 diffs. 
                        );

                Patch<String> parsed = DiffUtils.parseUnifiedDiff(patch);
                handler.handle(originalFileName, revisedFileName, parsed);
            } while (line != null);
        }
    }
}
