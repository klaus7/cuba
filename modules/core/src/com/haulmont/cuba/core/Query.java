/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 13.11.2008 18:02:40
 *
 * $Id$
 */
package com.haulmont.cuba.core;

import com.haulmont.cuba.core.global.View;

import javax.persistence.TemporalType;
import java.util.List;
import java.util.Date;

/**
* Interface used to control query execution.
*/
public interface Query
{
    String getQueryString();
    
    void setQueryString(String queryString);

    /**
     * Execute a SELECT query and return the query results as a List.
     * @return a list of the results
     * @throws IllegalStateException if called for a Java Persistence query language UPDATE or DELETE statement
     */
    List getResultList();

    /**
     * Execute a SELECT query that returns a single result.
     * @return the result
     * @throws javax.persistence.NoResultException if there is no result
     * @throws javax.persistence.NonUniqueResultException if more than one result
     * @throws IllegalStateException if called for a Java Persistence query language UPDATE or DELETE statement
     */
    Object getSingleResult();

    /**
     * Execute an update or delete statement.
     * @return the number of entities updated or deleted
     * @throws IllegalStateException if called for a Java Persistence query language SELECT statement
     * @throws javax.persistence.TransactionRequiredException if there is no transaction
     */
    int executeUpdate();

    /**
     * Set the maximum number of results to retrieve.
     * @param maxResult
     * @return the same query instance
     * @throws IllegalArgumentException if argument is negative
     */
    Query setMaxResults(int maxResult);

    /**
     * Set the position of the first result to retrieve.
     * @param startPosition position of the first result, numbered from 0
     * @return the same query instance
     * @throws IllegalArgumentException if argument is negative
     */
    Query setFirstResult(int startPosition);

    /**
     * Bind an argument to a named parameter.
     * @param name the parameter name
     * @param value
     * @return the same query instance
     * @throws IllegalArgumentException if parameter name does not correspond to parameter in query string
     * or argument is of incorrect type
     */
    Query setParameter(String name, Object value);

    /**
     * Bind an instance of java.util.Date to a named parameter.
     * @param name
     * @param value
     * @param temporalType
     * @return the same query instance
     * @throws IllegalArgumentException if parameter name does not correspond to parameter in query string
     */
    Query setParameter(String name, Date value, TemporalType temporalType);

    /**
     * Bind an argument to a positional parameter.
     * @param position
     * @param value
     * @return the same query instance
     * @throws IllegalArgumentException if position does not correspond to positional parameter of query
     * or argument is of incorrect type
     */
    Query setParameter(int position, Object value);

    /**
     * Bind an instance of java.util.Date to a positional parameter.
     * @param position
     * @param value
     * @param temporalType
     * @return the same query instance
     * @throws IllegalArgumentException if position does not correspond to positional parameter of query
     */
    Query setParameter(int position, Date value, TemporalType temporalType);

    /**
     * Set View for this Query instance
     * @param view
     * @return the same query instance
     */
    Query setView(View view);
}
