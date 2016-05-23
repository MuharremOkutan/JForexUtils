package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderCreateUtilTest extends InstrumentUtilForTest {

    private OrderCreateUtil orderCreateUtil;

    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    @Captor
    private ArgumentCaptor<OrderEventTypeData> typeDataCaptor;
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderCreateUtil = new OrderCreateUtil(engineMock, orderUtilHandlerMock);
    }

    private void captureAndRunOrderCall() throws JFException {
        verify(orderUtilHandlerMock).runOrderSupplierCall(orderCallCaptor.capture(), typeDataCaptor.capture());
        orderCallCaptor.getValue().get();
    }

    public class SubmitSetup {

        private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
        private final TestSubscriber<OrderEvent> submitSubscriber = new TestSubscriber<>();
        private final Observable<OrderEvent> submitObservable = Observable.empty();
        private final OrderEventTypeData submitTypeData = OrderEventTypeData.submitData;

        public class SubmitCall {

            @Before
            public void setUp() throws JFException {
                when(orderUtilHandlerMock
                        .runOrderSupplierCall(orderCallCaptor.capture(), eq(submitTypeData)))
                                .thenReturn(submitObservable);

                orderCreateUtil.submitOrder(orderParamsBUY).subscribe(submitSubscriber);

                captureAndRunOrderCall();
            }

            @Test
            public void testSubmitCallsOnIEngine() throws JFException {
                engineForTest.verifySubmit(orderParamsBUY, 1);
            }

            @Test
            public void testSubmitCallsHandlerWithCorrectTypeData() {
                final OrderEventTypeData orderEventTypeData = typeDataCaptor.getValue();

                assertThat(orderEventTypeData, equalTo(submitTypeData));
            }

            @Test
            public void testSuscriberIsNotifiesOnEvent() {
                orderEventSubject.onCompleted();

                submitSubscriber.assertCompleted();
            }
        }
    }

    public class MergeSetup {

        private final Set<IOrder> mergeOrders = Sets.newHashSet();
        private final String mergeOrderLabel = "MergeLabel";
        private final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();
        private final Observable<OrderEvent> mergeObservable = Observable.empty();
        private final OrderEventTypeData mergeTypeData = OrderEventTypeData.mergeData;

        public class MergeCall {

            @Before
            public void setUp() throws JFException {
                when(orderUtilHandlerMock
                        .runOrderSupplierCall(orderCallCaptor.capture(), eq(mergeTypeData)))
                                .thenReturn(mergeObservable);

                orderCreateUtil.mergeOrders(mergeOrderLabel, mergeOrders).subscribe(mergeSubscriber);

                captureAndRunOrderCall();
            }

            @Test
            public void testMergeCallsOnIEngine() throws JFException {
                engineForTest.verifyMerge(mergeOrderLabel, mergeOrders, 1);
            }

            @Test
            public void testMergeCallsHandlerWithCorrectTypeData() {
                final OrderEventTypeData orderEventTypeData = typeDataCaptor.getValue();

                assertThat(orderEventTypeData, equalTo(mergeTypeData));
            }

            @Test
            public void testSuscriberIsNotifiesOnEvent() {
                orderEventSubject.onCompleted();

                mergeSubscriber.assertCompleted();
            }
        }
    }
}