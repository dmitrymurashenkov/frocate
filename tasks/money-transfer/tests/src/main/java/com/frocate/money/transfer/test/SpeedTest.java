package com.frocate.money.transfer.test;

import com.frocate.money.transfer.balance.AccountGenerator;
import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.MetricBuilder;
import com.frocate.taskrunner.result.Range;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.frocate.money.transfer.balance.BalanceServiceParams.TOO_MANY_EXCEPTIONS_COUNT;
import static com.frocate.money.transfer.balance.BalanceServiceParams.TOO_MANY_EXCEPTIONS_TIMEOUT_MS;
import static com.frocate.money.transfer.test.expect.ExpectedTx.createTx;

public class SpeedTest extends FunctionalTest
{
    @Test(timeout = 30*1000)
    public void tooManyErrors() throws Throwable
    {
        int exceptions = 7;
        long minPossibleTimeMs = minTimeToDebitWithExceptions(exceptions);

        Metric result = new MetricBuilder()
                .name( "TOO_MANY_ERRORS handling (" + exceptions + " exceptions)")
                .unit("ms")
                .description("Single transaction performed, but several unknown exceptions are thrown in process causing TOO_MANY_ERRORS, " +
                        "min possible time is " + minPossibleTimeMs + "ms")
                .excellentRange(new Range(0, minPossibleTimeMs + 500))
                .goodRange(new Range(minPossibleTimeMs + 501, minPossibleTimeMs + 2000))
                .appendResultTo(MoneyTransferTask.metrics)
                .calculate(() -> tooManyErrors_test(exceptions));
        System.out.println(result);
    }

