package com.frocate.money.transfer.test.expect;

import com.frocate.money.transfer.balance.RollbackTxException;
import com.frocate.money.transfer.balance.UnknownException;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExpectedTxTest
{
    @Test
    public void createTx_shouldGenerateUniqueTxId()
    {
        ExpectedTx tx1 = ExpectedTx.createTx().build();
        ExpectedTx tx2 = ExpectedTx.createTx().build();
        assertFalse(tx1.getId().equals(tx2.getId()));
    }

    @Test
    public void transfer_shouldGenerateDebitAndCreditOperations()
    {
        ExpectedTx tx = ExpectedTx.createTx()
                .transfer("1", "2", 5)
                .build();

        assertEquals(2, tx.operationsCount());
        ExpectedOperation credit = tx.getOperation("1");
        ExpectedOperation debit = tx.getOperation("2");

        assertEquals("1", credit.accountId);
        assertEquals(-5, credit.amount);
        assertEquals(0, credit.unknownExceptionsToThrow);
        assertFalse(credit.throwRollbackTxException);

        assertEquals("2", debit.accountId);
        assertEquals(5, debit.amount);
        assertEquals(0, debit.unknownExceptionsToThrow);
        assertFalse(debit.throwRollbackTxException);
    }

    @Test
    public void transfer_shouldGenerateCreditExceptions()
    {
        ExpectedTx tx = ExpectedTx.createTx()
                .transfer("1", "2", 5)
                .throwExceptionsDuringCredit(1)
                .throwRollbackDuringCredit()
                .build();

        assertEquals(2, tx.operationsCount());
        ExpectedOperation credit = tx.getOperation("1");
        ExpectedOperation debit = tx.getOperation("2");

        assertEquals("1", credit.accountId);
        assertEquals(-5, credit.amount);
        assertEquals(1, credit.unknownExceptionsToThrow);
        assertTrue(credit.throwRollbackTxException);

        assertEquals("2", debit.accountId);
        assertEquals(5, debit.amount);
        assertEquals(0, debit.unknownExceptionsToThrow);
        assertFalse(debit.throwRollbackTxException);
    }

    @Test
    public void transfer_shouldGenerateDebitExceptions()
    {
        ExpectedTx tx = ExpectedTx.createTx()
                .transfer("1", "2", 5)
                .throwExceptionsDuringDebit(1)
                .throwRollbackDuringDebit()
                .build();

        assertEquals(2, tx.operationsCount());
        ExpectedOperation credit = tx.getOperation("1");
        ExpectedOperation debit = tx.getOperation("2");

        assertEquals("1", credit.accountId);
        assertEquals(-5, credit.amount);
        assertEquals(0, credit.unknownExceptionsToThrow);
        assertFalse(credit.throwRollbackTxException);

        assertEquals("2", debit.accountId);
        assertEquals(5, debit.amount);
        assertEquals(1, debit.unknownExceptionsToThrow);
        assertTrue(debit.throwRollbackTxException);
    }

    @Test
    public void onPerform_shouldThrowSpecifiedNumberOfExceptions() throws RollbackTxException, UnknownException
    {
        int expectedExceptions = 3;
        ExpectedTx tx = ExpectedTx.createTx()
                .transfer("1", "2", 5)
                .throwExceptionsDuringCredit(expectedExceptions)
                .build();

        ExpectedOperation credit = tx.getOperation("1");

        for (int i = 0; i < expectedExceptions; i++)
        {
            try
            {
                credit.onPerform();
                fail("Exception expected");
            }
            catch (UnknownException e)
            {
                //ok
            }
        }
        credit.onPerform();
    }

    @Test(expected = RollbackTxException.class)
    public void onPerform_shouldThrowRollback() throws RollbackTxException, UnknownException
    {
        ExpectedTx tx = ExpectedTx.createTx()
                .transfer("1", "2", 5)
                .throwRollbackDuringCredit()
                .build();

        ExpectedOperation credit = tx.getOperation("1");
        credit.onPerform();
    }

    @Test(expected = RollbackTxException.class)
    public void onPerform_shouldThrowRollbackAfterUnknownExceptions() throws RollbackTxException, UnknownException
    {
        ExpectedTx tx = ExpectedTx.createTx()
                .transfer("1", "2", 5)
                .throwExceptionsDuringCredit(1)
                .throwRollbackDuringCredit()
                .build();

        ExpectedOperation credit = tx.getOperation("1");
        try
        {
            credit.onPerform();
            fail("Exception expected");
        }
        catch (UnknownException e)
        {
            //ok
        }
        catch (RollbackTxException e)
        {
            fail("Unknown exception was expected during first invocation");
        }
        credit.onPerform();
    }
}
