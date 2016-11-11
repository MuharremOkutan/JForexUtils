package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public abstract class CommonParamsBase {

    protected ComposeParams composeParams;
    protected Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    protected CommonParamsBase(final BasicParamsBuilder<?> builder) {
        composeParams = builder.composeParams;
        consumerForEvent = builder.consumerForEvent;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    public ComposeParams composeParams() {
        return composeParams;
    }
}