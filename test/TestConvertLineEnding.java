/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import endlineconverter.Main;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugins.assembly.utils.LineEndings;
import org.apache.maven.plugins.assembly.utils.LineEndingsUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pthung
 */
public class TestConvertLineEnding {
    
    public TestConvertLineEnding() {
    
    }
    static final String TEST_ROOT_FOLDER = ".testConvertData";
    static Logger logger = LoggerFactory.getLogger(TestConvertLineEnding.class);

    @BeforeClass
    public static void setUpClass() {
        logger.info("setUpClass");
        Path root = Paths.get(TEST_ROOT_FOLDER);

        if (Files.exists(root)) {
            logger.info("Test folder existed: {}", TEST_ROOT_FOLDER);
            try {
                FileUtils.deleteDirectory(new File(TEST_ROOT_FOLDER));
                logger.info("Test folder deleted: {}", TEST_ROOT_FOLDER);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(TestListFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            Files.createDirectories(root);
            logger.info("Test root folder created: {}", TEST_ROOT_FOLDER);

            PrintStream ps = new PrintStream(Files.newOutputStream(root.resolve("windows.txt")));
            ps.print("line1\r\n");
            ps.print("line2\r\n");
            ps.print("line3\r\n");
            ps.close();
            
            ps = new PrintStream(Files.newOutputStream(root.resolve("linux.txt")));
            ps.print("line1\n");
            ps.print("line2\n");
            ps.print("line3\n");
            ps.close();
            

        } catch (IOException ex) {
            logger.error("Create test folder failed: {}", TEST_ROOT_FOLDER, ex);
        }
    }
    
    @AfterClass
    public static void tearDownClass() {

    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConvert_WindowsFile(){
        String target = TEST_ROOT_FOLDER + "/windows.txt";
        LineEndings format = LineEndings.windows;
        Main.convertFile(target, "out", format);
    
    }
}
