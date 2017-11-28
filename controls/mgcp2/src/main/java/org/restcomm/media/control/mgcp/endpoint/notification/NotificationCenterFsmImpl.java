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

import com.google.common.util.concurrent.FutureCallback;
import org.apache.log4j.Logger;
import org.restcomm.media.control.mgcp.pkg.MgcpEvent;
import org.restcomm.media.control.mgcp.pkg.MgcpEventObserver;
import org.squirrelframework.foundation.fsm.StateMachineStatus;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import java.util.Set;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 */
public class NotificationCenterFsmImpl extends AbstractStateMachine<NotificationCenterFsm, NotificationCenterState, NotificationCenterEvent, NotificationCenterTransitionContext> implements NotificationCenterFsm {

    private static final Logger log = Logger.getLogger(NotificationCenterFsmImpl.class);

    private final NotificationCenterContext context;

    public NotificationCenterFsmImpl(NotificationCenterContext context) {
        super();
        this.context = context;
    }

    @Override
    public NotificationCenterContext getContext() {
        return this.context;
    }

    @Override
    protected void afterTransitionCausedException(NotificationCenterState fromState, NotificationCenterState toState, NotificationCenterEvent event, NotificationCenterTransitionContext context) {
        final Throwable t = this.getLastException().getTargetException();
        final String requestId = this.context.getRequestId();

        log.error("Endpoint " + this.context.getEndpointId() + " caught unexpected error in MGCP RQNT " + requestId + ". Cancelling request.", t);

        this.setStatus(StateMachineStatus.IDLE);
        this.fire(NotificationCenterEvent.FAILURE, context);
        // TODO Implement rollback on NotificationCenterFsmImpl
    }

    @Override
    protected void afterTransitionDeclined(NotificationCenterState fromState, NotificationCenterEvent event, NotificationCenterTransitionContext context) {
        final FutureCallback<?> callback = context.get(NotificationCenterTransitionParameter.CALLBACK, FutureCallback.class);

        if (callback != null) {
            // Warn callback that requested operation failed
            final IllegalStateException error = new IllegalStateException("Operation " + event.name() + " not allowed on state " + fromState);
            callback.onFailure(error);
        }
    }

    @Override
    public void observe(MgcpEventObserver observer) {
        final boolean added = this.context.getEventObservers().add(observer);
        if (added && log.isTraceEnabled()) {
            log.trace("Registered MgcpEventObserver@" + observer.hashCode() + ". Count: " + this.context.getEventObservers().size());
        }
    }

    @Override
    public void forget(MgcpEventObserver observer) {
        final boolean removed = this.context.getEventObservers().remove(observer);
        if (removed && log.isTraceEnabled()) {
            log.trace("Unregistered MgcpEventObserver@" + observer.hashCode() + ". Count: " + this.context.getEventObservers().size());
        }
    }

    @Override
    public void notify(Object originator, MgcpEvent event) {
        final Set<MgcpEventObserver> observers = this.context.getEventObservers();
        for (MgcpEventObserver observer : observers) {
            observer.onEvent(originator, event);
        }
    }

}
