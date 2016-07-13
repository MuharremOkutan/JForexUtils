package com.jforex.programming.order.test;

import static com.jforex.programming.order.OrderStaticUtil.buyOrderCommands;
import static com.jforex.programming.order.OrderStaticUtil.combinedDirection;
import static com.jforex.programming.order.OrderStaticUtil.combinedSignedAmount;
import static com.jforex.programming.order.OrderStaticUtil.direction;
import static com.jforex.programming.order.OrderStaticUtil.directionToCommand;
import static com.jforex.programming.order.OrderStaticUtil.instrumentPredicate;
import static com.jforex.programming.order.OrderStaticUtil.isCanceled;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isConditional;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isNoSLSet;
import static com.jforex.programming.order.OrderStaticUtil.isNoTPSet;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.OrderStaticUtil.ofInstrument;
import static com.jforex.programming.order.OrderStaticUtil.offerSideForOrderCommand;
import static com.jforex.programming.order.OrderStaticUtil.orderSLPredicate;
import static com.jforex.programming.order.OrderStaticUtil.orderTPPredicate;
import static com.jforex.programming.order.OrderStaticUtil.sellOrderCommands;
import static com.jforex.programming.order.OrderStaticUtil.signedAmount;
import static com.jforex.programming.order.OrderStaticUtil.slPriceWithPips;
import static com.jforex.programming.order.OrderStaticUtil.statePredicate;
import static com.jforex.programming.order.OrderStaticUtil.switchCommand;
import static com.jforex.programming.order.OrderStaticUtil.switchDirection;
import static com.jforex.programming.order.OrderStaticUtil.tpPriceWithPips;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.google.common.collect.Sets;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

public class OrderStaticUtilTest extends InstrumentUtilForTest {

    private final IOrderForTest buyOrderEURUSD = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrderEURUSD = IOrderForTest.sellOrderEURUSD();
    private Set<IOrder> orders;
    private final double currentPriceForSLTP = 1.32165;
    private final double pipsToSLTP = 17.4;

    @Before
    public void setUp() throws JFException {
        buyOrderEURUSD.setState(IOrder.State.FILLED);
        sellOrderEURUSD.setState(IOrder.State.FILLED);
        sellOrderEURUSD.setLabel("SecondOrderLabel");

        orders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    }

    private void assertSLTPCalculation(final IOrder order,
                                       final double calculatedPrice,
                                       final int factor) {
        final double expectedPrice = CalculationUtil.addPips(order.getInstrument(),
                                                             currentPriceForSLTP,
                                                             factor * pipsToSLTP);
        assertThat(calculatedPrice, equalTo(expectedPrice));
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(OrderStaticUtil.class);
    }

    @Test
    public void testBuyOrderCommandSet() {
        assertTrue(buyOrderCommands.contains(OrderCommand.BUY));
        assertTrue(buyOrderCommands.contains(OrderCommand.BUYLIMIT));
        assertTrue(buyOrderCommands.contains(OrderCommand.BUYLIMIT_BYBID));
        assertTrue(buyOrderCommands.contains(OrderCommand.BUYSTOP));
        assertTrue(buyOrderCommands.contains(OrderCommand.BUYSTOP_BYBID));
    }

    @Test
    public void testSellOrderCommandSet() {
        assertTrue(sellOrderCommands.contains(OrderCommand.SELL));
        assertTrue(sellOrderCommands.contains(OrderCommand.SELLLIMIT));
        assertTrue(sellOrderCommands.contains(OrderCommand.SELLLIMIT_BYASK));
        assertTrue(sellOrderCommands.contains(OrderCommand.SELLSTOP));
        assertTrue(sellOrderCommands.contains(OrderCommand.SELLSTOP_BYASK));
    }

