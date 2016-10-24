package com.frocate.taskrunner.result;

public class TestResult
{
    private String name;
    private boolean success;
    private String error;

    public TestResult(String name)
    {
        this(name, true, null);
    }

    public TestResult(String name, boolean success, String error)
    {
        if (success && error != null)
        {
            throw new IllegalArgumentException("If frocate is success then erro message must be null");
        }
        else if (!success && error == null)
        {
            throw new IllegalArgumentException("If frocate failed then error must be provided");
        }
        this.name = name;
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getName()
    {
        return name;
    }

    public String getError()
    {
        return error == null ? "" : error;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        TestResult that = (TestResult) o;

        if (success != that.success)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        return error != null ? error.equals(that.error) : that.error == null;

    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return name + " " + (success ? "OK" : "FAIL " + error);
    }
}
