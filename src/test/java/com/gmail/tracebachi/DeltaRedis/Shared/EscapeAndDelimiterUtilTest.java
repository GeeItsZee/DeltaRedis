package com.gmail.tracebachi.DeltaRedis.Shared;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/10/16.
 */
public class EscapeAndDelimiterUtilTest
{
    private EscapeAndDelimiterUtil util = EscapeAndDelimiterUtil.COMMA_SEPARATED;

    @Test
    public void testEscapeAndDelimit() throws Exception
    {
        List<String> input;

        input = Collections.emptyList();
        assertEquals("", util.escapeAndDelimit(input));

        input = Arrays.asList("a", "b", "c");
        assertEquals("a,b,c", util.escapeAndDelimit(input));

        input = Arrays.asList("a");
        assertEquals("a", util.escapeAndDelimit(input));

        input = Arrays.asList("abc");
        assertEquals("abc", util.escapeAndDelimit(input));

        input = Arrays.asList("a", "b,", ",c,");
        assertEquals("a,b\\,,\\,c\\,", util.escapeAndDelimit(input));

        input = Arrays.asList(",,", "\\", "\\,,", ",,\\", ",\\,");
        assertEquals(
            "\\,\\,,\\\\,\\\\\\,\\,,\\,\\,\\\\,\\,\\\\\\,",
            util.escapeAndDelimit(input));
    }

    @Test
    public void testUnescapeAndUndelimit() throws Exception
    {
        List<String> output;

        output = util.unescapeAndUndelimit("");
        assertEquals("", stringListAsString(output));

        output = util.unescapeAndUndelimit("a,b,c");
        assertEquals("a  b  c", stringListAsString(output));

        output = util.unescapeAndUndelimit("a,");
        assertEquals("a  ", stringListAsString(output));

        output = util.unescapeAndUndelimit(",a");
        assertEquals("  a", stringListAsString(output));

        output = util.unescapeAndUndelimit(",");
        assertEquals("  ", stringListAsString(output));

        output = util.unescapeAndUndelimit("a\\,b\\,c,d");
        assertEquals("a,b,c  d", stringListAsString(output));

        output = util.unescapeAndUndelimit("a,b,c,\\,");
        assertEquals("a  b  c  ,", stringListAsString(output));

        output = util.unescapeAndUndelimit("\\,\\,,a,b,");
        assertEquals(",,  a  b  ", stringListAsString(output));
    }

    @Test
    public void testExceptionInUnescapeAndUndelimit() throws Exception
    {
        try
        {
            util.unescapeAndUndelimit("\\,\\,,a,b,\\");
            fail("No exception thrown");
        }
        catch(IllegalArgumentException ex)
        {
            if(!ex.getMessage().contains("index: 9"))
            {
                throw ex;
            }
        }

        try
        {
            util.unescapeAndUndelimit("\\,\\,,\\a,b,\\");
            fail("No exception thrown");
        }
        catch(IllegalArgumentException ex)
        {
            if(!ex.getMessage().contains("index: 5"))
            {
                throw ex;
            }
        }
    }

    private String stringListAsString(List<String> input)
    {
        return String.join("  ", input);
    }
}
