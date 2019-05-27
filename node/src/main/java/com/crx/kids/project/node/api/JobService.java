package com.crx.kids.project.node.api;

import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.logic.QueensResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);


    public boolean stop() {
        return false;
    }


    public boolean start(int dimension) {
        return false;
    }

    public Result<QueensResult> result(int dimension) {
        return Result.of(new QueensResult());
    }


    public boolean pause() {
        return false;
    }




}
