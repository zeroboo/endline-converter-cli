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
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 *
 * @author pthung
 */
public class TestListFile {

    static final String TEST_ROOT_FOLDER = ".testData";
    static Logger logger = LoggerFactory.getLogger(TestListFile.class);

    public TestListFile() {
    }

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

            String[] subFolders = new String[]{"folderA", "folderB", "folderC"};
            for (String folder : subFolders) {
                Path newFolder = root.resolve(folder);
                Files.createDirectories(newFolder);
                logger.info("Test folder created: {}", newFolder);
                
                for (int i = 0; i < 5; i++) {
                    Path newFile = newFolder.resolve("file_" + i + (i%2==0?".txt":".java"));
                    
                    OutputStream os = Files.newOutputStream(newFile, StandardOpenOption.CREATE_NEW);
                    os.write(String.format("test %d", i).getBytes(Charset.forName("UTF8")));
                    os.close();
                    
                    
                    Path newFile2 = newFolder.resolve("file_" + Character.toString((char) ('c' + (char)i)) + (i%2==0?".txt":".jar"));
                    
                    OutputStream os2 = Files.newOutputStream(newFile2, StandardOpenOption.CREATE_NEW);
                    os2.write(String.format("test %d", i).getBytes(Charset.forName("UTF8")));
                    os2.close();
                    
                    logger.info("Test file created: {}", newFile);
                }
            }
            Path newFile = root.resolve("file_0.txt");
            OutputStream os = Files.newOutputStream(newFile, StandardOpenOption.CREATE_NEW);
            os.write(String.format("test 00000").getBytes(Charset.forName("UTF8")));
            os.close();

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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testListFile_NoInclude_GetSubFolders_CorrectResult() {
        try {
            logger.info("testListFile_NoInclude_GetSubFolders_CorrectResult");
            String[] allFiles = Main.listFile(TEST_ROOT_FOLDER, "");
            logger.info("List file result: {}", Arrays.toString(allFiles));
            assertThat(allFiles, Matchers.arrayContainingInAnyOrder(".testData\\file_0.txt"
                , ".testData\\folderA"
                , ".testData\\folderB"
                , ".testData\\folderC")
            ); // for collections
            
        } catch (IOException ex) {
            logger.error("", ex);
            fail("Has exception");
        }
    }
    @Test
    public void testListFile_NoInclude_GetSubFileFiles_CorrectResult() {
        try {
            logger.info("testListFile_NoInclude_GetSubFileFiles_CorrectResult");
            String testFolder = TEST_ROOT_FOLDER + "/folderA";
            String[] allFiles = Main.listFile(testFolder, "");
            logger.info("List file result: {}", Arrays.toString(allFiles));
            assertThat(allFiles, Matchers.arrayContainingInAnyOrder(
                ".testData\\folderA\\file_0.txt"
                , ".testData\\folderA\\file_1.java"
                , ".testData\\folderA\\file_2.txt"
                , ".testData\\folderA\\file_3.java"
                , ".testData\\folderA\\file_4.txt"
                , ".testData\\folderA\\file_c.txt"
                , ".testData\\folderA\\file_d.jar"
                , ".testData\\folderA\\file_e.txt"
                , ".testData\\folderA\\file_f.jar"
                , ".testData\\folderA\\file_g.txt"
            )
            ); // for collections
            
        } catch (IOException ex) {
            logger.error("", ex);
            fail("Has exception");
        }
    }
    @Test
    public void testListFile_WithInclude_GetSubFileFiles_CorrectResult() {
        try {
            logger.info("testListFile_WithInclude_GetSubFileFiles_CorrectResult");
            String testFolder = TEST_ROOT_FOLDER + "/folderA";
            String[] allFiles = Main.listFile(testFolder, "*.jar");
            logger.info("List file result: {}", Arrays.toString(allFiles));
            assertThat(allFiles, Matchers.arrayContainingInAnyOrder(
                ".testData/folderA\\file_d.jar"
                , ".testData/folderA\\file_f.jar"
            )
            ); // for collections
            
        } catch (IOException ex) {
            logger.error("", ex);
            fail("Has exception");
        }
    }
}
