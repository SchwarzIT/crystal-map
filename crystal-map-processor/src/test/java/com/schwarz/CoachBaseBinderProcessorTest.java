package com.schwarz;


import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor;

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
                        .compile(JavaFileObjects.forSourceString("com.kaufland.testModels.ListTest", """
                                package com.kaufland.testModels;
                                
                                import java.util.ArrayList;
                                import com.schwarz.crystalapi.Entity;
                                import com.schwarz.crystalapi.Field;
                                import com.schwarz.crystalapi.Fields;
                                import java.io.InputStream;
                                
                                @Entity(
                                        database = "mydb_db"
                                )
                                @Fields({@Field(
                                        defaultValue = "product",
                                        type = String.class,
                                        name = "type",
                                        readonly = true
                                ), @Field(
                                        type = String.class,
                                        name = "name"
                                ), @Field(
                                        list = true,
                                        type = String.class,
                                        name = "comments"
                                )\
                                , @Field(
                                         type = Boolean.class,
                                                       name = "bool"
                                                                 )\
                                , @Field(
                                        list = true,
                                        type = String.class,
                                        name = "identifiers"
                                )})\
                                public class ListTest {
                                }"""));

        if(compilation.status() == Compilation.Status.FAILURE){
            Diagnostic<? extends JavaFileObject> diagnostic = compilation.diagnostics().get(0);
            Assert.fail(diagnostic.getMessage(Locale.GERMAN));
            return;
        }
        Assert.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @Test
    public void testSuccessSubEntityProcessing() {

        JavaFileObject mMainEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.ListTest", """
                package com.kaufland.testModels;
                
                import java.util.ArrayList;
                import com.schwarz.crystalapi.Entity;
                import com.schwarz.crystalapi.Field;
                import com.schwarz.crystalapi.Fields;
                import java.io.InputStream;
                
                @Entity
                @Fields({@Field(
                        type = com.kaufland.testModels.Sub.class,
                        name = "list_sub",
                        list = true
                ), @Field(
                        type = Sub.class,
                        name = "sub"
                )})\
                public class ListTest {
                }""");
        JavaFileObject subEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.Sub", """
                package com.kaufland.testModels;
                
                import com.schwarz.crystalapi.MapWrapper;
                import com.schwarz.crystalapi.Field;
                import com.schwarz.crystalapi.Fields;
                
                /**
                 * Created by sbra0902 on 31.05.17.
                 */
                @MapWrapper
                @Fields({@Field(
                        type = String.class,
                        name = "test"
                )})\
                public class Sub {
                }""");
        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(mMainEntity, subEntity);

        Assert.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @Test
    public void testFailContructorAndPublicFieldProcessing() {

        JavaFileObject subEntity = JavaFileObjects.forSourceString("com.kaufland.testModels.Sub", """
                package com.kaufland.testModels;
                
                import com.schwarz.crystalapi.Entity;
                import com.schwarz.crystalapi.Field;
                import com.schwarz.crystalapi.Fields;
                
                /**
                 * Created by sbra0902 on 31.05.17.
                 */
                @Entity
                @Fields({@Field(
                        type = String.class,
                        name = "test"
                ), @Field(
                        type = String.class,
                        name = "type",
                        defaultValue = "product",
                        readonly= true
                )})\
                public class Sub {
                
                 public Sub(String test){
                 }
                }""");


        Compilation compilation =
                javac()
                        .withProcessors(new CoachBaseBinderProcessor())
                        .compile(subEntity);


        Assert.assertEquals(Compilation.Status.FAILURE, compilation.status());

        Assert.assertTrue(compilation.diagnostics().stream().anyMatch(diagnostic -> diagnostic.getMessage(Locale.GERMAN).equals("Entity should not have a constructor")));
    }
}


