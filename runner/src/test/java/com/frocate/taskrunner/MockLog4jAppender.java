package com.frocate.taskrunner;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

public class MockLog4jAppender extends AbstractAppender
{
    private final List<LogEvent> events = new ArrayList<>();
    private final Class category;

    public MockLog4jAppender(Class category)
    {
        super("MockAppender", null, null);
        this.category = category;
        Logger logger = (Logger) LogManager.getLogger(category);
        logger.addAppender(this);
        start();
    }

    @Override
    public void append(LogEvent event)
    {
        //log4j uses mutable events so we need to store copies
        events.add(new Log4jLogEvent.Builder(event).build());
    }

    public void close()
    {
        Logger logger = (Logger) LogManager.getLogger(category);
        logger.removeAppender(this);
    }

    public void assertNoMessagesWithLevel(Level level)
    {
        for (LogEvent event : events)
        {
            if (event.getLevel() == level)
            {
                fail("Message with level " + level + " found: " + event.getMessage().getFormattedMessage());
            }
        }
    }

    public List<LogEvent> getEventsWithLevel(Level level)
    {
        return events.stream().filter(logEvent -> logEvent.getLevel() == level).collect(Collectors.toList());
    }

    public void assertContainsMessage(Level level, String containsText)
    {
        for (LogEvent event : events)
        {
            if (event.getLevel() == level
                    && event.getMessage().getFormattedMessage().contains(containsText))
            {
                return;
            }
        }
        fail("Message of level " + level + " containing text '" + containsText + "' not found");
    }
}