package com.schwarz;

import com.schwarz.crystalprocessor.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

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

        new Logger(mMock).warn("warnMessage", null);
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.WARNING, "warnMessage", null);
    }

    @Test
    public void testError() {

        new Logger(mMock).error("errorMessage", null);
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.ERROR, "errorMessage", null);
    }

    @Test
    public void testErrorAbort() {

        try {
            new Logger(mMock).abortWithError("abortMessage", new ArrayList<>(), null);
            Assert.fail("Should throw Exception");
        } catch (RuntimeException ignored) {

        }
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.ERROR, "abortMessage", null);

    }

    @Test
    public void testInfo() {

        new Logger(mMock).info("infoMessage");
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.NOTE, "infoMessage");

        new Logger(mMock).info("infoMessageWithNull", null);
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.NOTE, "infoMessageWithNull", null);
    }

}
