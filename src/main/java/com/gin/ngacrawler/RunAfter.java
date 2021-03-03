package com.gin.ngacrawler;

import com.gin.ngacrawler.service.ScanService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动后执行
 *
 * @author bx002
 * @date 2020/11/19 13:56
 */
@Component
@Order(value = 1)
public class RunAfter implements ApplicationRunner {
    private final ScanService scanService;

    public RunAfter(ScanService scanService) {
        this.scanService = scanService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        scanService.autoScanContinue();
//        scanService.printOutContent();
    }
}
