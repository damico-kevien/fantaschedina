package com.fantacalcio.fantaschedina.scheduler;

import com.fantacalcio.fantaschedina.service.MatchdayClosingService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchdayCloseJob implements Job {

    public static final String MATCHDAY_ID_KEY = "matchdayId";

    private final MatchdayClosingService matchdayClosingService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        Long matchdayId = data.getLong(MATCHDAY_ID_KEY);
        matchdayClosingService.closeAndAutoSubmit(matchdayId);
    }
}