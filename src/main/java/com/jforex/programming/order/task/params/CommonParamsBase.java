package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class CommonParamsBase implements RetryParams {

    protected Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;
    protected final int noOfRetries;
    protected final long delayInMillis;

    protected CommonParamsBase(final CommonParamsBuilder<?> builder) {
        consumerForEvent = builder.consumerForEvent;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    @Override
    public int noOfRetries() {
        return noOfRetries;
    }

    @Override
    public long delayInMillis() {
        return delayInMillis;
    }
}
