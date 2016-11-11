package com.jforex.programming.order.task.test;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.CancelSLTPAndMergeTask;
import com.jforex.programming.order.task.MergePositionTaskObservable;
import com.jforex.programming.order.task.params.position.MergeAllPositionsParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class MergePositionTaskObservableTest extends InstrumentUtilForTest {

    private MergePositionTaskObservable mergeTask;

    @Mock
    private CancelSLTPAndMergeTask splitterMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private MergeAllPositionsParams mergeAllPositionsParamsMock;
    private final Function<Instrument, MergePositionParams> paramsFactoryMock =
            instrument -> mergePositionParamsMock;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = mergeEvent;
    private Observable<OrderEvent> testObservable;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() {
        setUpMocks();

        mergeTask = new MergePositionTaskObservable(splitterMock, positionUtilMock);
    }

    private void setUpMocks() {
        when(positionUtilMock.filledOrders(instrumentEURUSD))
            .thenReturn(toMergeOrders);
        when(mergePositionParamsMock.instrument())
            .thenReturn(instrumentEURUSD);
        when(mergeAllPositionsParamsMock.paramsFactory())
            .thenReturn(paramsFactoryMock);
    }

    private void setUpSplitterObservable(final Observable<OrderEvent> splitterObservable) {
        when(splitterMock.observe(toMergeOrders, mergePositionParamsMock))
            .thenReturn(splitterObservable);
    }

    public class MergeCallSetup {

        @Before
        public void setUp() {
            testObservable = mergeTask.merge(toMergeOrders, mergePositionParamsMock);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(splitterMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @Test
        public void splitterMockIsCalledCorrect() {
            setUpSplitterObservable(eventObservable(testEvent));

            testObservable
                .test()
                .assertValue(testEvent);

            verify(splitterMock).observe(toMergeOrders, mergePositionParamsMock);
        }
    }

    public class MergePositionCallSetup {

        @Before
        public void setUp() {
            testObservable = mergeTask.merge(mergePositionParamsMock);
        }

        @Test
        public void mergePositionCallIsDeferred() {
            verifyZeroInteractions(splitterMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @Test
        public void splitterMockIsCalledCorrect() {
            setUpSplitterObservable(eventObservable(testEvent));

            testObservable
                .test()
                .assertValue(testEvent);

            verify(splitterMock).observe(toMergeOrders, mergePositionParamsMock);
            verify(positionUtilMock).filledOrders(instrumentEURUSD);
        }
    }

    public class MergeAllCallSetup {

        private List<Observable<OrderEvent>> closeObservables;

        private void mergeAllSubscribe() {
            testObserver = mergeTask
                .mergeAll(mergeAllPositionsParamsMock)
                .test();
        }

        private void setUpPositionUtilObservables(final Observable<OrderEvent> firstObservable,
                                                  final Observable<OrderEvent> secondObservable) {
            closeObservables = Stream
                .of(firstObservable, secondObservable)
                .collect(Collectors.toList());

            when(positionUtilMock.observablesFromFactory(any())).thenReturn(closeObservables);

            mergeAllSubscribe();
        }

        @Test
        public void mergePositionCallIsDeferred() {
            verifyZeroInteractions(splitterMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @Test
        public void verifyThatMergeCommandsAreMerged() {
            setUpPositionUtilObservables(neverObservable(),
                                         eventObservable(testEvent));

            testObserver.assertValue(testEvent);
            testObserver.assertNotComplete();
        }

        @Test
        public void completesWhenAllSingleCommandsComplete() {
            setUpPositionUtilObservables(emptyObservable(), emptyObservable());

            testObserver.assertComplete();
        }
    }
}