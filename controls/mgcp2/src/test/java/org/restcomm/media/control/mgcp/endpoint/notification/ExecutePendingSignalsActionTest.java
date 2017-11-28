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

package org.restcomm.media.control.mgcp.endpoint.notification;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.restcomm.media.control.mgcp.endpoint.notification.NotificationCenterEvent.NOTIFICATION_REQUEST;
import static org.restcomm.media.control.mgcp.endpoint.notification.NotificationCenterState.ACTIVE;
import static org.restcomm.media.control.mgcp.endpoint.notification.NotificationCenterState.IDLE;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.restcomm.media.control.mgcp.endpoint.EndpointIdentifier;
import org.restcomm.media.control.mgcp.endpoint.MgcpEndpoint;
import org.restcomm.media.control.mgcp.signal.BriefSignal;
import org.restcomm.media.control.mgcp.signal.TimeoutSignal;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class ExecutePendingSignalsActionTest {

    @Test
    public void testExecutePendingSignalsWithoutActiveBriefSignal() {
        // given
        final TimeoutSignal timeoutSignal1 = mock(TimeoutSignal.class);
        final TimeoutSignal timeoutSignal2 = mock(TimeoutSignal.class);
        final TimeoutSignal timeoutSignal3 = mock(TimeoutSignal.class);
        final Set<TimeoutSignal> timeoutSignals = Sets.newSet(timeoutSignal1, timeoutSignal2, timeoutSignal3);
        
        final BriefSignal briefSignal1 = mock(BriefSignal.class);
        final BriefSignal briefSignal2 = mock(BriefSignal.class);
        final BriefSignal briefSignal3 = mock(BriefSignal.class);
        final List<BriefSignal> briefSignals = Arrays.asList(briefSignal1, briefSignal2, briefSignal3);

        final MgcpEndpoint endpoint = mock(MgcpEndpoint.class);
        final EndpointIdentifier endpointId = new EndpointIdentifier("restcomm/mock/1", "127.0.0.1:2427");
        when(endpoint.getEndpointId()).thenReturn(endpointId);
        
        final NotificationCenterContext context = spy(new NotificationCenterContext());
        final NotificationCenterFsm stateMachine = mock(NotificationCenterFsm.class);
        when(stateMachine.getContext()).thenReturn(context);

        context.setEndpointId(endpointId.toString());
        context.setActiveBriefSignal(null);
        context.setTimeoutSignals(timeoutSignals);
        context.setPendingBriefSignals(briefSignals);
        
        // when
        final NotificationCenterTransitionContext txContext = new NotificationCenterTransitionContext();
        txContext.set(NotificationCenterTransitionParameter.PENDING_TIMEOUT_SIGNALS, Sets.newSet(timeoutSignal2, timeoutSignal3));
        
        final ExecutePendingSignalsAction action = new ExecutePendingSignalsAction();
        action.execute(IDLE, ACTIVE, NOTIFICATION_REQUEST, txContext, stateMachine);

        // then
        verify(timeoutSignal1, never()).execute(any(TimeoutSignalExecutionCallback.class));
        verify(timeoutSignal2).execute(any(TimeoutSignalExecutionCallback.class));
        verify(timeoutSignal3).execute(any(TimeoutSignalExecutionCallback.class));
        
        verify(briefSignal1).execute(any(BriefSignalExecutionCallback.class));
        verify(briefSignal2, never()).execute(any(BriefSignalExecutionCallback.class));
        verify(briefSignal3, never()).execute(any(BriefSignalExecutionCallback.class));
        assertFalse(context.getPendingBriefSignals().contains(briefSignal1));
        assertEquals(2, context.getPendingBriefSignals().size());
        assertEquals(briefSignal1, context.getActiveBriefSignal());
    }

    @Test
    public void testExecutePendingSignalsWithActiveBriefSignal() {
        // given
        final TimeoutSignal timeoutSignal1 = mock(TimeoutSignal.class);
        final TimeoutSignal timeoutSignal2 = mock(TimeoutSignal.class);
        final TimeoutSignal timeoutSignal3 = mock(TimeoutSignal.class);
        final Set<TimeoutSignal> timeoutSignals = Sets.newSet(timeoutSignal1, timeoutSignal2, timeoutSignal3);
        
        final BriefSignal activeBriefSignal = mock(BriefSignal.class);
        final BriefSignal briefSignal1 = mock(BriefSignal.class);
        final BriefSignal briefSignal2 = mock(BriefSignal.class);
        final BriefSignal briefSignal3 = mock(BriefSignal.class);
        final List<BriefSignal> briefSignals = Arrays.asList(briefSignal1, briefSignal2, briefSignal3);
        
        final MgcpEndpoint endpoint = mock(MgcpEndpoint.class);
        final EndpointIdentifier endpointId = new EndpointIdentifier("restcomm/mock/1", "127.0.0.1:2427");
        when(endpoint.getEndpointId()).thenReturn(endpointId);
        
        final NotificationCenterContext context = spy(new NotificationCenterContext());
        final NotificationCenterFsm stateMachine = mock(NotificationCenterFsm.class);
        when(stateMachine.getContext()).thenReturn(context);

        context.setEndpointId(endpointId.toString());
        context.setActiveBriefSignal(activeBriefSignal);
        context.setTimeoutSignals(timeoutSignals);
        context.setPendingBriefSignals(briefSignals);
        
        // when
        final NotificationCenterTransitionContext txContext = new NotificationCenterTransitionContext();
        txContext.set(NotificationCenterTransitionParameter.PENDING_TIMEOUT_SIGNALS, Sets.newSet(timeoutSignal2, timeoutSignal3));
        
        final ExecutePendingSignalsAction action = new ExecutePendingSignalsAction();
        action.execute(IDLE, ACTIVE, NOTIFICATION_REQUEST, txContext, stateMachine);
        
        // then
        verify(timeoutSignal1, never()).execute(any(TimeoutSignalExecutionCallback.class));
        verify(timeoutSignal2).execute(any(TimeoutSignalExecutionCallback.class));
        verify(timeoutSignal3).execute(any(TimeoutSignalExecutionCallback.class));
        
        verify(briefSignal1, never()).execute(any(BriefSignalExecutionCallback.class));
        verify(briefSignal2, never()).execute(any(BriefSignalExecutionCallback.class));
        verify(briefSignal3, never()).execute(any(BriefSignalExecutionCallback.class));
        assertEquals(3, context.getPendingBriefSignals().size());
        assertEquals(activeBriefSignal, context.getActiveBriefSignal());
    }

}
