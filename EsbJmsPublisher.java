/*
 * @author Rob Urquhart
 */

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.springframework.context.*;
import org.springframework.context.support.*;
import ca.sfu.icat.jms.JmsMessageSender;

/**
 * 
 * Class to send Grouper events to JMS server, formatted as JSON strings
 * Copied to /opt/grouper/src/esb/edu/internet2/middleware/grouper/changeLog/esb/consumer
 *
 */
public class EsbJmsPublisher extends EsbListenerBase {

  private static Log LOG; 
  private static JmsMessageSender jmsMessageSender;

  public EsbJmsPublisher() {
    super();
    if (LOG == null) {
      LOG = GrouperUtil.getLog(EsbJmsPublisher.class);
    }
    if (jmsMessageSender == null) {
      jmsMessageSender = new JmsMessageSender("Grouper", "ICAT.grouper.events", LOG);
    }
  }

  /**
   * @see EsbListenerBase#dispatchEvent(String, String)
   */
  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {

    if (LOG.isDebugEnabled()) {
      LOG.info("Consumer " + consumerName + " publishing "
          + GrouperUtil.indent(eventJsonString, false));
    }

    jmsMessageSender.send(eventJsonString);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("ESB JMS client " + consumerName + " sent message");
    }
    return true;
  }

  /**
   * 
   */
  @Override
  public void disconnect() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("ESB JMS client disconnect()");
    }
    //do nothing
  }
}
