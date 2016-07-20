package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public class PositionSingleTask {

    private final OrderUtilHandler orderUtilHandler;

    public PositionSingleTask(final OrderUtilHandler orderUtilHandler) {
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> setSLObservable(final IOrder orderToChangeSL,
                                                  final double newSL) {
        return Observable
                .just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(order))
                .flatMap(order -> orderUtilHandler
                        .callWithRetryObservable(new SetSLCommand(orderToChangeSL, newSL)));
    }

    public Observable<OrderEvent> setTPObservable(final IOrder orderToChangeTP,
                                                  final double newTP) {
        return Observable
                .just(orderToChangeTP)
                .filter(order -> !isTPSetTo(newTP).test(order))
                .flatMap(order -> orderUtilHandler
                        .callWithRetryObservable(new SetTPCommand(orderToChangeTP, newTP)));
    }

    public Observable<OrderEvent> closeObservable(final IOrder orderToClose) {
        return Observable
                .just(orderToClose)
                .filter(order -> !isClosed.test(order))
                .flatMap(order -> orderUtilHandler
                        .callWithRetryObservable(new CloseCommand(orderToClose)));
    }
}
