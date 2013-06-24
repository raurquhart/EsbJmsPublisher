package edu.internet2.middleware.grouper.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Simple sender that is autowired with the JMSTemplate bean and the queue name.
 # Copied to  /opt/grouper/src/grouper/edu/internet2/middleware/grouper/jms/JmsConnectionBean.java
 */
@Component
public class JmsConnectionBean
{
    private final JmsTemplate jmsTemplate;

    @Autowired
    public JmsConnectionBean( final JmsTemplate jmsTemplate ) {
        this.jmsTemplate = jmsTemplate;
    }

    public void send( final String message ) {
        jmsTemplate.convertAndSend( message );
    }
}

