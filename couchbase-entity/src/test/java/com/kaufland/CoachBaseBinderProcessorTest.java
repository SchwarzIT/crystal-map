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
                                "import kaufland.com.coachbasebinderapi.Constant;\n" +
                                "import kaufland.com.coachbasebinderapi.Default;\n" +
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
                                "), @Field(\n" +
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
    public void testKotlinSuccessProcessing() {

        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(JavaFileObjects.forSourceString("schwarz.fwws.shared.model.ListTest", "package schwarz.fwws.shared.model\n" +
                                "\n" +
                                "\n" +
                                "import kaufland.com.coachbasebinderapi.Field\n" +
                                "import kaufland.com.coachbasebinderapi.MapWrapper\n" +
                                "import java.util.*\n" +
                                "\n" +
                                "@MapWrapper\n" +
                                "open class Price: Model {\n" +
                                "    companion object {\n" +
                                "        const val TYPE: String = \"Price\"\n" +
                                "        const val PREFIX: String = \"price\"\n" +
                                "\n" +
                                "        fun documentId(storeId: String, articleNo: String, uuid: String): String {\n" +
                                "            return \"$PREFIX:$storeId:$articleNo:$uuid\"\n" +
                                "        }\n" +
                                "    }\n" +
                                "\n" +
                                "    @Field(\"storeId\")\n" +
                                "    @JvmField\n" +
                                "private final var storeId: String = \"\"\n" +
                                "    @Field(\"article_no\")\n" +
                                "    @JvmField\n" +
                                "private final var article_no: String = \"\"\n" +
                                "    @Field(\"type\")\n" +
                                "    @JvmField\n" +
                                "private final var type: String = TYPE\n" +
                                "    @Field(\"condition_no\")\n" +
                                "    @JvmField\n" +
                                "private final var condition_no: String? = null\n" +
                                "    @Field(\"start_date\")\n" +
                                "    @JvmField\n" +
                                "private final var start_date: Date? = null\n" +
                                "    @Field(\"end_date\")\n" +
                                "    @JvmField\n" +
                                "private final var end_date: Date? = null\n" +
                                "    @Field(\"sales_price\")\n" +
                                "    @JvmField\n" +
                                "private final var sales_price: String? = null\n" +
                                "    @Field(\"currency_unit\")\n" +
                                "    @JvmField\n" +
                                "private final var currency_unit: String? = null\n" +
                                "\n" +
                                "    override fun documentId(): String {\n" +
                                "        return Companion.documentId(storeId, article_no, UUID.randomUUID().toString())\n" +
                                "    }\n" +
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


