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
        
package org.restcomm.media.rtp.connection;

import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class RtpConnectionFsmBuilder {
    
    public static final RtpConnectionFsmBuilder INSTANCE = new RtpConnectionFsmBuilder();
    
    private final StateMachineBuilder<RtpConnectionFsm, RtpConnectionState, RtpConnectionEvent, RtpConnectionTransitionContext> builder; 
    
    public RtpConnectionFsmBuilder() {
        this.builder = StateMachineBuilderFactory.<RtpConnectionFsm, RtpConnectionState, RtpConnectionEvent, RtpConnectionTransitionContext>create(RtpConnectionFsmImpl.class, RtpConnectionState.class, RtpConnectionEvent.class, RtpConnectionTransitionContext.class, RtpConnectionContext.class);
        
        this.builder.externalTransition().from(RtpConnectionState.IDLE).to(RtpConnectionState.OPENING).on(RtpConnectionEvent.OPEN);
        this.builder.externalTransition().from(RtpConnectionState.IDLE).toFinal(RtpConnectionState.CLOSED).on(RtpConnectionEvent.CLOSE);
        
        this.builder.onEntry(RtpConnectionState.OPENING).callMethod("enterOpening");
        this.builder.defineSequentialStatesOn(RtpConnectionState.OPENING, RtpConnectionState.PARSING_REMOTE_DESCRIPTION, RtpConnectionState.ALLOCATING_SESSION, RtpConnectionState.SETTING_SESSION_MODE, RtpConnectionState.NEGOTIATING_SESSION, RtpConnectionState.GENERATING_LOCAL_DESCRIPTION);
        this.builder.externalTransition().from(RtpConnectionState.OPENING).to(RtpConnectionState.CORRUPTED).on(RtpConnectionEvent.PARSE_REMOTE_DESCRIPTION_FAILURE);
        this.builder.externalTransition().from(RtpConnectionState.OPENING).to(RtpConnectionState.CORRUPTED).on(RtpConnectionEvent.SESSION_ALLOCATION_FAILURE);
        this.builder.externalTransition().from(RtpConnectionState.OPENING).to(RtpConnectionState.CORRUPTED).on(RtpConnectionEvent.SESSION_MODE_UPDATE_FAILURE);
        this.builder.externalTransition().from(RtpConnectionState.OPENING).to(RtpConnectionState.CORRUPTED).on(RtpConnectionEvent.SESSION_NEGOTIATION_FAILURE);
        this.builder.externalTransition().from(RtpConnectionState.OPENING).to(RtpConnectionState.CORRUPTED).on(RtpConnectionEvent.GENERATE_LOCAL_DESCRIPTION_FAILURE);
        this.builder.externalTransition().from(RtpConnectionState.OPENING).to(RtpConnectionState.OPEN).on(RtpConnectionEvent.OPENED);
        this.builder.externalTransition().from(RtpConnectionState.OPENING).to(RtpConnectionState.CLOSING).on(RtpConnectionEvent.CLOSE);
        this.builder.onExit(RtpConnectionState.OPENING).callMethod("exitOpening");
        
        this.builder.onEntry(RtpConnectionState.PARSING_REMOTE_DESCRIPTION).callMethod("enterParsingRemoteDescription");
        this.builder.localTransition().from(RtpConnectionState.PARSING_REMOTE_DESCRIPTION).to(RtpConnectionState.ALLOCATING_SESSION).on(RtpConnectionEvent.PARSED_REMOTE_DESCRIPTION);
        this.builder.onExit(RtpConnectionState.PARSING_REMOTE_DESCRIPTION).callMethod("exitParsingRemoteDescription");
        
        this.builder.onEntry(RtpConnectionState.ALLOCATING_SESSION).callMethod("enterAllocatingSession");
        this.builder.localTransition().from(RtpConnectionState.ALLOCATING_SESSION).to(RtpConnectionState.SETTING_SESSION_MODE).on(RtpConnectionEvent.SESSION_ALLOCATED);
        this.builder.onExit(RtpConnectionState.ALLOCATING_SESSION).callMethod("exitAllocatingSession");
        
        this.builder.onEntry(RtpConnectionState.SETTING_SESSION_MODE).callMethod("enterSettingSessionMode");
        this.builder.localTransition().from(RtpConnectionState.SETTING_SESSION_MODE).to(RtpConnectionState.NEGOTIATING_SESSION).on(RtpConnectionEvent.SESSION_MODE_UPDATED);
        this.builder.onExit(RtpConnectionState.SETTING_SESSION_MODE).callMethod("exitSettingSessionMode");
        
        this.builder.onEntry(RtpConnectionState.NEGOTIATING_SESSION).callMethod("enterNegotiatingSession");
        this.builder.localTransition().from(RtpConnectionState.NEGOTIATING_SESSION).to(RtpConnectionState.GENERATING_LOCAL_DESCRIPTION).on(RtpConnectionEvent.SESSION_NEGOTIATED);
        this.builder.onExit(RtpConnectionState.NEGOTIATING_SESSION).callMethod("exitNegotiatingSession");
        
        this.builder.onEntry(RtpConnectionState.GENERATING_LOCAL_DESCRIPTION).callMethod("enterGeneratingLocalDescription");
        this.builder.onExit(RtpConnectionState.GENERATING_LOCAL_DESCRIPTION).callMethod("exitGeneratingLocalDescription");
        
        this.builder.onEntry(RtpConnectionState.OPEN).callMethod("enterOpen");
        this.builder.externalTransition().from(RtpConnectionState.OPEN).to(RtpConnectionState.UPDATING_MODE).on(RtpConnectionEvent.UPDATE_MODE);
        this.builder.externalTransition().from(RtpConnectionState.OPEN).to(RtpConnectionState.CLOSING).on(RtpConnectionEvent.CLOSE);
        this.builder.onExit(RtpConnectionState.OPEN).callMethod("exitOpen");
        
        this.builder.onEntry(RtpConnectionState.CORRUPTED).callMethod("enterCorrupted");
        this.builder.externalTransition().from(RtpConnectionState.CORRUPTED).to(RtpConnectionState.CLOSING).on(RtpConnectionEvent.CLOSE);
        this.builder.onExit(RtpConnectionState.CORRUPTED).callMethod("exitCorrupted");

        this.builder.onEntry(RtpConnectionState.UPDATING_MODE).callMethod("enterUpdatingMode");
        this.builder.defineSequentialStatesOn(RtpConnectionState.UPDATING_MODE, RtpConnectionState.UPDATING_SESSION_MODE, RtpConnectionState.SESSION_MODE_UPDATED);
        this.builder.externalTransition().from(RtpConnectionState.UPDATING_MODE).to(RtpConnectionState.OPEN).on(RtpConnectionEvent.MODE_UPDATED);
        this.builder.externalTransition().from(RtpConnectionState.UPDATING_MODE).to(RtpConnectionState.CORRUPTED).on(RtpConnectionEvent.SESSION_MODE_UPDATE_FAILURE);
        this.builder.externalTransition().from(RtpConnectionState.UPDATING_MODE).to(RtpConnectionState.CLOSING).on(RtpConnectionEvent.CLOSE);
        this.builder.onExit(RtpConnectionState.UPDATING_MODE).callMethod("exitUpdatingMode");
        
        this.builder.onEntry(RtpConnectionState.UPDATING_SESSION_MODE).callMethod("enterUpdatingSessionMode");
        this.builder.localTransition().from(RtpConnectionState.UPDATING_SESSION_MODE).to(RtpConnectionState.SESSION_MODE_UPDATED).on(RtpConnectionEvent.SESSION_MODE_UPDATED);
        this.builder.onExit(RtpConnectionState.UPDATING_SESSION_MODE).callMethod("exitUpdatingSessionMode");

        this.builder.onEntry(RtpConnectionState.SESSION_MODE_UPDATED).callMethod("enterSessionModeUpdated");
        this.builder.onExit(RtpConnectionState.SESSION_MODE_UPDATED).callMethod("exitSessionModeUpdated");
        
        this.builder.onEntry(RtpConnectionState.CLOSING).callMethod("enterClosing");
        this.builder.defineSequentialStatesOn(RtpConnectionState.CLOSING, RtpConnectionState.CLOSING_SESSION, RtpConnectionState.CLOSED_SESSION);
        this.builder.externalTransition().from(RtpConnectionState.CLOSING).toFinal(RtpConnectionState.CLOSED).on(RtpConnectionEvent.CLOSED);
        this.builder.onExit(RtpConnectionState.CLOSING).callMethod("exitClosing");
        
        this.builder.onEntry(RtpConnectionState.CLOSING_SESSION).callMethod("enterClosingSession");
        this.builder.localTransition().from(RtpConnectionState.CLOSING_SESSION).to(RtpConnectionState.CLOSED_SESSION).on(RtpConnectionEvent.SESSION_CLOSED);
        this.builder.localTransition().from(RtpConnectionState.CLOSING_SESSION).to(RtpConnectionState.CLOSED_SESSION).on(RtpConnectionEvent.SESSION_CLOSE_FAILURE);
        this.builder.onExit(RtpConnectionState.CLOSING_SESSION).callMethod("exitClosingSession");

        this.builder.onEntry(RtpConnectionState.CLOSED_SESSION).callMethod("enterClosedSession");
        this.builder.onExit(RtpConnectionState.CLOSED_SESSION).callMethod("exitClosedSession");

        this.builder.onEntry(RtpConnectionState.CLOSED).callMethod("enterClosed");
    }
    
    public RtpConnectionFsm build(RtpConnectionContext context) {
        return this.builder.newStateMachine(RtpConnectionState.IDLE, context);
    }

}