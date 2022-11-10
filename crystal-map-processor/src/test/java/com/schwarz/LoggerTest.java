package com.schwarz;

import com.schwarz.crystalprocessor.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

public class LoggerTest {

    private ProcessingEnvironment mMock;
    private Messager mMessagerMock;

    @Before
    public void init() {
        mMock = Mockito.mock(ProcessingEnvironment.class);

        mMessagerMock = Mockito.mock(Messager.class);
        Mockito.when(mMock.getMessager()).thenReturn(mMessagerMock);
    }

    @Test
    public void testWarn() {

        new Logger(mMock).warn("message", null);
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.WARNING, "message", null);
    }

    @Test
    public void testError() {

        new Logger(mMock).error("message", null);
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.ERROR, "message", null);
    }

    @Test
    public void testErrorAbort() {

        try {
            new Logger(mMock).abortWithError("message", null, null);
            Assert.fail("Should throw Exception");
        } catch (RuntimeException e) {

        }
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.ERROR, "message", null);

    }

    @Test
    public void testInfo() {

        new Logger(mMock).info("message");
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.NOTE, "message");

        new Logger(mMock).info("message", null);
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.NOTE, "message", null);
    }

}
