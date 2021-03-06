/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.restcomm.media.core.asr;

import java.util.List;

import org.restcomm.media.core.drivers.asr.AsrDriverConfigurationException;
import org.restcomm.media.core.drivers.asr.UnknownAsrDriverException;
import org.restcomm.media.core.resource.vad.VoiceActivityDetectorListener;
import org.restcomm.media.core.spi.MediaSink;

/**
 * @author gdubina
 */
public interface AsrEngine extends MediaSink {

    /**
     * Configures the ASR Engine.
     *
     * @param driver   The ASR driver to be loaded.
     * @param language The language to be used.
     * @param hints    The list of hotwords to help raise detection accuracy.
     * @throws UnknownAsrDriverException       When the provided driver is not supported.
     * @throws AsrDriverConfigurationException When the driver is badly configured.
     */
    void configure(String driver, String language, List<String> hints) throws UnknownAsrDriverException, AsrDriverConfigurationException;

    /**
     * Attaches a listener to the engine to be notified about ASR events.
     *
     * @param listener The listener who will be notified by the ASR Engine.
     */
    void setListener(AsrEngineListener listener);

    /**
     * Gets the amount of time the engine will wait for the ASR provider to provide a transcription.
     *
     * @return The waiting time, in milliseconds.
     */
    int getResponseTimeoutInMilliseconds();

    /**
     * Starts speech detection process.
     *
     * @param voiceActivityDetectorListener The listener who will be notified about speech detection events.
     */
    void startSpeechDetection(VoiceActivityDetectorListener voiceActivityDetectorListener);

    /**
     * Stops the speech detection process.
     */
    void stopSpeechDetection();

}
