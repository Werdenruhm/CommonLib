package CommonLib;

import java.util.LinkedHashMap;

/**
 *
 * 
 */
public class ConsoleInputInterpreter {
    public static class CommandHandler 
    { 
        public final String cmd; public final String description; public final boolean allowArgs; public final Common.Action1<String> handler; 
        public CommandHandler(String cmd, String description, boolean allowArgs, Common.Action1<String> handler) 
        { this.cmd = cmd; this.description = description; this.allowArgs = allowArgs; this.handler = handler; } 
    }               
    static boolean singletonStarted;
    public static void start(CommandHandler[] handlersA)
    {
        if (singletonStarted)
            throw new RuntimeException("InputInterpreter singleton already started!");
        Common.setTimeout(()->
        {
            try
            {
                StringBuilder sb = new StringBuilder();
                while (true)
                {
                    if (System.in.available() > 0)
                    {
                        byte[] b = new byte[100];
                        int l = System.in.read(b);
                        String s = new String(b, 0, l);
                        s = s.replace("\r\n", "\n").replace("\r", "\n").replace("\n\n", "\n");
                        if (s.contains("\n"))
                        {
                            sb.append(s.substring(0, s.indexOf("\n")));   
                            s = sb.toString().trim();
                            String a = null;
                            if (s.contains(" "))
                            {
                                a = s.substring(s.indexOf(" ") + 1);
                                s = s.substring(0, s.indexOf(" "));
                            }
                            if (handlers.containsKey(s))
                            {
                                CommandHandler h = handlers.get(s);
                                if (!h.allowArgs && a != null)
                                    System.out.println(sb.length() + ">'" + s + "' command cannot take (" + a + ") arguments");
                                else
                                {
                                    String m = sb.length() + "> command '" + s + (a == null ? "" : " " + a) + "' entered";
                                    System.out.println(m);
                                    try
                                    {
                                        h.handler.call(a);
                                    }
                                    catch(Throwable th)
                                    {
                                        System.out.println("(System.in handling) exception occured while executing handler of command  '" + s + "': " + Common.throwableToString(th, Common.getCurrentSTE()));
                                    }
                                }
                            }
                            else
                                System.out.println(sb.length() + ">command not found:" + s);
                            sb.setLength(0);
                        }
                        else
                            sb.append(s);
                    }

                    Thread.sleep(100);
                }
            }
            catch (Throwable th) {
                System.out.println("(System.in handling) error (thread stopped):" + Common.throwableToString(th, Common.getCurrentSTE()));
            }
        }, 0, true, "System.in handling thread");
        handlers.put("help", new CommandHandler("help", "Help.", false, (a)->{
            String m = "Available commands:";
            System.out.println(m);
            for(CommandHandler h : handlers.values())
            {
                System.out.println(h.cmd + "    -   "  + h.description);
                System.out.println();
            }
        }));
        handlers.put("stack", new CommandHandler("stack", "Print all threads stacks.", false, (a)->{
            System.out.println(Common.getAllStackTraces());
        }));
        handlers.put("debug", new CommandHandler("debug", "On/off debug mode. Argument: on / off", true, (a)->{
            if (a == null || !(a.equals("on") || a.equals("off")))
                System.out.println("debug command must have argument on / off (now isDebug = " + Common.isDebug + ")");
            else
            {
                String m = "(isDebug was = " + Common.isDebug + ")";
                System.out.println(m);
                Common.isDebug = a.equals("on");
            }
        }));
        for(CommandHandler h : handlersA)
            handlers.put(h.cmd, h);
        singletonStarted = true;
    }
    final static LinkedHashMap<String, CommandHandler> handlers = new LinkedHashMap<>();
    private ConsoleInputInterpreter() { }
}
