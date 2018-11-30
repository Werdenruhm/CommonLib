package CommonLib;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 
 */
public class Cacher 
{
    private final ConcurrentHashMap<String, Object> cacheHM = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> cacheHMKeysLOCK = new ConcurrentHashMap<>();
    private final Object cacheHMKeysLOCKLOCK = new Object();
    @SuppressWarnings("unchecked")
    public <T>T cache(String casheKey, SupplierTHROWS<T> valFunc)
    {
        casheKey = casheKey.toLowerCase();
        if (!cacheHMKeysLOCK.containsKey(casheKey))
        {
            synchronized (cacheHMKeysLOCKLOCK)
            {
                if (!cacheHMKeysLOCK.containsKey(casheKey))
                {
                    cacheHMKeysLOCK.put(casheKey, casheKey);
                }
            }
        }
        T result = null;
        if (!cacheHM.containsKey(casheKey))
        {
            synchronized (cacheHMKeysLOCK.get(casheKey))
            {
                if (!cacheHM.containsKey(casheKey))
                {
                    try {
                        result = valFunc.get();
                    } catch (RuntimeException rex) {
                        throw rex;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    if (null!=result) {
                        cacheHM.put(casheKey, result);
                    }
                }
            }
        }    
        if (result == null)
        {
            Object o = cacheHM.get(casheKey);
            if (o != null)
                result = (T)cacheHM.get(casheKey);
        }
        return result;
    }
    @FunctionalInterface
    public interface SupplierTHROWS<T> {
        T get() throws Exception;
    }
}
