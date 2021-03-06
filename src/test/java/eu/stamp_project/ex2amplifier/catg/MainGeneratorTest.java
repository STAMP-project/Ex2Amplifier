package eu.stamp_project.ex2amplifier.catg;

import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.ex2amplifier.AbstractTest;
import org.junit.Test;
import spoon.SpoonModelBuilder;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/01/18
 */
public class MainGeneratorTest extends AbstractTest {

    @Override
    protected String getPathToConfigurationFile() {
        return "src/test/resources/tavern/tavern.properties";
    }

    @Test
    public void test() throws Exception {

        /*
            The MainGenerator should return a main method (std java) build from the given test case.
            The returned main method is the same as the test case, but literals has been extracted has local variables
            and initialize with a method call from CATG: catg.CATG.makeXXX(<originalValue>)

            The produced main method must be compilable and runnable
            The test class is also modified to support JUnit4 features: @BeforeClass, @Before, @After, @AfterClass etc..
         */

        final CtClass<Object> testClass = this.launcher.getFactory().Class().get("fr.inria.stamp.MainTest");
        final CtMethod<?> mainMethodFromTestMethod = MainGenerator.generateMainMethodFromTestMethod(
                testClass.getMethodsByName("test").get(0) , testClass
        );

        System.out.println(mainMethodFromTestMethod.toString());
        testClass.addMethod(mainMethodFromTestMethod);
        this.launcher.getModelBuilder().setBinaryOutputDirectory(new File("target/trash/"));
        this.launcher.getModelBuilder().compile(SpoonModelBuilder.InputType.CTTYPES);

        final String expectedMainMethod = "public static void main(String[] args) {" + AmplificationHelper.LINE_SEPARATOR + 
                "    try {" + AmplificationHelper.LINE_SEPARATOR +
                "        MainTest.setUpBeforeClass();" + AmplificationHelper.LINE_SEPARATOR +
                "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR +
                "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "    MainTest mainTest = new MainTest();" + AmplificationHelper.LINE_SEPARATOR +
                "    try {" + AmplificationHelper.LINE_SEPARATOR + 
                "        mainTest.setUp();" + AmplificationHelper.LINE_SEPARATOR + 
                "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR + 
                "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR + 
                "    }" + AmplificationHelper.LINE_SEPARATOR + 
                "    try {" + AmplificationHelper.LINE_SEPARATOR + 
                "        String lit1 = catg.CATG.readString((String)\"\\\"bar\\\"\");" + AmplificationHelper.LINE_SEPARATOR + 
                "        String lit2 = catg.CATG.readString((String)\"NEW\" + System.getProperty(\"line.separator\") + \"LINE\");" + AmplificationHelper.LINE_SEPARATOR + 
                "        boolean lit3 = catg.CATG.readBool((boolean)true);" + AmplificationHelper.LINE_SEPARATOR + 
                "        char lit4 = catg.CATG.readChar((char)'<');" + AmplificationHelper.LINE_SEPARATOR + 
                "        char lit5 = catg.CATG.readChar((char)'\\'');" + AmplificationHelper.LINE_SEPARATOR + 
                "        byte lit6 = catg.CATG.readByte((byte)3);" + AmplificationHelper.LINE_SEPARATOR + 
                "        short lit7 = catg.CATG.readShort((short)3);" + AmplificationHelper.LINE_SEPARATOR + 
                "        int lit8 = catg.CATG.readInt((int)3);" + AmplificationHelper.LINE_SEPARATOR + 
                "        long lit9 = catg.CATG.readLong((long)3);" + AmplificationHelper.LINE_SEPARATOR + 
                "        byte lit10 = catg.CATG.readByte((byte)16);" + AmplificationHelper.LINE_SEPARATOR + 
                "        int lit11 = catg.CATG.readInt((int)100);" + AmplificationHelper.LINE_SEPARATOR + 
                "        String lit12 = catg.CATG.readString((String)\"Potion\");" + AmplificationHelper.LINE_SEPARATOR + 
                "        int lit13 = catg.CATG.readInt((int)5);" + AmplificationHelper.LINE_SEPARATOR + 
                "        String lit14 = catg.CATG.readString((String)\"Timoleon\");" + AmplificationHelper.LINE_SEPARATOR + 
                "        int lit15 = catg.CATG.readInt((int)1000);" + AmplificationHelper.LINE_SEPARATOR + 
                "        String lit16 = catg.CATG.readString((String)\"Potion\");" + AmplificationHelper.LINE_SEPARATOR + 
                "        System.out.println(mainTest.aUsedNumber);" + AmplificationHelper.LINE_SEPARATOR + 
                "        System.out.println(mainTest.getANumber());" + AmplificationHelper.LINE_SEPARATOR + 
                "        System.out.println(lit1);" + AmplificationHelper.LINE_SEPARATOR + 
                "        System.out.println(lit2);" + AmplificationHelper.LINE_SEPARATOR + 
                "        System.out.println(lit3);" + AmplificationHelper.LINE_SEPARATOR + 
                "        System.out.println(lit4);" + AmplificationHelper.LINE_SEPARATOR + 
                "        System.out.println(lit5);" + AmplificationHelper.LINE_SEPARATOR + 
                "        byte b = lit6;" + AmplificationHelper.LINE_SEPARATOR + 
                "        short s = lit7;" + AmplificationHelper.LINE_SEPARATOR + 
                "        int i = lit8;" + AmplificationHelper.LINE_SEPARATOR + 
                "        long l = lit9;" + AmplificationHelper.LINE_SEPARATOR + 
                "        byte[] array_byte = new byte[]{ lit10 };" + AmplificationHelper.LINE_SEPARATOR + 
                "        Integer toto = null;" + AmplificationHelper.LINE_SEPARATOR + 
                "        Seller seller = new Seller(lit11, Collections.singletonList(new Item(lit12, lit13)));" + AmplificationHelper.LINE_SEPARATOR + 
                "        Player player = new Player(lit14, lit15);" + AmplificationHelper.LINE_SEPARATOR + 
                "        player.toString();" + AmplificationHelper.LINE_SEPARATOR + 
                "        seller.toString();" + AmplificationHelper.LINE_SEPARATOR + 
                "        player.buyItem(lit16, seller);" + AmplificationHelper.LINE_SEPARATOR + 
                "        player.toString();" + AmplificationHelper.LINE_SEPARATOR + 
                "        seller.toString();" + AmplificationHelper.LINE_SEPARATOR + 
                "    } catch (Exception __exceptionEx2AmplifierException) {" + AmplificationHelper.LINE_SEPARATOR + 
                "        throw new RuntimeException(__exceptionEx2AmplifierException);" + AmplificationHelper.LINE_SEPARATOR + 
                "    }" + AmplificationHelper.LINE_SEPARATOR + 
                "    try {" + AmplificationHelper.LINE_SEPARATOR + 
                "        mainTest.tearDown();" + AmplificationHelper.LINE_SEPARATOR + 
                "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR + 
                "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR + 
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "    try {" + AmplificationHelper.LINE_SEPARATOR +
                "        MainTest.tearDownAfterClass();" + AmplificationHelper.LINE_SEPARATOR +
                "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR +
                "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMainMethod, mainMethodFromTestMethod.toString());
    }
}
