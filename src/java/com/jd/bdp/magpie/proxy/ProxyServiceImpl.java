package com.jd.bdp.magpie.proxy;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class ProxyServiceImpl implements ProxyService {
    private static IFn requireFn = RT.var("clojure.core", "require").fn();
    static {
        requireFn.invoke(Symbol.intern("com.jd.bdp.magpie.magpie-proxy.utils"));
    }
    private static IFn mockEchoStr = RT.var("com.jd.bdp.magpie.magpie-proxy.utils", "mock-echo-str").fn();
    private static IFn getTaskStatus = RT.var("com.jd.bdp.magpie.magpie-proxy.proxy", "get-task-status").fn();
    private static IFn mockSubmitTask = RT.var("com.jd.bdp.magpie.magpie-proxy.core", "mock-submit-task").fn();
    private static IFn mockOperateTask = RT.var("com.jd.bdp.magpie.magpie-proxy.core", "mock-operate-task").fn();

    @Override
    public String echoStr(String str) {
        return (String)mockEchoStr.invoke(str);
    }

    @Override
    public String getTaskStatus(String clusterId, String taskId) {
        return (String)getTaskStatus.invoke(clusterId, taskId);
    }

    @Override
    public String submitTask(String clusterId, String taskId, String jar, String klass, String group, String type) {
        return (String)mockSubmitTask.invoke(clusterId, taskId, jar, klass, group, type);
    }
    
    @Override
    public String operateTask(String clusterId, String taskId, String command) {
        return (String)mockOperateTask.invoke(clusterId, taskId, command);
    }
}
