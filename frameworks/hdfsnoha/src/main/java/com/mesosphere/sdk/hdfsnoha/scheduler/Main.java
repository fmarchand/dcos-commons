package com.mesosphere.sdk.hdfsnoha.scheduler;

import com.mesosphere.sdk.scheduler.SchedulerFlags;
import com.mesosphere.sdk.specification.*;

import java.io.File;

/**
 * Template service.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            new DefaultService(new File(args[0]), SchedulerFlags.fromEnv()).run();
        }
    }
}
