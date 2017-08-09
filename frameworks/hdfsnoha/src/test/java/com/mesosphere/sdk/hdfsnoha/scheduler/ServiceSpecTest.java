package com.mesosphere.sdk.hdfsnoha.scheduler;

import com.mesosphere.sdk.testing.BaseServiceSpecTest;
import org.junit.Test;

public class ServiceSpecTest extends BaseServiceSpecTest {

    public ServiceSpecTest() {
        super(
                "EXECUTOR_URI", "",
                "LIBMESOS_URI", "",
                "PORT_API", "8080",
                "FRAMEWORK_NAME", "hdfsnoha",

                "TASKCFG_ALL_NAME_NODE_RPC_PORT","9001",
                "TASKCFG_ALL_NAME_NODE_HTTP_PORT","9002",
                "TASKCFG_ALL_DATA_NODE_RPC_PORT","9003",
                "TASKCFG_ALL_DATA_NODE_HTTP_PORT","9004",
                "TASKCFG_ALL_DATA_NODE_IPC_PORT","9005",

                "HDFS_DFS_REPLICATION","1",
                "NODE_COUNT", "2",
                "NODE_CPUS", "0.1",
                "NODE_MEM", "512",
                "NODE_DISK", "5000",
                "NODE_DISK_TYPE", "ROOT",

                "NAME_COUNT", "2",
                "NAME_CPUS", "0.1",
                "NAME_MEM", "512",
                "NAME_DISK", "5000",
                "NAME_DISK_TYPE", "ROOT",

                "SLEEP_DURATION", "1000");
    }

    @Test
    public void testYmlBase() throws Exception {
        testYaml("svc.yml");
    }
}
