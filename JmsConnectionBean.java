package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Simple sender that is autowired with the JMSTemplate bean and the queue name.
 * Copied to /opt/grouper/src/esb/edu/internet2/middleware/grouper/changeLog/esb/consumer
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

