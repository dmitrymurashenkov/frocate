package com.frocate.money.transfer.test.expect;

import com.frocate.money.transfer.balance.*;
import org.junit.Test;

import static com.frocate.money.transfer.test.expect.ExpectedTx.createTx;
import static org.junit.Assert.fail;

public class AssertingBalanceServiceTest
{
    private final AssertingBalanceService service = new AssertingBalanceService(AccountGenerator.withBalance(10, 100));

    @Test
    public void assert_shouldSucceed_ifAllTxPerformed() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.expect(createTx("txId1").transfer("1", "2", 5).build());
        service.expect(createTx("txId2").transfer("3", "4", 7).build());
        service.debit("txId1", "1", -5);
        service.debit("txId1", "2", 5);
        service.debit("txId2", "3", -7);
        service.debit("txId2", "4", 7);
        service.assertAllTxPerformed();
    }

    @Test(expected = AssertionError.class)
    public void assert_shouldThrowException_ifUnexpectedTxPerformed() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.debit("txId1", "1", -5);
        service.assertAllTxPerformed();
    }

    @Test
    public void assert_shouldSucceed_ifTxRolledBack() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.expect(createTx("txId1")
                .transfer("1", "2", 5)
                .throwRollbackDuringDebit()
                .build()
        );
        service.debit("txId1", "1", -5);
        try
        {
            service.debit("txId1", "2", 5);
        }
        catch (RollbackTxException e)
        {
            //ok
            service.debit("txId1", "1", 5);
        }
        service.assertAllTxPerformed();
    }

    @Test
    public void assert_shouldSucceed_ifUnknownExceptionThrown() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.expect(createTx("txId1")
                .transfer("1", "2", 5)
                .throwExceptionsDuringDebit(1)
                .build()
        );
        service.debit("txId1", "1", -5);
        try
        {
            service.debit("txId1", "2", 5);
        }
        catch (UnknownException e)
        {
            service.debit("txId1", "2", 5);
        }
        service.assertAllTxPerformed();
    }

    @Test(expected = AssertionError.class)
    public void assert_shouldThroException_ifNoRetryAfterUnknownExceptionThrown() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.expect(createTx("txId1")
                .transfer("1", "2", 5)
                .throwExceptionsDuringDebit(1)
                .build()
        );
        service.debit("txId1", "1", -5);
        try
        {
            service.debit("txId1", "2", 5);
        }
        catch (UnknownException e)
        {
            //ok
        }
        service.assertAllTxPerformed();
    }

    @Test(expected = AssertionError.class)
    public void assert_shouldThrowException_ifTxNotRolledBack() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.expect(createTx("txId1")
                .transfer("1", "2", 5)
                .throwRollbackDuringDebit()
                .build()
        );
        service.debit("txId1", "1", -5);
        try
        {
            service.debit("txId1", "2", 5);
        }
        catch (RollbackTxException e)
        {
            //ok

        }
        service.assertAllTxPerformed();
    }

    @Test(expected = AssertionError.class)
    public void assert_shouldThrowException_ifNotAllOperationsPerformed() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.expect(createTx("txId").transfer("1", "2", 5).build());
        service.debit("txId", "1", -5);
        service.assertAllTxPerformed();
    }

    @Test(expected = AssertionError.class)
    public void assert_shouldThrowException_ifNotAllTxPerformed() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.expect(createTx("txId1").transfer("1", "2", 5).build());
        service.expect(createTx("txId2").transfer("3", "4", 5).build());
        service.debit("txId1", "1", -5);
        service.debit("txId1", "2", 5);
        service.assertAllTxPerformed();
    }

    @Test(expected = AssertionError.class)
    public void assert_shouldThrowException_ifWrongAmount() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.expect(createTx("txId").transfer("1", "2", 5).build());
        service.debit("txId", "1", 1);
        service.debit("txId", "2", 5);
        service.assertAllTxPerformed();
    }

    @Test
    public void debit_shouldThrowException_ifUnknownExceptionExpected() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        int expectedException = 3;
        service.expect(createTx("txId").transfer("1", "2", 5)
                .throwExceptionsDuringCredit(expectedException)
                .build());
        for (int i = 0; i < expectedException; i++)
        {
            try
            {
                service.debit("txId", "1", -5);
                fail("Exception expected");
            }
            catch (UnknownException e)
            {
                //ok
            }
        }
        service.debit("txId", "1", -5);
        service.debit("txId", "2", 5);
        service.assertAllTxPerformed();
    }

    @Test(expected = RollbackTxException.class)
    public void debit_shouldThrowException_ifRollbackExpected() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.expect(createTx("txId").transfer("1", "2", 5)
                .throwRollbackDuringCredit()
                .build());
        service.debit("txId", "1", -5);
    }

    @Test
    public void debit_shouldThrowRollbackAfterUnknownExceptions() throws RollbackTxException, UnknownException, NotEnoughMoneyException, TooManyErrorsException, RequestMalformedException
    {
        service.expect(createTx("txId").transfer("1", "2", 5)
                .throwExceptionsDuringCredit(1)
                .throwRollbackDuringCredit()
                .build());
        try
        {
            service.debit("txId", "1", -5);
            fail("Exception expected");
        }
        catch (UnknownException e)
        {
            //ok
        }
        try
        {
            service.debit("txId", "1", -5);
            fail("Exception expected");
        }
        catch (RollbackTxException e)
        {
            //ok
        }
    }
}