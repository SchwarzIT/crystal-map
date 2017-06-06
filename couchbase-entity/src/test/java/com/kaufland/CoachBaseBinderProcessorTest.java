package com.kaufland;


import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Assert;
import org.junit.Test;

import static com.google.testing.compile.Compiler.javac;

public class CoachBaseBinderProcessorTest {

    @Test
    public void testSuccessProcessing() {

        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(JavaFileObjects.forSourceString("com.kaufland.testModels.ListTest", "package com.kaufland.testModels;\n" +
                                "\n" +
                                "import java.util.ArrayList;\n" +
                                "import kaufland.com.coachbasebinderapi.CblEntity;\n" +
                                "import kaufland.com.coachbasebinderapi.CblField;\n" +
                                "import java.io.InputStream;\n" +
                                "\n" +
                                "@CblEntity\n" +
                                "public class ListTest {\n" +
                                "\n" +
                                "\n" +
                                "    @CblField\n" +
                                "    private String type;\n" +
                                "\n" +
                                "    @CblField(\"title\")\n" +
                                "    private String title;\n" +
                                "\n" +
                                "    @CblField(\"created_at\")\n" +
                                "    private String createdAt;\n" +
                                "\n" +
                                "    @CblField(\"members\")\n" +
                                "    private ArrayList<String> members;\n" +
                                "\n" +
                                "    @CblField(\"owner\")\n" +
                                "    private String owner;\n" +
                                "    @CblField(value = \"image\", attachmentType = \"image/jpg\")\n" +
                                "    private InputStream image;" +
                                "\n" +
                                "\n" +
                                "}"));

        Assert.assertEquals(compilation.status(), Compilation.Status.SUCCESS);
    }
}
