package CommonLib;

/**
 *
 * 
 * @param <T>
 */
public interface CodeAnalizer<T extends CodeAnalizer.NonCodePlace> {
    T[] allNonCode();
    public interface NonCodePlace
    {
        char[] beg();
        char[] end();
    }
    @FunctionalInterface public static interface NextChar<T extends CodeAnalizer.NonCodePlace> { void call(T placeType, int chrN); }
    @FunctionalInterface public static interface NextPlace<T extends CodeAnalizer.NonCodePlace> { void call(T type, int begIx, int endIxExcl); }
    public default void parceCode(char[] codeText, NextChar<T> onNextChar, NextPlace<T> onNextPlace)
    {
        T cur = null;
        int curBeg = 0;
        for(int n = 0; n < codeText.length; n++)
            if (cur != null)
            {
                if (Common.ArrayContainsArrayAt(codeText, n, cur.end()))
                {
                    if (onNextChar != null)
                        for (int nn = n; nn < n + cur.end().length; nn++)
                            onNextChar.call(cur, nn);
                    n += cur.end().length - 1;
                    if (onNextPlace != null)
                        onNextPlace.call(cur, curBeg, n + 1);
                    cur = null;
                    curBeg = n + 1;
                }
                if (cur != null && onNextChar != null)
                    onNextChar.call(cur, n);
            }
            else
            {
                for(T iscur : allNonCode())
                    if (Common.ArrayContainsArrayAt(codeText, n, iscur.beg()))
                    {
                        if (n > 0)
                        {
                            if (onNextPlace != null)
                                onNextPlace.call(null, curBeg, n);
                        }
                        curBeg = n;
                        cur = iscur;
                        if (onNextChar != null)
                            for (int nn = n; nn < n + iscur.beg().length; nn++)
                                onNextChar.call(cur, nn);
                        n += iscur.beg().length - 1;
                        break;
                    }
                if (cur == null && onNextChar != null)
                    onNextChar.call(null, n);
            }
        if (cur != null)
            throw new RuntimeException("Code is incorrect - not finished " + cur + (Common.IsDebug() ? ". codeText: " + new String(codeText) : ""));
        if (curBeg < codeText.length && onNextPlace != null)
            onNextPlace.call(null, curBeg, codeText.length);
    }
}
