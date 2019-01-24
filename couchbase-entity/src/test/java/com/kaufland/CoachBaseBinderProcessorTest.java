package com.kaufland;


import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

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
                                "import kaufland.com.coachbasebinderapi.Entity;\n" +
                                "import kaufland.com.coachbasebinderapi.Field;\n" +
                                "import kaufland.com.coachbasebinderapi.Constant;\n" +
                                "import kaufland.com.coachbasebinderapi.Default;\n" +
                                "import java.io.InputStream;\n" +
                                "\n" +
                                "@Entity\n" +
                                "public class ListTest {\n" +
                                "\n" +
                                "\n" +
                                "    @Field(\"title\")\n" +
                                "    private String title;\n" +
                                "\n" +
                                "\n" +
                                "    @Field(\"count\")\n" +
                                "    private Integer count;\n" +
                                "\n" +
                                "    @Constant(value = \"type\", constant = \"List\")\n" +
                                "    private String type;\n" +
                                "\n" +
                                "    @Field(\"created_at\")\n" +
                                "    @Default(\"1970\")\n" +
                                "    private String createdAt;\n" +
                                "\n" +
                                "    @Field(\"members\")\n" +
                                "    private ArrayList<String> members;\n" +
                                "\n" +
                                "    @Field(\"owner\")\n" +
                                "    private String owner;\n" +
                                "    @Field(value = \"image\")\n" +
                                "    private InputStream image;" +
                                "\n" +
                                "\n" +
                                "}"));

        if(compilation.status() == Compilation.Status.FAILURE){
            Diagnostic<? extends JavaFileObject> diagnostic = compilation.diagnostics().get(0);
            Assert.fail(diagnostic.getMessage(Locale.GERMAN));
            return;
        }
        Assert.assertEquals(compilation.status(), Compilation.Status.SUCCESS);
    }

    @Test
    public void testSuccessSubEntityProcessing() {

        JavaFileObject mMainEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.ListTest", "package com.kaufland.testModels;\n" +
                "\n" +
                "import java.util.ArrayList;\n" +
                "import kaufland.com.coachbasebinderapi.Entity;\n" +
                "import kaufland.com.coachbasebinderapi.Field;\n" +
                "import java.io.InputStream;\n" +
                "\n" +
                "@Entity\n" +
                "public class ListTest {\n" +
                "\n" +
                "    @Field(\"list_sub\")\n" +
                "    private ArrayList<com.kaufland.testModels.Sub> listSub;\n" +
                "    @Field(\"sub\")\n" +
                "    private Sub sub;" +
                "}");
        JavaFileObject subEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.Sub", "package com.kaufland.testModels;\n" +
                "\n" +
                "import kaufland.com.coachbasebinderapi.MapWrapper;\n" +
                "import kaufland.com.coachbasebinderapi.Field;\n" +
                "\n" +
                "/**\n" +
                " * Created by sbra0902 on 31.05.17.\n" +
                " */\n" +
                "@MapWrapper\n" +
                "public class Sub {\n" +
                "\n" +
                "    @Field\n" +
                "    private String test;\n" +
                "\n" +
                "}");
        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(mMainEntity, subEntity);

        Assert.assertEquals(compilation.status(), Compilation.Status.SUCCESS);
    }

    @Test
    public void testWrongAssignedSubEntityProcessing() {

        JavaFileObject mMainEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.ListTest", "package com.kaufland.testModels;\n" +
                "\n" +
                "import java.util.ArrayList;\n" +
                "import kaufland.com.coachbasebinderapi.Entity;\n" +
                "import kaufland.com.coachbasebinderapi.Field;\n" +
                "import java.io.InputStream;\n" +
                "\n" +
                "@Entity\n" +
                "public class ListTest {\n" +
                "\n" +
                "    @Field(\"list_sub\")\n" +
                "    private ArrayList<com.kaufland.testModels.Sub> listSub;\n" +
                "    @Field(\"sub\")\n" +
                "    private Sub sub;" +
                "}");
        JavaFileObject subEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.Sub", "package com.kaufland.testModels;\n" +
                "\n" +
                "import kaufland.com.coachbasebinderapi.Entity;\n" +
                "import kaufland.com.coachbasebinderapi.Field;\n" +
                "\n" +
                "/**\n" +
                " * Created by sbra0902 on 31.05.17.\n" +
                " */\n" +
                "@Entity\n" +
                "public class Sub {\n" +
                "\n" +
                "    @Field\n" +
                "    private String test;\n" +
                "\n" +
                "}");
        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(mMainEntity, subEntity);

        Assert.assertEquals(compilation.status(), Compilation.Status.FAILURE);
        Assert.assertEquals(compilation.diagnostics().size(), 2);
        Assert.assertEquals(compilation.diagnostics().get(0).getMessage(Locale.GERMAN), "Entity not valid to use as member needs to be MapWrapper");
        Assert.assertEquals(compilation.diagnostics().get(1).getMessage(Locale.GERMAN), "Entity not valid to use as member needs to be MapWrapper");
    }

    @Test
    public void testFailContructorAndPublicFieldProcessing() {

        JavaFileObject subEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.Sub", "package com.kaufland.testModels;\n" +
                "\n" +
                "import kaufland.com.coachbasebinderapi.Entity;\n" +
                "import kaufland.com.coachbasebinderapi.Field;\n" +
                "import kaufland.com.coachbasebinderapi.Constant;\n" +
                "\n" +
                "/**\n" +
                " * Created by sbra0902 on 31.05.17.\n" +
                " */\n" +
                "@Entity\n" +
                "public class Sub {\n" +
                "\n" +
                " public Sub(String test){\n" +
                " }\n" +
                "\n" +
                "    @Field\n" +
                "    public String test;\n" +
                "    @Constant(value = \"type\", constant = \"product\")\n" +
                "    public String test2;\n" +
                "\n" +
                "}");


        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(subEntity);


        Assert.assertEquals(compilation.status(), Compilation.Status.FAILURE);
        Assert.assertEquals(compilation.diagnostics().size(), 3);
        Assert.assertEquals(compilation.diagnostics().get(0).getMessage(Locale.GERMAN), "Entity should not have a contructor");
        Assert.assertEquals(compilation.diagnostics().get(1).getMessage(Locale.GERMAN), "Field must be private");
        Assert.assertEquals(compilation.diagnostics().get(2).getMessage(Locale.GERMAN), "Constant must be private");
    }

    @Test
    public void testFailPrivateInnerClass() {

        JavaFileObject subEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.Sub", "package com.kaufland.testModels;\n" +
                "\n" +
                "import kaufland.com.coachbasebinderapi.Entity;\n" +
                "import kaufland.com.coachbasebinderapi.Field;\n" +
                "\n" +
                "/**\n" +
                " * Created by sbra0902 on 31.05.17.\n" +
                " */\n" +
                "@Entity\n" +
                "public class Sub {\n" +
                "\n" +
                "    @Field\n" +
                "    private String test;\n" +
                "\n" +
                "@Entity\n" +
                "private class Bad {}\n" +
                "}");


        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(subEntity);


        Assert.assertEquals(compilation.status(), Compilation.Status.FAILURE);
        Assert.assertEquals(compilation.diagnostics().size(), 1);
        Assert.assertEquals(compilation.diagnostics().get(0).getMessage(Locale.GERMAN), "Entity can not be private");
    }

    @Test
    public void testFailFinalInnerClass() {

        JavaFileObject subEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.Sub", "package com.kaufland.testModels;\n" +
                "\n" +
                "import kaufland.com.coachbasebinderapi.Entity;\n" +
                "import kaufland.com.coachbasebinderapi.Field;\n" +
                "\n" +
                "/**\n" +
                " * Created by sbra0902 on 31.05.17.\n" +
                " */\n" +
                "@Entity\n" +
                "public class Sub {\n" +
                "\n" +
                "    @Field\n" +
                "    private String test;\n" +
                "\n" +
                "@Entity\n" +
                "public final class Bad {}\n" +
                "}");


        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(subEntity);


        Assert.assertEquals(compilation.status(), Compilation.Status.FAILURE);
        Assert.assertEquals(compilation.diagnostics().size(), 1);
        Assert.assertEquals(compilation.diagnostics().get(0).getMessage(Locale.GERMAN), "Entity can not be final");
    }

    @Test
    public void testFieldAndConstantSameTime() {

        JavaFileObject subEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.Sub", "package com.kaufland.testModels;\n" +
                "\n" +
                "import kaufland.com.coachbasebinderapi.Entity;\n" +
                "import kaufland.com.coachbasebinderapi.Field;\n" +
                "import kaufland.com.coachbasebinderapi.Constant;\n" +
                "\n" +
                "/**\n" +
                " * Created by sbra0902 on 31.05.17.\n" +
                " */\n" +
                "@Entity\n" +
                "public class Sub {\n" +
                "\n" +
                "    @Field\n" +
                "    @Constant(value = \"type\", constant = \"product\")\n" +
                "    private String test;\n" +
                "\n" +
                "}");


        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(subEntity);


        Assert.assertEquals(compilation.status(), Compilation.Status.FAILURE);
        Assert.assertEquals(compilation.diagnostics().size(), 1);
        Assert.assertEquals(compilation.diagnostics().get(0).getMessage(Locale.GERMAN), "Element can´t be Field and Constant at the same time");
    }



}


