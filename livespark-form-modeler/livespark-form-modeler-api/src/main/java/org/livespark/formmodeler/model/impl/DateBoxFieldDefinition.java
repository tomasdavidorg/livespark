package org.livespark.formmodeler.model.impl;

import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by pefernan on 3/19/15.
 */
@Portable
public class DateBoxFieldDefinition extends AbstractIntputFieldDefinition<Date> {

    @Override
    public String getStandaloneClassName() {
        return Date.class.getCanonicalName();
    }
}