    private long tooManyErrors_test(int exceptions)
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx().transfer("1", "2", 5)
                .throwExceptionsDuringCredit(exceptions)
                .build());
        long startTime = System.nanoTime();
        service.transfer(tx);
        long endTime = System.nanoTime();
        balance.assertAllTxPerformed();
        return TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
    }

    @Test(timeout = 30*1000)
    public void concurrentOperationsOnSingleAccount() throws Throwable
    {
        int transactions = 3;
        long processingDelayMs = 1000;
        long minPossibleTimeMs = processingDelayMs;

        Metric result = new MetricBuilder()
                .name("Concurrent processing of single money (" + transactions + " transactions, " + processingDelayMs + "ms processing delay)")
                .unit("ms")
                .description("Several transactions move money from single money, server has some processing delay, " +
                        "min possible time is " + minPossibleTimeMs + "ms")
                .excellentRange(new Range(0, processingDelayMs + 500))
                .goodRange(new Range(processingDelayMs + 501, transactions*processingDelayMs + 2000))
                .appendResultTo(MoneyTransferTask.metrics)
                .calculate(() -> concurrentOperationsOnSingleAccount_test(transactions, processingDelayMs));
        System.out.println(result);
    }

    private long concurrentOperationsOnSingleAccount_test(int transactions, long processingDelayMs) throws Exception
    {
        startBootstrap(AccountGenerator.withBalance(5, 1000));
        List<TxService.TransferFuture> futures = new ArrayList<>();
        long startTime = System.nanoTime();
        for (int i = 0; i < transactions; i++)
        {
            TxService.Tx tx = balance.expect(createTx()
                    .transfer("1", "2", 1)
                    .processingTimeDebitMs(processingDelayMs)
                    .processingTimeCreditMs(processingDelayMs)
                    .build());
            futures.add(service.transferAsync(tx));
        }
        for (TxService.TransferFuture future : futures)
        {
            future.get(30, TimeUnit.SECONDS);
        }
        long endTime = System.nanoTime();
        balance.assertAllTxPerformed();
        return TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
    }

    @Test(timeout = 30*1000)
    public void concurrentRequestsWithProcessingDelay() throws Throwable
    {
        int transactions = 100;
        long processingDelay = 5000;
        long minPossibleTimeMs = processingDelay;

        Metric result = new MetricBuilder()
                .name("Concurrent processing of multiple accounts (" + transactions + " transactions, " + processingDelay + "ms processing delay)")
                .unit("ms")
                .description("Several concurrent requests are sent, but balance service takes long time to answer each," +
                        "min possible time is " + minPossibleTimeMs + "ms")
                .excellentRange(new Range(0, minPossibleTimeMs + 1000))
                .goodRange(new Range(minPossibleTimeMs + 1001, processingDelay*2))
                .appendResultTo(MoneyTransferTask.metrics)
                .calculate(() -> concurrentRequestsWithProcessingDelay_test(transactions, processingDelay));
        System.out.println(result);
    }

    private long concurrentRequestsWithProcessingDelay_test(int transactions, long processingDelayMs) throws Exception
    {
        startBootstrap(AccountGenerator.withBalance(1000, 1));
        List<TxService.TransferFuture> futures = new ArrayList<>();
        long startTime = System.nanoTime();
        for (int i = 0; i < transactions; i++)
        {
            TxService.Tx tx = balance.expect(createTx()
                    .transfer(i + "", (i + 1) + "", 1)
                    .processingTimeDebitMs(processingDelayMs)
                    .processingTimeCreditMs(processingDelayMs)
                    .build());
            futures.add(service.transferAsync(tx));
        }
        for (TxService.TransferFuture future : futures)
        {
            future.get(30, TimeUnit.SECONDS);
        }
        long endTime = System.nanoTime();
        balance.assertAllTxPerformed();
        return TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
    }

    @Test(timeout = 20*1000)
    public void quickRollback() throws Throwable
    {
        int exceptions = 8;
        long delayBeforeRollback = 1000;
        long minPossibleTimeMs = delayBeforeRollback;
        long minAchievableTimeMs = TOO_MANY_EXCEPTIONS_TIMEOUT_MS + delayBeforeRollback;
        if (delayBeforeRollback > minTimeToDebitWithExceptions(exceptions))
        {
            throw new RuntimeException("Test has incorrect settings");
        }

        Metric result = new MetricBuilder()
                .name("Quick rollback (" + delayBeforeRollback + "ms till rollback exception, " + exceptions + " exceptions)")
                .unit("ms")
                .description("Single request, but with several transfers, some of which will end with errors, " +
                        "take long time to complete or will require rollback, " +
                        "min possible time in the best case is " + delayBeforeRollback + "ms, " +
                        "but depending on TOO_MANY_ERRORS timeouts intersection min possible time in the worst case " +
                        "is " + minAchievableTimeMs + "ms")
                .excellentRange(new Range(0, minAchievableTimeMs + 1000))
                .goodRange(new Range(minAchievableTimeMs + 1001, minTimeToDebitWithExceptions(exceptions) + 1000))
                .appendResultTo(MoneyTransferTask.metrics)
                .calculate(() -> quickRollback_test(delayBeforeRollback, exceptions));
        System.out.println(result);
    }

    public long quickRollback_test(long delayBeforeRollback, int exceptions) throws Exception
    {
        startBootstrap(AccountGenerator.withBalance(10, 100));
        TxService.Tx tx = balance.expect(createTx()
                .transfer("1", "2", 10)
                .processingTimeCreditMs(Math.max(0, delayBeforeRollback - 100))
                .processingTimeDebitMs(Math.max(0, delayBeforeRollback - 100))
                .transfer("3", "4", 10)
                .throwExceptionsDuringCredit(exceptions)
                .throwExceptionsDuringDebit(exceptions)
                .transfer("5", "6", 10)
                .processingTimeDebitMs(delayBeforeRollback)
                .throwRollbackDuringDebit()
                .build());

        long startTime = System.nanoTime();
        service.transfer(tx);
        long endTime = System.nanoTime();
        balance.assertAllTxPerformed();
        return TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
    }

    /**
     * Returns min possible time to perform debit operation that will throw specified number of exceptions.
     *
     * If total K unknown exceptions would be thrown then
     * First N call which result in exceptions till too many errors threshold reach take 0ms time
     * Next M = K - N call must be performed with a delay and will result in exceptions also
     * Next call will succeed, but also must be performed after a delay
     */
    private long minTimeToDebitWithExceptions(int exceptions)
    {
        return (exceptions - TOO_MANY_EXCEPTIONS_COUNT + 1) * TOO_MANY_EXCEPTIONS_TIMEOUT_MS;
    }
}
