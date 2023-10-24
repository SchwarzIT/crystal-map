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
    private final transient String messageLiteral = "message";

    private transient ProcessingEnvironment mMock;
    private transient Messager mMessagerMock;

    @Before
    public void init() {
        mMock = Mockito.mock(ProcessingEnvironment.class);

        mMessagerMock = Mockito.mock(Messager.class);
        Mockito.when(mMock.getMessager()).thenReturn(mMessagerMock);
    }

    @Test
    public void testWarn() {

        new Logger(mMock).warn(messageLiteral, null);
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.WARNING, messageLiteral, null);
    }

    @Test
    public void testError() {

        new Logger(mMock).error(messageLiteral, null);
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.ERROR, messageLiteral, null);
    }

    @Test
    public void testErrorAbort() {

        try {
            new Logger(mMock).abortWithError(messageLiteral, null, null);
            Assert.fail("Should throw Exception");
        } catch (RuntimeException ignored) {}

        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.ERROR, messageLiteral, null);
    }

    @Test
    public void testInfo() {

        new Logger(mMock).info(messageLiteral);
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.NOTE, messageLiteral);

        new Logger(mMock).info(messageLiteral, null);
        Mockito.verify(mMessagerMock).printMessage(Diagnostic.Kind.NOTE, messageLiteral, null);
    }

}
