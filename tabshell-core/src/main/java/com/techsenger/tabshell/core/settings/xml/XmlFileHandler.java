/*
 * Copyright 2024-2025 Pavel Castornii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techsenger.tabshell.core.settings.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common class for writing/reading XML files using JAXB.
 *
 * @author Pavel Castornii
 */
public final class XmlFileHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(XmlFileHandler.class);

    private final Class<T> clazz;

    private final Path path;

    private T object;

    public XmlFileHandler(Class<T> clazz, Path path) {
        this.clazz = clazz;
        this.path = path;
    }

    public synchronized void read() {
        //there is a problem when this method is called from plugin layer
        var contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(XmlFileHandler.class.getClassLoader());
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setEventHandler(new ValidationEventHandlerImpl());
            this.object = (T) unmarshaller.unmarshal(path.toFile());
            logger.debug("Read from {} object: {}", path, this.object);
        } catch (JAXBException e) {
            //it is necessary to do e.toString() to get detailed messages
            logger.error("Error reading object from {}: {}", path, e.toString(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public synchronized void write() {
        //there is a problem when this method is called from plugin layer
        var contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(XmlFileHandler.class.getClassLoader());
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(object, path.toFile());
            marshaller.setEventHandler(new ValidationEventHandlerImpl());
            logger.debug("Wrote to {} object: {}", path, this.object);
        }  catch (JAXBException e) {
            //it is necessary to do e.toString() to get detailed messages
            logger.error("Error writing object to {}: {}", path, e.toString(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public T getObject() {
        return object;
    }
}
