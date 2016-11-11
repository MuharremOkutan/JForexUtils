package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.position.MergeAllPositionsParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

import io.reactivex.functions.Action;

public class MergeAllPositionsParamsTest extends CommonParamsForTest {

    private MergeAllPositionsParams mergeAllPositionsParams;

    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private Action actionMock;
    @Mock
    private Function<IOrder, Action> actionConsumerMock;
    @Mock
    private Consumer<Throwable> errorConsumerMock;
    @Mock
    private BiConsumer<Throwable, IOrder> biErrorConsumerMock;
    @Mock
    private Consumer<OrderEvent> eventConsumerMock;
    private final IOrder orderForTest = buyOrderEURUSD;
    private static final int noOfRetries = 3;
    private static final long delayInMillis = 1500L;

    private final Function<Instrument, MergePositionParams> paramsFactory =
            instrument -> mergePositionParamsMock;

    @Before
    public void setUp() {
        when(actionConsumerMock.apply(orderForTest)).thenReturn(actionMock);

        mergeAllPositionsParams = MergeAllPositionsParams
            .newBuilder(paramsFactory)

            .doOnMergeAllPositionsStart(actionMock)
            .doOnMergeAllPositionsComplete(actionMock)
            .doOnMergeAllPositionsError(errorConsumerMock)
            .retryOnMergeAllPositionsReject(noOfRetries, delayInMillis)

            .build();
    }

    private void assertComposeParams(final ComposeParams composeParams) {
        assertActions(composeParams);
        assertErrorConsumer(composeParams.errorConsumer());
        assertRetries(composeParams.retryParams());
    }

    private void assertActions(final ComposeParams composeParams) {
        assertThat(composeParams.startAction(), equalTo(actionMock));
        assertThat(composeParams.completeAction(), equalTo(actionMock));
    }

    private void assertErrorConsumer(final Consumer<Throwable> errorConsumer) {
        assertThat(errorConsumer, equalTo(errorConsumerMock));
    }

    private void assertRetries(final RetryParams retryParams) {
        assertThat(retryParams.noOfRetries(), equalTo(noOfRetries));
        assertThat(retryParams.delayInMillis(), equalTo(delayInMillis));
    }

    @Test
    public void defaultValuesAreCorrect() {
        mergeAllPositionsParams = MergeAllPositionsParams
            .newBuilder(paramsFactory)
            .build();

        assertThat(mergeAllPositionsParams.paramsFactory(), equalTo(paramsFactory));
    }

    @Test
    public void assertSpecifiedValues() {
        assertThat(mergeAllPositionsParams.paramsFactory(), equalTo(paramsFactory));
    }

    @Test
    public void assertMergeAllPositionsValues() {
        assertComposeParams(mergeAllPositionsParams.mergeAllPositionsComposeParams());
    }
}