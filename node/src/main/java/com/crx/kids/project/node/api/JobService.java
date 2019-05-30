package com.crx.kids.project.node.api;

import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.logic.QueensResult;
import com.crx.kids.project.node.logic.QueensService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private QueensService queensService;


    public boolean stop() {
        return false;
    }


    public Result start(int dimension) {

        queensService.calculateJobsByDimension(dimension);
        queensService.startWorkForDimension(dimension);

        return Result.of(null);
    }

    public Result<QueensResult> result(int dimension) {
        return Result.of(new QueensResult());
    }


    public boolean pause() {
        return false;
    }




}
