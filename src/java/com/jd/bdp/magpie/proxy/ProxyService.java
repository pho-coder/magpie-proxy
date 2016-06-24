package com.jd.bdp.magpie.proxy;

public interface ProxyService {

    public String echoStr(String str);

    public String getTaskInfo(String clusterId, String taskId);

    public String submitTask(String clusterId, String taskId, String jar, String klass, String group, String type);

    public String operateTask(String clusterId, String taskId, String command);
    
}
