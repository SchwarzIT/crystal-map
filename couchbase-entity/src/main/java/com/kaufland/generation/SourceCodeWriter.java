/**
 * Copyright (C) 2010-2016 eBusiness Information, Excilys Group
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kaufland.generation;

import com.helger.jcodemodel.AbstractCodeWriter;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.SourcePrintWriter;
import com.kaufland.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

public class SourceCodeWriter extends AbstractCodeWriter {

    private static final VoidOutputStream VOID_OUTPUT_STREAM = new VoidOutputStream();
    private static final Logger LOGGER = Logger.getInstance();
    private final Filer filer;

    private Element[] mElements;



    private static class VoidOutputStream extends OutputStream {
        @Override
        public void write(int arg0) throws IOException {
            // Do nothing
        }
    }

    public SourceCodeWriter(Filer filer, Charset charset, Element[] elements) {
        super(charset);
        this.filer = filer;
        mElements = elements;
    }

    @Override
    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        String qualifiedClassName = toQualifiedClassName(pkg, fileName);

        try {
            JavaFileObject sourceFile;

            sourceFile = filer.createSourceFile(qualifiedClassName, mElements);

            return sourceFile.openOutputStream();
        } catch (FilerException e) {
            /*
			 * This exception is expected, when some files are created twice. We
			 * cannot delete existing files, unless using a dirty hack. Files a
			 * created twice when the same file is created from different
			 * annotation rounds. Happens when renaming classes, and for
			 * Background executor. It also probably means I didn't fully
			 * understand how annotation processing works. If anyone can point
			 * me out...
			 */
            return VOID_OUTPUT_STREAM;
        }
    }

    private String toQualifiedClassName(JPackage pkg, String fileName) {
        int suffixPosition = fileName.lastIndexOf('.');
        String className = fileName.substring(0, suffixPosition);

        String qualifiedClassName = pkg.name() + "." + className;
        return qualifiedClassName;
    }

    @Override
    public void close() throws IOException {
    }
}
