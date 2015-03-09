package org.cubyte.edumon.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StringUtil
{
    private StringUtil()
    {
    }

    public static String repeat(String string, int i)
    {
        StringBuilder sb = new StringBuilder();
        while (i > 0)
        {
            --i;
            sb.append(string);
        }
        return sb.toString();
    }

    public static String repeat(char string, int i)
    {
        StringBuilder sb = new StringBuilder();
        while (i > 0)
        {
            --i;
            sb.append(string);
        }
        return sb.toString();
    }

    /**
     * This method splits a string without RegExes
     *
     * @param delim  the delimiter
     * @param string the string to split
     *
     * @return an array containing the parts
     */
    public static String[] explode(String delim, String string)
    {
        return explode(delim, string, true);
    }

    /**
     * This method splits a string without RegExes
     *
     * @param delim          the delimiter
     * @param string         the string to split
     * @param keepEmptyParts whether to keep empty parts
     *
     * @return an array containing the parts
     */
    public static String[] explode(String delim, String string, boolean keepEmptyParts)
    {
        int pos, offset = 0, delimLen = delim.length();
        List<String> tokens = new ArrayList<String>();
        String part;

        while ((pos = string.indexOf(delim, offset)) > -1)
        {
            part = string.substring(offset, pos);
            if (part.length() > 0 || keepEmptyParts)
            {
                tokens.add(part);
            }
            offset = pos + delimLen;
        }
        part = string.substring(offset);
        if (part.length() > 0 || keepEmptyParts)
        {
            tokens.add(part);
        }

        return tokens.toArray(new String[tokens.size()]);
    }

    /**
     * This method merges an array of strings to a single string
     *
     * @param delim   the delimiter
     * @param strings the strings to implode
     *
     * @return the imploded string
     */
    public static String implode(String delim, String[] strings)
    {
        return implode(delim, Arrays.asList(strings));
    }

    /**
     * This method merges an array of strings to a single string
     *
     * @param delim   the delimiter
     * @param strings the strings to implode
     *
     * @return the imploded string
     */
    public static String implode(String delim, Iterable<String> strings)
    {
        Iterator<String> iterator = strings.iterator();
        if (!iterator.hasNext())
        {
            return "";
        }
        else
        {
            StringBuilder sb = new StringBuilder(iterator.next());

            while (iterator.hasNext())
            {
                sb.append(delim).append(iterator.next());
            }

            return sb.toString();
        }
    }
}
