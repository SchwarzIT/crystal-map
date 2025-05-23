package com.schwarz.generation;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor;
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
                        .compile(JavaFileObjects.forSourceString("com.kaufland.testModels.UnsupportedOperationExceptionTest", """
                                package com.kaufland.testModels;
                                
                                import java.util.ArrayList;
                                import com.schwarz.crystalapi.Entity;
                                import com.schwarz.crystalapi.Field;
                                import com.schwarz.crystalapi.Fields;
                                import com.schwarz.crystalapi.MapWrapper;
                                import com.schwarz.crystalapi.deprecated.Deprecated;
                                import com.schwarz.crystalapi.deprecated.DeprecatedField;
                                import java.io.InputStream;
                                
                                @Entity(type = Entity.Type.READONLY, database = "hydra_db")
                                @MapWrapper\
                                @Fields({@Field(
                                        name = "type",
                                        type = String.class,
                                        defaultValue = "Article",
                                        readonly = true),
                                        @Field(
                                        name = "bottle",
                                        type = String.class,
                                        list = true),
                                        @Field(
                                        name = "bottle_box",
                                        type = String.class,
                                        readonly = false),
                                        @Field(
                                        name = "bottle2",
                                        type = String.class),
                                        @Field(
                                        name = "bottle_box2",
                                        type = String.class,
                                        list = true)
                                })
                                @Deprecated(fields = {@DeprecatedField(
                                           field = "bottle",
                                           replacedBy = "bottle2",
                                           inUse = true), @DeprecatedField(
                                           field = "bottle_box",
                                           replacedBy = "bottle_box2",
                                           inUse = true)
                                })
                                public class UnsupportedOperationExceptionTest {
                                }"""));

        if (compilation.status() == Compilation.Status.FAILURE){
            Diagnostic<? extends JavaFileObject> diagnostic = compilation.diagnostics().get(0);
            Assert.fail(diagnostic.getMessage(Locale.GERMAN));
            return;
        }
        Assert.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

}
