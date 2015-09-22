/*
 * @author Rob Urquhart
 */

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import java.util.Set;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;

import org.springframework.context.*;
import org.springframework.context.support.*;
import ca.sfu.icat.jms.JmsMessageSender;
import ca.sfu.icat.camel.*;
import ca.sfu.icat.grouper.EsbEvent;
import ca.sfu.icat.grouper.EsbEvents;
import ca.sfu.icat.grouper.ADGroupUpdateEvent;
import ca.sfu.icat.grouper.SADGroupUpdateEvent;


/**
 * Class to send Grouper events to JMS server, formatted as JSON strings
 * Copied to /opt/grouper/src/esb/edu/internet2/middleware/grouper/changeLog/esb/consumer
 */
public class EsbJmsPublisher extends EsbListenerBase {

    private static Log LOG;
    private static JmsMessageSender jmsMessageSender;
    private static GrouperJSONMessageConverter messageConverter;

    public EsbJmsPublisher() {
        super();
        if (LOG == null) {
            LOG = GrouperUtil.getLog(EsbJmsPublisher.class);
        }
        if (jmsMessageSender == null) {
            jmsMessageSender = new JmsMessageSender("Grouper", "ICAT.grouper.events", LOG);
        }
        if (messageConverter == null) {
            messageConverter = new GrouperJSONMessageConverter();
        }
    }

    /**
     * @see EsbListenerBase#dispatchEvent(String, String)
     */
    @Override
    public boolean dispatchEvent(String eventJsonString, String consumerName) {
        boolean isADGroup = false;
        boolean isSADGroup = false;
        Group group;

        if (LOG.isDebugEnabled()) {
            LOG.info("Consumer " + consumerName + " publishing "
                    + GrouperUtil.indent(eventJsonString, false));
        }
        EsbEvents esbEvents = (EsbEvents) messageConverter.fromString(eventJsonString);
        EsbEvent esbEvent = esbEvents.getEsbEvent()[0];
        String groupname = esbEvent.groupnameToModify();

/*
        if (esbEvent.isMembershipEvent() && esbEvent.isGrouperSourceId()) {
            String subjectId = esbEvent.getSubjectId();
            Group memberGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(subjectId, false);
            LOG.debug("memberGroup: " + memberGroup);
            //if (memberGroup == null) return true;
            //String groupName = group.getDisplayName();
            //esbEvent.setSubjectId(groupName);
            //eventJsonString = messageConverter.fromEsbEvent(esbEvent);
        }
*/

        group = GrouperDAOFactory.getFactory().getGroup().findByName(groupname, false, null);
        if (group == null) {
            LOG.info("Group not found: " + groupname);
            return true;
        }

        isADGroup = isADGroup(group);
        isSADGroup = isSADGroup(group);

        if (LOG.isDebugEnabled()) {
            LOG.info("Sending message: " + eventJsonString);
        }
        if (!send(eventJsonString)) {
            LOG.info("Error sending jms message: " + eventJsonString);
            return false;
        }

        if (isADGroup) {
            // Create a new ad_group_change message and send it
            ADGroupUpdateEvent adEvent = new ADGroupUpdateEvent(groupname);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending AD message: " + adEvent);
            }
            if (!send(adEvent)) {
                LOG.info("Error sending AD jms message: " + adEvent);
            }
        }
        if (isSADGroup) {
            // Create a new sad_group_change message and send it
            SADGroupUpdateEvent adEvent = new SADGroupUpdateEvent(groupname);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending SAD message: " + eventJsonString);
            }
            if (!send(adEvent)) {
                LOG.info("Error sending SAD jms message: " + adEvent);
            }
        }

        return true;
    }

    private boolean isADGroup(Group group) {
        if (!isSfuMaillistType(group)) return false;
        String isADGroupAttr;
        boolean isADGroup;
        isADGroupAttr = group.getAttributeOrFieldValue("sfuIsADGroup", false, false);
        isADGroup = isADGroupAttr != null && isADGroupAttr.equals("1");
        return isADGroup;
    }

    private boolean isSADGroup(Group group) {
        if (!isSfuMaillistType(group)) return false;
        String isSADGroupAttr;
        boolean isSADGroup;
        isSADGroupAttr = group.getAttributeOrFieldValue("sfuIsSADGroup", false, false);
        isSADGroup = isSADGroupAttr != null && isSADGroupAttr.equals("1");
        return isSADGroup;
    }

    private boolean isSfuMaillistType(Group group) {
        Set<GroupType> types = group.getTypes();
        LOG.info("types: " + types);
        for (GroupType groupType : types) {
            if ("sfuMaillist".equals(groupType.getName())) return true;
        }
        return false;
    }

    private boolean send(String eventJsonString) {
        jmsMessageSender.send(eventJsonString);
        if (LOG.isDebugEnabled()) {
            LOG.debug("ESB JMS client sent message");
        }
        return true;
    }

    private boolean send(EsbEvent event) {
        jmsMessageSender.send(event);
        if (LOG.isDebugEnabled()) {
            LOG.debug("ESB JMS client sent message");
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
