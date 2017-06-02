package com.kaufland;


import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import com.kaufland.CoachBaseBinderProcessor;
import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.Compiler.javac;


import org.junit.Test;

/**
 * Created by sbra0902 on 02.06.17.
 */

public class CoachBaseBinderProcessorTest{

    @Test
    public void testProcessing(){

        Compilation compilation =
    javac()
                         .withProcessors(new CoachBaseBinderProcessor())
                         .compile(JavaFileObjects.forSourceString("com.kaufland.testModels.ListTest", "package com.kaufland.testModels;\n" +
                                 "\n" +
                                 "import java.util.ArrayList;\n" +
                                 "\n" +
                                 "import kaufland.com.coachbasebinderapi.CblEntity;\n" +
                                 "import kaufland.com.coachbasebinderapi.CblField;\n" +
                                 "\n" +
                                 "/**\n" +
                                 " * Created by sbra0902 on 02.06.17.\n" +
                                 " */\n" +
                                 "@CblEntity\n" +
                                 "public class ListTest {\n" +
                                 "\n" +
                                 "\n" +
                                 "    @CblField\n" +
                                 "    String type;\n" +
                                 "\n" +
                                 "    @CblField(\"title\")\n" +
                                 "    String title;\n" +
                                 "\n" +
                                 "    @CblField(\"created_at\")\n" +
                                 "    String createdAt;\n" +
                                 "\n" +
                                 "    @CblField(\"members\")\n" +
                                 "    ArrayList<String> members;\n" +
                                 "\n" +
                                 "    @CblField(\"owner\")\n" +
                                 "    String owner;\n" +
                                 "\n" +
                                 "    @CblField(\"sub\")\n" +
                                 "    SubTest sub;\n" +
                                 "\n" +
                                 "    @CblField(\"list_sub\")\n" +
                                 "    java.util.List<SubTest> listSub;\n" +
                                 "\n" +
                                 "\n" +
                                 "}"));
    }
}
