package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CloseTask;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.order.task.params.CloseParams;
import com.jforex.programming.order.task.params.ClosePositionParams;
import com.jforex.programming.order.task.params.ComplexMergeParams;
import com.jforex.programming.order.task.params.MergeParams;
import com.jforex.programming.order.task.params.SetAmountParams;
import com.jforex.programming.order.task.params.SetGTTParams;
import com.jforex.programming.order.task.params.SetLabelParams;
import com.jforex.programming.order.task.params.SetOpenPriceParams;
import com.jforex.programming.order.task.params.SetSLParams;
import com.jforex.programming.order.task.params.SubmitParams;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private BasicTask basicTaskMock;
    @Mock
    private MergeTask orderMergeTaskMock;
    @Mock
    private CloseTask orderCloseTaskMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ComplexMergeParams mergepositionParamsMock;
    @Mock
    private Function<Instrument, ComplexMergeParams> mergePositionParamsFactory;
    @Mock
    private Function<Instrument, ClosePositionParams> closePositionParamsFactory;
    private final IOrder orderForTest = buyOrderEURUSD;
    private Observable<OrderEvent> orderEventObservable;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        orderUtil = new OrderUtil(basicTaskMock,
                                  orderMergeTaskMock,
                                  orderCloseTaskMock,
                                  positionUtilMock);
    }

    @Test
    public void submitOrderCallsSubscribeOnSubmitParams() {
        final SubmitParams submitParamsMock = mock(SubmitParams.class);

        orderUtil.submitOrder(submitParamsMock);

        verify(submitParamsMock).subscribe(basicTaskMock);
    }

    @Test
    public void mergeOrdersCallsSubscribeOnMergeParams() {
        final MergeParams mergeParamsMock = mock(MergeParams.class);

        orderUtil.mergeOrders(mergeParamsMock);

        verify(mergeParamsMock).subscribe(basicTaskMock);
    }

    @Test
    public void mergeOrdersWithpositionParamsDelegatesToMergeTask() {
        when(orderMergeTaskMock.merge(toMergeOrders, mergepositionParamsMock))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergeOrders(toMergeOrders, mergepositionParamsMock);

        verify(orderMergeTaskMock).merge(toMergeOrders, mergepositionParamsMock);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closeCallsSubscribeOnCloseParams() {
        final CloseParams closeParams = mock(CloseParams.class);

        orderUtil.close(closeParams);

        verify(closeParams).subscribe(basicTaskMock);
    }

    @Test
    public void setLabelDelegatesToOrderTask() {
        final SetLabelParams setLabelParamsMock = mock(SetLabelParams.class);

        orderUtil.setLabel(setLabelParamsMock);

        verify(setLabelParamsMock).subscribe(basicTaskMock);
    }

    @Test
    public void setGTTCallsSubscribeOnSetGTTParams() {
        final SetGTTParams setGTTParamsMock = mock(SetGTTParams.class);

        orderUtil.setGoodTillTime(setGTTParamsMock);

        verify(setGTTParamsMock).subscribe(basicTaskMock);
    }

    @Test
    public void setRequestedAmountCallsSubscribeOnSetAmountParams() {
        final SetAmountParams setAmountParamsMock = mock(SetAmountParams.class);

        orderUtil.setRequestedAmount(setAmountParamsMock);

        verify(setAmountParamsMock).subscribe(basicTaskMock);
    }

    @Test
    public void setOpenPriceCallsSubscribeOnSetOpenPriceParams() {
        final SetOpenPriceParams setOpenPriceParamsMock = mock(SetOpenPriceParams.class);

        orderUtil.setOpenPrice(setOpenPriceParamsMock);

        verify(setOpenPriceParamsMock).subscribe(basicTaskMock);
    }

    @Test
    public void setSLDelegatesToOrderTask() {
        final double newSL = 1.1234;
        when(basicTaskMock.setStopLossPrice(orderForTest, newSL))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setStopLossPrice(orderForTest, newSL);

        verify(basicTaskMock).setStopLossPrice(orderForTest, newSL);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setSLForPipsDelegatesToBasicTask() {
        final double pips = 12.3;
        when(basicTaskMock.setStopLossForPips(orderForTest, pips))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setStopLossForPips(orderForTest, pips);

        verify(basicTaskMock).setStopLossForPips(orderForTest, pips);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setSLWithParamsDelegatesToBasicTask() {
        final double newSL = 1.1234;
        final SetSLParams setSLParams = SetSLParams
            .newBuilder(orderForTest, newSL)
            .build();

        when(basicTaskMock.setStopLossPrice(setSLParams))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setStopLossPrice(setSLParams);

        verify(basicTaskMock).setStopLossPrice(setSLParams);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setTPDelegatesToOrderTask() {
        final double newTP = 1.1234;
        when(basicTaskMock.setTakeProfitPrice(orderForTest, newTP))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setTakeProfitPrice(orderForTest, newTP);

        verify(basicTaskMock).setTakeProfitPrice(orderForTest, newTP);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void setTPForPipsDelegatesToBasicTask() {
        final double pips = 12.3;
        when(basicTaskMock.setTakeProfitForPips(orderForTest, pips))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.setTakeProfitForPips(orderForTest, pips);

        verify(basicTaskMock).setTakeProfitForPips(orderForTest, pips);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void mergePositionDelegatesToMergeTask() {
        when(orderMergeTaskMock.mergePosition(instrumentEURUSD, mergepositionParamsMock))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable =
                orderUtil.mergePosition(instrumentEURUSD, mergepositionParamsMock);

        verify(orderMergeTaskMock).mergePosition(instrumentEURUSD, mergepositionParamsMock);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void mergeAllPositionsDelegatesToMergeTask() {
        when(orderMergeTaskMock.mergeAllPositions(mergePositionParamsFactory))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.mergeAllPositions(mergePositionParamsFactory);

        verify(orderMergeTaskMock).mergeAllPositions(mergePositionParamsFactory);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closePositionDelegatesToCloseTask() {
        final ClosePositionParams positionParams = mock(ClosePositionParams.class);

        when(orderCloseTaskMock.close(positionParams))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.closePosition(positionParams);

        verify(orderCloseTaskMock).close(positionParams);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void closeAllPositionsDelegatesToCloseTask() {
        when(orderCloseTaskMock.closeAllPositions(closePositionParamsFactory))
            .thenReturn(orderEventObservable);

        final Observable<OrderEvent> actualObservable = orderUtil.closeAllPositions(closePositionParamsFactory);

        verify(orderCloseTaskMock).closeAllPositions(closePositionParamsFactory);
        assertThat(actualObservable, equalTo(orderEventObservable));
    }

    @Test
    public void positionOrdersDelegatesToPositionTask() {
        final PositionOrders positionOrders = mock(PositionOrders.class);
        when(positionUtilMock.positionOrders(instrumentEURUSD))
            .thenReturn(positionOrders);

        final PositionOrders actualPositionOrders = orderUtil.positionOrders(instrumentEURUSD);

        verify(positionUtilMock).positionOrders(instrumentEURUSD);
        assertThat(actualPositionOrders, equalTo(positionOrders));
    }
}
