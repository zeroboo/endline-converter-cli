/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package endlineconverter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.maven.plugins.assembly.utils.LineEndings;
import org.apache.tools.ant.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.maven.plugins.assembly.utils.LineEndingsUtils;

/**
 *
 * @author pthung
 */
public class Main {

    static Logger staticLogger = LoggerFactory.getLogger(Main.class);

    static enum Command {
        Help
    }

    static Options options;
    static String FILE_PATH_SEPARATOR = System.getProperty("file.separator");
    static Charset DEFAULT_CHARSET = Charset.forName("utf-8");
    public static void main(String[] agrs) {
        staticLogger.info("LineEndingConverter cli: started, arguments={}", Arrays.toString(agrs));
        staticLogger.info("Current working directory: {}", System.getProperty("user.dir"));
        staticLogger.info("File separator: OS={} {}, Line separator={}", System.getProperty("os.name"), System.getProperty("os.version"), FILE_PATH_SEPARATOR);
        options = createProgramOptions();

//        DirectoryScanner dirScanner = new DirectoryScanner();
//        dirScanner.scan();
//        dirScanner.getIncludedFiles();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdOptions = parser.parse(options, agrs);

            String formatOption = cmdOptions.getOptionValue("f");
            String encodingOption = cmdOptions.getOptionValue("e");
            String inputFolder = cmdOptions.getOptionValue("in");
            String outputFolder = cmdOptions.getOptionValue("out");
            Charset charset;
            try {
                charset = Charset.forName(encodingOption);
            } catch (IllegalCharsetNameException ex) {
                charset = null;
                staticLogger.info("Invalid encoding {}, default encoding is used: {}", encodingOption, charset);
            }

            staticLogger.info("Arguments: format={}, encoding={}, argument={}", formatOption, encodingOption, cmdOptions.getArgList());
            if (formatOption != null && !formatOption.isEmpty()) {
                LineEndings format = LineEndings.valueOf(formatOption.toLowerCase());
                if (formatOption == null || formatOption.isEmpty()) {
                    showHelp();
                } else {
                    ///Do converting
                    Path outputPath = Paths.get(outputFolder);
                    if(!Files.exists(outputPath)){
                        Files.createDirectories(outputPath);
                    }
                    convert(inputFolder, outputFolder, format, charset!=null?charset.toString():null);
                }
            } else {
                throw new InvalidParameterException("Invalid format `" + formatOption + "`");
            }
        } catch (Exception ex) {
            // automatically generate the help statement
            staticLogger.debug("Exception, params={}", Arrays.toString(agrs), ex);
            staticLogger.error("Invalid arguments: " + ex.getMessage());
            showHelp();
        }
        ///DefaultSshClient client = new DefaultSshClient(ip, 0, username, password);
    }

    public static String[] listFile(String dirToScan, String includePatterns) throws IOException {
        staticLogger.debug("listFile: dirToScan={}, include={}", dirToScan, includePatterns);
        String result[] = null;
        if (includePatterns != null && !includePatterns.isEmpty()) {
            DirectoryScanner ds = new DirectoryScanner();
            String[] includes = {includePatterns};
            //String[] excludes = {"modules\\*\\**"};
            ds.setIncludes(includes);
            //ds.setExcludes(excludes);
            ds.setBasedir(dirToScan);
            //ds.setCaseSensitive(true);
            ds.scan();

            result = ds.getIncludedFiles();
            for (int i = 0; i < result.length; i++) {
                result[i] = dirToScan + FILE_PATH_SEPARATOR + result[i];
            }

        } else {
            return Files.list(Paths.get(dirToScan)).map(p -> p.toString()).toArray(String[]::new);
        }

        return result;
    }
    public static int convertFile(String targetFile, String outputFolder, LineEndings format) {
        return convertFile(targetFile, outputFolder, format, DEFAULT_CHARSET.toString());
    }
    public static int convertFile(String targetFile, String outputFolder, LineEndings format, String encoding) {
        File inputFile = new File(targetFile);
        int convertedFiles = 0;
        if (inputFile.isDirectory()) {
            staticLogger.info("Folder: {}, sub={}", targetFile, inputFile.list());
            for (String subFile : inputFile.list()) {
                String filePath = targetFile + FILE_PATH_SEPARATOR + subFile;
                convertedFiles += convertFile(filePath, outputFolder, format, encoding);
            }

        } else {
            try {
                Path outputPath = Paths.get(outputFolder).resolve(targetFile);
                if(!Files.exists(outputPath.getParent())){
                    Files.createDirectories(outputPath.getParent());
                }
                File outputFile = new File(outputPath.toString()) ;
                LineEndingsUtils.convertLineEndings(inputFile, outputFile, format, Boolean.TRUE, encoding);
                staticLogger.info("Converted file: {}", targetFile);
                convertedFiles = 1;
                
            } catch (IOException ex) {
                staticLogger.error("Fail to convert file {}, format {}, encoding {}", targetFile, format.toString(), encoding, ex);
                convertedFiles = 0;
            }

        }
        return convertedFiles;
    }

    public static void convert(String input, String outputFolder, LineEndings format, String encoding) throws IOException {
        Path targetPath = Paths.get(input);
        boolean isFolder = Files.isDirectory(targetPath);

        staticLogger.info("Start converting {}, input={}, output={}", input, isFolder);
        try {
            List<Path> allFiles = new ArrayList<>();
            Files.walk(targetPath)
                    .filter(Files::isRegularFile)
                    .forEach(allFiles::add);
            staticLogger.info("Total files: total={}", allFiles.size());
            convertFile(input, outputFolder, format, encoding);
        } catch (Exception ex) {
            staticLogger.error("Error ", ex);
        }

        staticLogger.info("Finish converting {}", input);
    }

    public static Options createProgramOptions() {
        // create Options object
        Options newOptions = new Options();
        newOptions.addOption("f", "format", true, "Format to convert to: " + Arrays.stream(LineEndings.values()).map(c -> c.name()).collect(Collectors.joining(", ")));
        
        newOptions.addOption("in", "input", true, "Input folder or file to convert");
        newOptions.addOption("out", "output", true, "Output folder or file to convert");

        newOptions.addOption("fi", "include", true, "File include pattern");
        newOptions.addOption("fe", "exclude", true, "File exclude pattern");
        newOptions.addOption("e", "encoding", true, "Encoding for output files, default is utf-8");

        return newOptions;
    }

    public static void showHelp() {
        if (options != null) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Usage: endlineConverter [Options] <Target> <Target2> ...", options);

        } else {
            staticLogger.error("Show help: null option");
        }
    }
}
