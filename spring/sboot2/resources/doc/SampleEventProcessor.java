package com.sas.mkt.apigw.sdk.streaming.agent.listener.thirdparty;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.sas.mkt.apigw.sdk.streaming.agent.event.command.StartEvent;
import com.sas.mkt.apigw.sdk.streaming.agent.event.command.StopEvent;
import com.sas.mkt.apigw.sdk.streaming.agent.event.status.StartedEvent;
import com.sas.mkt.apigw.sdk.streaming.agent.event.status.StoppedEvent;
import com.sas.mkt.apigw.sdk.streaming.agent.listener.BaseEventProcessor;

/**
 * Third party plug-in simulation. No Agent specific dependencies.
 *
 * @author Scott.Lynch@sas.com
 *
 */
@Component
public class SampleEventProcessor extends BaseEventProcessor {
	private final static Logger logger = LoggerFactory.getLogger(SampleEventProcessor.class);

    /**
     * Handle an event from the stream
     * @param event JSONObject event data from stream
     */
    @Async
    @EventListener
    public void handleEvent(JSONObject event) {
        logger.info("received:" + event);
    }

	@Override
	@EventListener
	public void handleStartEvent(StartEvent event) {
		/* Add initialization work here */
		logger.trace("received:" + event);

		publishStatusEvent(new StartedEvent(), logger);
	}

	@Override
	@EventListener
	public void handleStopEvent(StopEvent event) {
		/* Add shutdown work here */
		logger.trace("received:" + event);

		publishStatusEvent(new StoppedEvent(), logger);
	}

}