    @Test
    public void testStatePredicateIsCorrect() {
        final Predicate<IOrder> orderCancelPredicate = statePredicate.apply(IOrder.State.CANCELED);

        buyOrderEURUSD.setState(IOrder.State.CANCELED);
        assertTrue(orderCancelPredicate.test(buyOrderEURUSD));

        buyOrderEURUSD.setState(IOrder.State.OPENED);
        assertFalse(orderCancelPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testOpenPredicateIsCorrect() {
        buyOrderEURUSD.setState(IOrder.State.OPENED);
        assertTrue(isOpened.test(buyOrderEURUSD));

        buyOrderEURUSD.setState(IOrder.State.CANCELED);
        assertFalse(isOpened.test(buyOrderEURUSD));
    }

    @Test
    public void testFilledPredicateIsCorrect() {
        buyOrderEURUSD.setState(IOrder.State.FILLED);
        assertTrue(isFilled.test(buyOrderEURUSD));

        buyOrderEURUSD.setState(IOrder.State.CANCELED);
        assertFalse(isFilled.test(buyOrderEURUSD));
    }

    @Test
    public void testClosedPredicateIsCorrect() {
        buyOrderEURUSD.setState(IOrder.State.CLOSED);
        assertTrue(isClosed.test(buyOrderEURUSD));

        buyOrderEURUSD.setState(IOrder.State.CANCELED);
        assertFalse(isClosed.test(buyOrderEURUSD));
    }

    @Test
    public void testCanceledPredicateIsCorrect() {
        buyOrderEURUSD.setState(IOrder.State.CANCELED);
        assertTrue(isCanceled.test(buyOrderEURUSD));
    }

    @Test
    public void testConditionalPredicateIsCorrect() {
        buyOrderEURUSD.setState(IOrder.State.OPENED);
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUYLIMIT);
        assertTrue(isConditional.test(buyOrderEURUSD));

        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        assertFalse(isConditional.test(buyOrderEURUSD));
    }

    @Test
    public void testSLPredicateIsCorrect() throws JFException {
        final double sl = 1.34521;
        final Predicate<IOrder> slPredicate = orderSLPredicate.apply(sl);

        buyOrderEURUSD.setStopLossPrice(sl);
        assertTrue(slPredicate.test(buyOrderEURUSD));

        buyOrderEURUSD.setStopLossPrice(sl + 0.1);
        assertFalse(slPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testTPPredicateIsCorrect() throws JFException {
        final double tp = 1.34521;
        final Predicate<IOrder> tpPredicate = orderTPPredicate.apply(tp);

        buyOrderEURUSD.setTakeProfitPrice(tp);
        assertTrue(tpPredicate.test(buyOrderEURUSD));

        buyOrderEURUSD.setTakeProfitPrice(tp + 0.1);
        assertFalse(tpPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsSLSetToPredicateCorrect() throws JFException {
        final double sl = 1.34521;
        final Predicate<IOrder> slPredicate = isSLSetTo(sl);

        buyOrderEURUSD.setStopLossPrice(sl);
        assertTrue(slPredicate.test(buyOrderEURUSD));

        buyOrderEURUSD.setStopLossPrice(sl + 0.1);
        assertFalse(slPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsTPSetToPredicateCorrect() throws JFException {
        final double tp = 1.34521;
        final Predicate<IOrder> tpPredicate = isTPSetTo(tp);

        buyOrderEURUSD.setTakeProfitPrice(tp);
        assertTrue(tpPredicate.test(buyOrderEURUSD));

        buyOrderEURUSD.setTakeProfitPrice(tp + 0.1);
        assertFalse(tpPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsNoSLSetPredicateCorrect() throws JFException {
        final double sl = 1.34521;
        final Predicate<IOrder> slPredicate = isNoSLSet;

        buyOrderEURUSD.setStopLossPrice(platformSettings.noSLPrice());
        assertTrue(slPredicate.test(buyOrderEURUSD));

        buyOrderEURUSD.setStopLossPrice(sl);
        assertFalse(slPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testIsNoTPSetPredicateCorrect() throws JFException {
        final double tp = 1.34521;
        final Predicate<IOrder> tpPredicate = isNoTPSet;

        buyOrderEURUSD.setTakeProfitPrice(platformSettings.noTPPrice());
        assertTrue(tpPredicate.test(buyOrderEURUSD));

        buyOrderEURUSD.setTakeProfitPrice(tp);
        assertFalse(tpPredicate.test(buyOrderEURUSD));
    }

    @Test
    public void testInstrumentPredicateIsCorrect() throws JFException {
        final Predicate<IOrder> predicate = instrumentPredicate.apply(instrumentEURUSD);

        assertTrue(predicate.test(buyOrderEURUSD));
        assertFalse(predicate.test(IOrderForTest.orderAUDUSD()));

        assertTrue(ofInstrument(instrumentEURUSD).test(buyOrderEURUSD));
        assertFalse(ofInstrument(instrumentAUDUSD).test(buyOrderEURUSD));
    }

    @Test
    public void testOrderDirectionIsFlatWhenOrderIsNull() {
        assertThat(direction(null), equalTo(OrderDirection.FLAT));
    }

    @Test
    public void testOrderDirectionIsLongForBuyCommand() {
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);

        assertThat(direction(buyOrderEURUSD), equalTo(OrderDirection.LONG));
    }

    @Test
    public void testOrderDirectionIsShortForSellCommand() {
        buyOrderEURUSD.setOrderCommand(OrderCommand.SELL);

        assertThat(direction(buyOrderEURUSD), equalTo(OrderDirection.SHORT));
    }

    @Test
    public void testCombinedDirectionIsLONGForBothOrdersHaveBuyCommands() {
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        sellOrderEURUSD.setOrderCommand(OrderCommand.BUY);

        assertThat(combinedDirection(orders), equalTo(OrderDirection.LONG));
    }

    @Test
    public void testCombinedDirectionIsLONGForBothOrdersHaveSellCommands() {
        buyOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        sellOrderEURUSD.setOrderCommand(OrderCommand.SELL);

        assertThat(combinedDirection(orders), equalTo(OrderDirection.SHORT));
    }

    @Test
    public void testCombinedDirectionIsFlatForBothOrdersCancelOut() {
        final double amount = 0.12;
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        sellOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        buyOrderEURUSD.setAmount(amount);
        sellOrderEURUSD.setAmount(amount);

        assertThat(combinedDirection(orders), equalTo(OrderDirection.FLAT));
    }

    @Test
    public void testCombinedDirectionIsLongWhenBuyIsBiggerThanSell() {
        final double buyAmount = 0.12;
        final double sellAmount = 0.11;
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        sellOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        buyOrderEURUSD.setAmount(buyAmount);
        sellOrderEURUSD.setAmount(sellAmount);

        assertThat(combinedDirection(orders), equalTo(OrderDirection.LONG));
    }

    @Test
    public void testCombinedDirectionIsShortWhenSellIsBiggerThanBuy() {
        final double buyAmount = 0.11;
        final double sellAmount = 0.12;
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        sellOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        buyOrderEURUSD.setAmount(buyAmount);
        sellOrderEURUSD.setAmount(sellAmount);

        assertThat(combinedDirection(orders), equalTo(OrderDirection.SHORT));
    }

    @Test
    public void testSignedAmountIsPositiveForBuyCommand() {
        final double buyAmount = 0.11;
        buyOrderEURUSD.setAmount(buyAmount);
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);

        assertThat(signedAmount(buyOrderEURUSD), equalTo(buyAmount));
        assertThat(signedAmount(buyAmount, OrderCommand.BUY), equalTo(buyAmount));
    }

    @Test
    public void testSignedAmountIsNegativeForSellCommand() {
        final double sellAmount = 0.11;
        buyOrderEURUSD.setAmount(sellAmount);
        buyOrderEURUSD.setOrderCommand(OrderCommand.SELL);

        assertThat(signedAmount(buyOrderEURUSD), equalTo(-sellAmount));
        assertThat(signedAmount(sellAmount, OrderCommand.SELL), equalTo(-sellAmount));
    }

    @Test
    public void testCombinedSignedAmountIsPositiveForBothOrdersHaveBuyCommands() {
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        sellOrderEURUSD.setOrderCommand(OrderCommand.BUY);

        assertThat(combinedSignedAmount(orders),
                   equalTo(buyOrderEURUSD.getAmount() + sellOrderEURUSD.getAmount()));
    }

    @Test
    public void testCombinedSignedAmountIsNegativeForBothOrdersHaveSellCommands() {
        buyOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        sellOrderEURUSD.setOrderCommand(OrderCommand.SELL);

        assertThat(combinedSignedAmount(orders),
                   equalTo(-buyOrderEURUSD.getAmount() - sellOrderEURUSD.getAmount()));
    }

    @Test
    public void testCombinedSignedAmountIsZeroForBothOrdersCancelOut() {
        final double amount = 0.12;
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        sellOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        buyOrderEURUSD.setAmount(amount);
        sellOrderEURUSD.setAmount(amount);

        assertThat(combinedSignedAmount(orders),
                   equalTo(0.0));
    }

    @Test
    public void testCombinedSignedAmountIsPositiveWhenBuyIsBiggerThanSell() {
        final double buyAmount = 0.12;
        final double sellAmount = 0.11;
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        sellOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        buyOrderEURUSD.setAmount(buyAmount);
        sellOrderEURUSD.setAmount(sellAmount);

        assertThat(combinedSignedAmount(orders),
                   equalTo(buyAmount - sellAmount));
    }

    @Test
    public void testCombinedSignedAmountIsNegativeWhenSellIsBiggerThanBuy() {
        final double buyAmount = 0.11;
        final double sellAmount = 0.12;
        buyOrderEURUSD.setOrderCommand(OrderCommand.BUY);
        sellOrderEURUSD.setOrderCommand(OrderCommand.SELL);
        buyOrderEURUSD.setAmount(buyAmount);
        sellOrderEURUSD.setAmount(sellAmount);

        assertThat(combinedSignedAmount(orders),
                   equalTo(buyAmount - sellAmount));
    }

    @Test
    public void testOfferSideForOrderCommandIsAskForBuyCommand() {
        assertThat(offerSideForOrderCommand(OrderCommand.BUY), equalTo(OfferSide.ASK));
    }

    @Test
    public void testOfferSideForOrderCommandIsBidForSellCommand() {
        assertThat(offerSideForOrderCommand(OrderCommand.SELL), equalTo(OfferSide.BID));
    }

    @Test
    public void testDirectionToCommand() {
        assertThat(directionToCommand(OrderDirection.LONG), equalTo(OrderCommand.BUY));
        assertThat(directionToCommand(OrderDirection.SHORT), equalTo(OrderCommand.SELL));
    }

    @Test
    public void testSwitchOrderCommandIsCorrect() {
        assertThat(switchCommand(OrderCommand.BUY), equalTo(OrderCommand.SELL));
        assertThat(switchCommand(OrderCommand.SELL), equalTo(OrderCommand.BUY));

        assertThat(switchCommand(OrderCommand.BUYLIMIT), equalTo(OrderCommand.SELLLIMIT));
        assertThat(switchCommand(OrderCommand.SELLLIMIT), equalTo(OrderCommand.BUYLIMIT));

        assertThat(switchCommand(OrderCommand.BUYLIMIT_BYBID),
                   equalTo(OrderCommand.SELLLIMIT_BYASK));
        assertThat(switchCommand(OrderCommand.SELLLIMIT_BYASK),
                   equalTo(OrderCommand.BUYLIMIT_BYBID));

        assertThat(switchCommand(OrderCommand.BUYSTOP), equalTo(OrderCommand.SELLSTOP));
        assertThat(switchCommand(OrderCommand.SELLSTOP), equalTo(OrderCommand.BUYSTOP));

        assertThat(switchCommand(OrderCommand.BUYSTOP_BYBID), equalTo(OrderCommand.SELLSTOP_BYASK));
        assertThat(switchCommand(OrderCommand.SELLSTOP_BYASK), equalTo(OrderCommand.BUYSTOP_BYBID));
    }

    @Test
    public void testSwitchOrderDirectionIsCorrect() {
        assertThat(switchDirection(OrderDirection.FLAT), equalTo(OrderDirection.FLAT));
        assertThat(switchDirection(OrderDirection.LONG), equalTo(OrderDirection.SHORT));
        assertThat(switchDirection(OrderDirection.SHORT), equalTo(OrderDirection.LONG));
    }

    @Test
    public void testCalculateSLPriceWithPipsIsCorrectForBuyOrder() {
        final double slPrice = slPriceWithPips(buyOrderEURUSD, currentPriceForSLTP, pipsToSLTP);

        assertSLTPCalculation(buyOrderEURUSD, slPrice, -1);
    }

    @Test
    public void testCalculateSLPriceWithPipsIsCorrectForSellOrder() {
        final double slPrice = slPriceWithPips(sellOrderEURUSD, currentPriceForSLTP, pipsToSLTP);

        assertSLTPCalculation(sellOrderEURUSD, slPrice, 1);
    }

    @Test
    public void testCalculateTPPriceWithPipsIsCorrectForBuyOrder() {
        final double tpPrice = tpPriceWithPips(buyOrderEURUSD, currentPriceForSLTP, pipsToSLTP);

        assertSLTPCalculation(buyOrderEURUSD, tpPrice, 1);
    }

    @Test
    public void testCalculateTPPriceWithPipsIsCorrectForSellOrder() {
        final double tpPrice = tpPriceWithPips(sellOrderEURUSD, currentPriceForSLTP, pipsToSLTP);

        assertSLTPCalculation(sellOrderEURUSD, tpPrice, -1);
    }
}
