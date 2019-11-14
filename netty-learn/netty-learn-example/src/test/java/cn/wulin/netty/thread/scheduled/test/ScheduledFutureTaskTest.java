package cn.wulin.netty.thread.scheduled.test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

//import io.netty.util.concurrent.ScheduledFutureTask;

public class ScheduledFutureTaskTest {
	
	private static final long SCHEDULE_QUIET_PERIOD_INTERVAL = TimeUnit.SECONDS.toNanos(1);
	
	@Before
	public void before() {
//		 final ScheduledFutureTask<Void> quietPeriodTask = new ScheduledFutureTask<Void>(
//		            this, Executors.<Void>callable(new Runnable() {
//		        @Override
//		        public void run() {
//		            // NOOP
//		        }
//		    }, null), ScheduledFutureTask.deadlineNanos(SCHEDULE_QUIET_PERIOD_INTERVAL), -SCHEDULE_QUIET_PERIOD_INTERVAL);
	}
	
	@Test
	public void quietPeriodTaskTest() {
		
	}

}
