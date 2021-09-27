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
                                "import kaufland.com.coachbasebinderapi.Fields;\n" +
                                "import java.io.InputStream;\n" +
                                "\n" +
                                "@Entity(\n" +
                                "        database = \"mydb_db\"\n" +
                                ")\n" +
                                "@Fields({@Field(\n" +
                                "        defaultValue = \"product\",\n" +
                                "        type = String.class,\n" +
                                "        name = \"type\",\n" +
                                "        readonly = true\n" +
                                "), @Field(\n" +
                                "        type = String.class,\n" +
                                "        name = \"name\"\n" +
                                "), @Field(\n" +
                                "        list = true,\n" +
                                "        type = String.class,\n" +
                                "        name = \"comments\"\n" +
                                ")" +
                                ", @Field(\n"+
                                "         type = Boolean.class,\n" +
                                "                       name = \"bool\"\n" +
                                "                                 )" +
                                ", @Field(\n" +
                                "        list = true,\n" +
                                "        type = String.class,\n" +
                                "        name = \"identifiers\"\n" +
                                ")})" +
                                "public class ListTest {\n" +
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
                "import kaufland.com.coachbasebinderapi.Fields;\n" +
                "import java.io.InputStream;\n" +
                "\n" +
                "@Entity\n" +
                "@Fields({@Field(\n" +
                "        type = com.kaufland.testModels.Sub.class,\n" +
                "        name = \"list_sub\",\n" +
                "        list = true\n" +
                "), @Field(\n" +
                "        type = Sub.class,\n" +
                "        name = \"sub\"\n" +
                ")})" +
                "public class ListTest {\n" +
                "}");
        JavaFileObject subEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.Sub", "package com.kaufland.testModels;\n" +
                "\n" +
                "import kaufland.com.coachbasebinderapi.MapWrapper;\n" +
                "import kaufland.com.coachbasebinderapi.Field;\n" +
                "import kaufland.com.coachbasebinderapi.Fields;\n" +
                "\n" +
                "/**\n" +
                " * Created by sbra0902 on 31.05.17.\n" +
                " */\n" +
                "@MapWrapper\n" +
                "@Fields({@Field(\n" +
                "        type = String.class,\n" +
                "        name = \"test\"\n" +
                ")})" +
                "public class Sub {\n" +
                "}");
        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(mMainEntity, subEntity);

        Assert.assertEquals(compilation.status(), Compilation.Status.SUCCESS);
    }

    @Test
    public void testFailContructorAndPublicFieldProcessing() {

        JavaFileObject subEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.Sub", "package com.kaufland.testModels;\n" +
                "\n" +
                "import kaufland.com.coachbasebinderapi.Entity;\n" +
                "import kaufland.com.coachbasebinderapi.Field;\n" +
                "import kaufland.com.coachbasebinderapi.Fields;\n" +
                "\n" +
                "/**\n" +
                " * Created by sbra0902 on 31.05.17.\n" +
                " */\n" +
                "@Entity\n" +
                "@Fields({@Field(\n" +
                "        type = String.class,\n" +
                "        name = \"test\"\n" +
                "), @Field(\n" +
                "        type = String.class,\n" +
                "        name = \"type\",\n" +
                "        defaultValue = \"product\",\n"+
                "        readonly= true\n"+
                ")})" +
                "public class Sub {\n" +
                "\n" +
                " public Sub(String test){\n" +
                " }\n" +
                "}");


        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(subEntity);


        Assert.assertEquals(compilation.status(), Compilation.Status.FAILURE);

        Assert.assertTrue(compilation.diagnostics().stream().anyMatch(diagnostic -> diagnostic.getMessage(Locale.GERMAN).equals("Entity should not have a contructor")));
    }



}


