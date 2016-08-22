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
    private static IFn getTaskInfo = RT.var("com.jd.bdp.magpie.magpie-proxy.proxy", "get-task-info").fn();
    private static IFn getTasksInfo = RT.var("com.jd.bdp.magpie.magpie-proxy.proxy", "get-tasks-info").fn();
    private static IFn submitTask = RT.var("com.jd.bdp.magpie.magpie-proxy.proxy", "submit-task").fn();
    private static IFn operateTask = RT.var("com.jd.bdp.magpie.magpie-proxy.proxy", "operate-task").fn();

    @Override
    public String echoStr(String str) {
        return (String)mockEchoStr.invoke(str);
    }

    @Override
    public String getTaskInfo(String clusterId, String taskId) {
        return (String)getTaskInfo.invoke(clusterId, taskId);
    }

    @Override
    public String getTasksInfo(String clusterId) {
        return (String)getTasksInfo.invoke(clusterId);
    }
    
    @Override
    public String submitTask(String clusterId, String taskId, String jar, String klass, String group, String type) {
        return (String)submitTask.invoke(clusterId, taskId, jar, klass, group, type);
    }
    
    @Override
    public String operateTask(String clusterId, String taskId, String command) {
        return (String)operateTask.invoke(clusterId, taskId, command);
    }
}
