/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 04.12.2008 12:43:55
 *
 * $Id$
 */
package com.haulmont.cuba.web;

import com.haulmont.cuba.security.app.LoginService;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.web.sys.WebSecurityUtils;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Connection to the middleware.
 * <br>Can be obtained via {@link com.haulmont.cuba.web.App#getConnection()} method.
 */
public class Connection implements Serializable
{
    private static Log log = LogFactory.getLog(Connection.class);

    private Map<ConnectionListener, Object> connListeners = new HashMap<ConnectionListener, Object>();
    private Map<UserSubstitutionListener, Object> usListeners = new HashMap<UserSubstitutionListener, Object>();

    private boolean connected;
    private UserSession session;

    private static final long serialVersionUID = 3687127630079762741L;

    /**
     * True if the web application is succesfully logged in to middleware and a user session exists.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Current user session. Null if not connected.
     */
    public UserSession getSession() {
        return session;
    }

    /**
     * Set user session for this connection
     */
    public void setSession(UserSession session) {
        this.session = session;
    }

    private LoginService getLoginService() {
        LoginService ls = ServiceLocator.lookup(LoginService.JNDI_NAME);
        return ls;
    }

    public void update(UserSession session) throws LoginException {
        this.session = session;
        connected = true;

        try {
            internalLogin();
        } catch (RuntimeException e) {
            internalLogout();
            throw e;
        } catch (Exception e) {
            internalLogout();
            throw new RuntimeException(e);
        }
    }

    /**
     * Perform login
     * @param login login name
     * @param password encrypted password
     * @param locale
     * @throws LoginException
     */
    public void login(String login, String password, Locale locale) throws LoginException {
        if (locale == null)
            throw new IllegalArgumentException("Locale is null");

        update(getLoginService().login(login, password, locale));
    }

    /**
     * Perform login using password stored in ActiveDirectory
     * @param login login name
     * @throws LoginException
     */
    public void loginActiveDirectory(String login, Locale locale) throws LoginException {
        if (locale == null)
            throw new IllegalArgumentException("Locale is null");

        update(getLoginService().loginActiveDirectory(login, locale));
    }

    private void internalLogin() throws LoginException {
        WebBrowser browser = ((WebApplicationContext) App.getInstance().getContext()).getBrowser();
        session.setAddress(browser.getAddress());
        session.setClientInfo(browser.getBrowserApplication());

        WebSecurityUtils.setSecurityAssociation(session.getUser().getLogin(), session.getId());
        fireConnectionListeners();

        if (log.isDebugEnabled()) {
            log.debug(String.format("Logged in: user=%s, ip=%s, browser=%s",
                    session.getUser().getLogin(), browser.getAddress(), browser.getBrowserApplication()));
        }
    }

    /**
     * Substitute user. Current user session will get rights and constraints of substituted user.
     */
    public void substituteUser(User substitutedUser) {
        session = getLoginService().substituteUser(substitutedUser);
        fireSubstitutionListeners();
    }

    /**
     * Perform logout
     */
    public void logout() {
        if (!connected)
            return;

        internalLogout();
        try {
            fireConnectionListeners();
        } catch (LoginException e) {
            log.warn("Exception on logout:", e);
        }
    }

    private void internalLogout() {
        LoginService ls = getLoginService();
        ls.logout();

        WebSecurityUtils.clearSecurityAssociation();

        connected = false;
        session = null;
    }

    public void addListener(ConnectionListener listener) {
        connListeners.put(listener, null);
    }

    public void removeListener(ConnectionListener listener) {
        connListeners.remove(listener);
    }

    public void addListener(UserSubstitutionListener listener) {
        usListeners.put(listener, null);
    }

    public void removeListener(UserSubstitutionListener listener) {
        usListeners.remove(listener);
    }

    private void fireConnectionListeners() throws LoginException {
        for (ConnectionListener listener : connListeners.keySet()) {
            listener.connectionStateChanged(this);
        }
    }

    private void fireSubstitutionListeners() {
        for (UserSubstitutionListener listener : usListeners.keySet()) {
            listener.userSubstituted(this);
        }
    }
}
