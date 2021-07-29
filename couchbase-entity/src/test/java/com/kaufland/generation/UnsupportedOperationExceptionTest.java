package com.kaufland.generation;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import com.kaufland.CoachBaseBinderProcessor;
import org.junit.Assert;
import org.junit.Test;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;

public class UnsupportedOperationExceptionTest {

    @Test
    public void testInUse() {

        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(JavaFileObjects.forSourceString("com.kaufland.testModels.UnsupportedOperationExceptionTest", "package com.kaufland.testModels;\n" +
                                "\n" +
                                "import java.util.ArrayList;\n" +
                                "import kaufland.com.coachbasebinderapi.Entity;\n" +
                                "import kaufland.com.coachbasebinderapi.Field;\n" +
                                "import kaufland.com.coachbasebinderapi.Fields;\n" +
                                "import kaufland.com.coachbasebinderapi.MapWrapper;\n" +
                                "import kaufland.com.coachbasebinderapi.deprecated.Deprecated;\n" +
                                "import kaufland.com.coachbasebinderapi.deprecated.DeprecatedField;\n" +
                                "import java.io.InputStream;\n" +
                                "\n" +
                                "@Entity(type = Entity.Type.READONLY, database = \"hydra_db\")\n" +
                                "@MapWrapper" +
                                "@Fields({@Field(\n" +
                                "        name = \"type\",\n" +
                                "        type = String.class,\n" +
                                "        defaultValue = \"Article\",\n" +
                                "        readonly = true),\n" +
                                "        @Field(\n" +
                                "        name = \"bottle\",\n" +
                                "        type = String.class,\n" +
                                "        list = true),\n" +
                                "        @Field(\n" +
                                "        name = \"bottle_box\",\n" +
                                "        type = String.class,\n" +
                                "        readonly = false),\n" +
                                "        @Field(\n" +
                                "        name = \"bottle2\",\n" +
                                "        type = String.class),\n" +
                                "        @Field(\n" +
                                "        name = \"bottle_box2\",\n" +
                                "        type = String.class,\n" +
                                "        list = true)\n" +
                                "})\n" +
                                "@Deprecated(fields = {@DeprecatedField(\n" +
                                "           field = \"bottle\",\n" +
                                "           replacedBy = \"bottle2\",\n" +
                                "           inUse = true), @DeprecatedField(\n" +
                                "           field = \"bottle_box\",\n" +
                                "           replacedBy = \"bottle_box2\",\n" +
                                "           inUse = true)\n" +
                                "})\n" +
                                "public class UnsupportedOperationExceptionTest {\n" +
                                "}"));

        if (compilation.status() == Compilation.Status.FAILURE){
            Diagnostic<? extends JavaFileObject> diagnostic = compilation.diagnostics().get(0);
            Assert.fail(diagnostic.getMessage(Locale.GERMAN));
            return;
        }
        Assert.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

}
